// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.agent.manager.allocator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.manager.allocator.PodAllocator;
import org.apache.capacity.CapacityVO;
import org.apache.capacity.dao.CapacityDao;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.dc.DataCenter;
import org.apache.dc.DataCenterVO;
import org.apache.dc.HostPodVO;
import org.apache.dc.Pod;
import org.apache.dc.dao.HostPodDao;
import org.apache.log4j.Logger;
import org.apache.offering.ServiceOffering;
import org.apache.service.ServiceOfferingVO;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.storage.VolumeVO;
import org.apache.storage.dao.VMTemplateHostDao;
import org.apache.storage.dao.VMTemplatePoolDao;
import org.apache.storage.dao.VolumeDao;
import org.apache.template.VirtualMachineTemplate;
import org.apache.utils.DateUtil;
import org.apache.utils.NumbersUtil;
import org.apache.utils.Pair;
import org.apache.utils.component.AdapterBase;
import org.apache.utils.db.SearchCriteria;
import org.apache.vm.UserVmVO;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.VirtualMachine.State;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.VMInstanceDao;

@Local(value = PodAllocator.class)
public class UserConcentratedAllocator extends AdapterBase implements PodAllocator {
    private final static Logger s_logger = Logger.getLogger(UserConcentratedAllocator.class);

    @Inject
    UserVmDao _vmDao;
    @Inject
    VolumeDao _volumeDao;
    @Inject
    HostPodDao _podDao;
    @Inject
    VMTemplatePoolDao _templatePoolDao;
    @Inject
    ServiceOfferingDao _offeringDao;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    VMInstanceDao _vmInstanceDao;

    Random _rand = new Random(System.currentTimeMillis());
    private int _secondsToSkipStoppedVMs = 86400;
    private int _secondsToSkipDestroyedVMs = 0;

    @Override
    public Pair<HostPodVO, Long> allocateTo(VirtualMachineTemplate template, ServiceOfferingVO offering, DataCenterVO zone, long accountId,
            Set<Long> avoids) {
        long zoneId = zone.getId();
        List<HostPodVO> podsInZone = _podDao.listByDataCenterId(zoneId);

        if (podsInZone.size() == 0) {
            s_logger.debug("No pods found in zone " + zone.getName());
            return null;
        }

        // Find pods that have enough CPU/memory capacity
        List<HostPodVO> availablePods = new ArrayList<HostPodVO>();
        Map<Long, Long> podHostCandidates = new HashMap<Long, Long>();
        for (HostPodVO pod : podsInZone) {
            long podId = pod.getId();
            if (!avoids.contains(podId)) {
                if (template != null && !templateAvailableInPod(template.getId(), pod.getDataCenterId(), podId)) {
                    continue;
                }

                if (offering != null) {
                    // test for enough memory in the pod (make sure to check for
                    // enough memory for the service offering, plus
                    // some extra padding for xen overhead
                    long[] hostCandiates = new long[1];
                    boolean enoughCapacity = dataCenterAndPodHasEnoughCapacity(zoneId, podId, (offering.getRamSize()) * 1024L * 1024L,
                            CapacityVO.CAPACITY_TYPE_MEMORY, hostCandiates);

                    if (!enoughCapacity) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Not enough RAM available in zone/pod to allocate storage for user VM (zone: " + zoneId + ", pod: "
                                    + podId + ")");
                        }
                        continue;
                    }

                    // test for enough CPU in the pod
                    enoughCapacity = dataCenterAndPodHasEnoughCapacity(zoneId, podId, (offering.getCpu() * offering.getSpeed()),
                            CapacityVO.CAPACITY_TYPE_CPU, hostCandiates);
                    if (!enoughCapacity) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Not enough cpu available in zone/pod to allocate storage for user VM (zone: " + zoneId + ", pod: "
                                    + podId + ")");
                        }
                        continue;
                    }

                    podHostCandidates.put(podId, hostCandiates[0]);
                }

                // If the pod has VMs or volumes in it, return this pod
                List<UserVmVO> vmsInPod = _vmDao.listByAccountAndPod(accountId, pod.getId());
                if (!vmsInPod.isEmpty()) {
                    return new Pair<HostPodVO, Long>(pod, podHostCandidates.get(podId));
                }

                List<VolumeVO> volumesInPod = _volumeDao.findByAccountAndPod(accountId, pod.getId());
                if (!volumesInPod.isEmpty()) {
                    return new Pair<HostPodVO, Long>(pod, podHostCandidates.get(podId));
                }

                availablePods.add(pod);
            }
        }

        if (availablePods.size() == 0) {
            s_logger.debug("There are no pods with enough memory/CPU capacity in zone " + zone.getName());
            return null;
        } else {
            // Return a random pod
            int next = _rand.nextInt(availablePods.size());
            HostPodVO selectedPod = availablePods.get(next);
            s_logger.debug("Found pod " + selectedPod.getName() + " in zone " + zone.getName());
            return new Pair<HostPodVO, Long>(selectedPod, podHostCandidates.get(selectedPod.getId()));
        }
    }

    private boolean dataCenterAndPodHasEnoughCapacity(long dataCenterId, long podId, long capacityNeeded, short capacityType, long[] hostCandidate) {
        List<CapacityVO> capacities = null;

        SearchCriteria<CapacityVO> sc = _capacityDao.createSearchCriteria();
        sc.addAnd("capacityType", SearchCriteria.Op.EQ, capacityType);
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, dataCenterId);
        sc.addAnd("podId", SearchCriteria.Op.EQ, podId);
        s_logger.trace("Executing search");
        capacities = _capacityDao.search(sc, null);
        s_logger.trace("Done with a search");

        boolean enoughCapacity = false;
        if (capacities != null) {
            for (CapacityVO capacity : capacities) {
                if (capacityType == CapacityVO.CAPACITY_TYPE_CPU || capacityType == CapacityVO.CAPACITY_TYPE_MEMORY) {
                    //
                    // for CPU/Memory, we now switch to static allocation
                    //
                    if ((capacity.getTotalCapacity() - calcHostAllocatedCpuMemoryCapacity(capacity.getHostOrPoolId(), capacityType)) >= capacityNeeded) {

                        hostCandidate[0] = capacity.getHostOrPoolId();
                        enoughCapacity = true;
                        break;
                    }
                } else {
                    if ((capacity.getTotalCapacity() - capacity.getUsedCapacity()) >= capacityNeeded) {
                        hostCandidate[0] = capacity.getHostOrPoolId();
                        enoughCapacity = true;
                        break;
                    }
                }
            }
        }
        return enoughCapacity;
    }

    private boolean skipCalculation(VMInstanceVO vm) {
        if (vm.getState() == State.Expunging) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Skip counting capacity for Expunging VM : " + vm.getInstanceName());
            }
            return true;
        }

        if (vm.getState() == State.Destroyed && vm.getType() != VirtualMachine.Type.User) {
            return true;
        }

        if (vm.getState() == State.Stopped || vm.getState() == State.Destroyed) {
            // for Stopped/Destroyed VMs, we will skip counting it if it hasn't
            // been used for a while
            int secondsToSkipVMs = _secondsToSkipStoppedVMs;

            if (vm.getState() == State.Destroyed) {
                secondsToSkipVMs = _secondsToSkipDestroyedVMs;
            }

            long millisecondsSinceLastUpdate = DateUtil.currentGMTTime().getTime() - vm.getUpdateTime().getTime();
            if (millisecondsSinceLastUpdate > secondsToSkipVMs * 1000L) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Skip counting " + vm.getState().toString() + " vm " + vm.getInstanceName()
                            + " in capacity allocation as it has been " + vm.getState().toString().toLowerCase() + " for "
                            + millisecondsSinceLastUpdate / 60000 + " minutes");
                }
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param hostId
     *            Host id to calculate against
     * @param capacityType
     *            CapacityVO.CAPACITY_TYPE_MEMORY or
     *            CapacityVO.CAPACITY_TYPE_CPU
     * @return
     */
    private long calcHostAllocatedCpuMemoryCapacity(long hostId, short capacityType) {
        assert (capacityType == CapacityVO.CAPACITY_TYPE_MEMORY || capacityType == CapacityVO.CAPACITY_TYPE_CPU) : "Invalid capacity type passed in calcHostAllocatedCpuCapacity()";

        // List<VMInstanceVO> vms = _vmInstanceDao.listByLastHostId(hostId);
        List<VMInstanceVO> vms = null;
        long usedCapacity = 0;
        for (VMInstanceVO vm : vms) {
            if (skipCalculation(vm)) {
                continue;
            }

            ServiceOffering so = null;

            if (vm.getType() == VirtualMachine.Type.User) {
                UserVmVO userVm = _vmDao.findById(vm.getId());
                if (userVm == null) {
                    continue;
                }
            }

            so = _offeringDao.findById(vm.getServiceOfferingId());

            if (capacityType == CapacityVO.CAPACITY_TYPE_MEMORY) {
                usedCapacity += so.getRamSize() * 1024L * 1024L;

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Counting memory capacity used by vm: " + vm.getId() + ", size: " + so.getRamSize() + "MB, host: " + hostId
                            + ", currently counted: " + usedCapacity + " Bytes");
                }
            } else if (capacityType == CapacityVO.CAPACITY_TYPE_CPU) {
                usedCapacity += so.getCpu() * so.getSpeed();

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Counting cpu capacity used by vm: " + vm.getId() + ", cpu: " + so.getCpu() + ", speed: " + so.getSpeed()
                            + ", currently counted: " + usedCapacity + " Bytes");
                }
            }
        }

        return usedCapacity;
    }

    private boolean templateAvailableInPod(long templateId, long dcId, long podId) {
        return true;
        /*
         * List<VMTemplateHostVO> thvoList = _templateHostDao.listByTemplateStatus(templateId, dcId, podId, Status.DOWNLOADED);
         * List<VMTemplateStoragePoolVO> tpvoList = _templatePoolDao.listByTemplateStatus(templateId, dcId, podId,
         * Status.DOWNLOADED);
         *
         * if (thvoList != null && thvoList.size() > 0) { if (s_logger.isDebugEnabled()) { s_logger.debug("Found " +
         * thvoList.size() + " storage hosts in pod " + podId + " with template " + templateId); } return true; } else if
         * (tpvoList != null && tpvoList.size() > 0) { if (s_logger.isDebugEnabled()) { s_logger.debug("Found " +
         * tpvoList.size() + " storage pools in pod " + podId + " with template " + templateId); } return true; }else { return
         * false; }
         */
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        Map<String, String> configs = _configDao.getConfiguration("management-server", params);
        String stoppedValue = configs.get("vm.resource.release.interval");
        // String destroyedValue =
        // configs.get("capacity.skipcounting.destroyed.hours");
        String destroyedValue = null;
        _secondsToSkipStoppedVMs = NumbersUtil.parseInt(stoppedValue, 86400);
        _secondsToSkipDestroyedVMs = NumbersUtil.parseInt(destroyedValue, 0);

        /*
         * ComponentLocator locator = ComponentLocator.getCurrentLocator(); _vmDao = locator.getDao(UserVmDao.class); if (_vmDao
         * == null) { throw new ConfigurationException("Unable to find UserVMDao."); }
         *
         * _volumeDao = locator.getDao(VolumeDao.class); if (_volumeDao == null) { throw new
         * ConfigurationException("Unable to find VolumeDao."); }
         *
         * _templateHostDao = locator.getDao(VMTemplateHostDao.class); if (_templateHostDao == null) { throw new
         * ConfigurationException("Unable to get template host dao."); }
         *
         * _templatePoolDao = locator.getDao(VMTemplatePoolDao.class); if (_templatePoolDao == null) { throw new
         * ConfigurationException("Unable to get template pool dao."); }
         *
         * _podDao = locator.getDao(HostPodDao.class); if (_podDao == null) { throw new
         * ConfigurationException("Unable to find HostPodDao."); }
         *
         * _capacityDao = locator.getDao(CapacityDao.class); if (_capacityDao == null) { throw new
         * ConfigurationException("Unable to retrieve " + CapacityDao.class); }
         */
        return true;
    }

    @Override
    public Pod allocateTo(VirtualMachineProfile vm, DataCenter dc, Set<? extends Pod> avoids) {
        return null;
    }
}