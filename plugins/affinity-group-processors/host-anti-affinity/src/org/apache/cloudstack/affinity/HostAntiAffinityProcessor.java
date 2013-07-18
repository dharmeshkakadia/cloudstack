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
package org.apache.cloudstack.affinity;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.framework.messagebus.MessageSubscriber;
import org.apache.configuration.Config;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.deploy.DeploymentPlan;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.exception.AffinityConflictException;
import org.apache.log4j.Logger;
import org.apache.utils.DateUtil;
import org.apache.utils.NumbersUtil;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.VMInstanceDao;

@Local(value = AffinityGroupProcessor.class)
public class HostAntiAffinityProcessor extends AffinityProcessorBase implements AffinityGroupProcessor {

    private static final Logger s_logger = Logger.getLogger(HostAntiAffinityProcessor.class);
    @Inject
    protected UserVmDao _vmDao;
    @Inject
    protected VMInstanceDao _vmInstanceDao;
    @Inject
    protected AffinityGroupDao _affinityGroupDao;
    @Inject
    protected AffinityGroupVMMapDao _affinityGroupVMMapDao;
    private int _vmCapacityReleaseInterval;
    @Inject 
    protected ConfigurationDao _configDao;
    
    @Override
    public void process(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan,
            ExcludeList avoid)
            throws AffinityConflictException {
        VirtualMachine vm = vmProfile.getVirtualMachine();
        List<AffinityGroupVMMapVO> vmGroupMappings = _affinityGroupVMMapDao.findByVmIdType(vm.getId(), getType());

        for (AffinityGroupVMMapVO vmGroupMapping : vmGroupMappings) {
            if (vmGroupMapping != null) {
                AffinityGroupVO group = _affinityGroupDao.findById(vmGroupMapping.getAffinityGroupId());

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Processing affinity group " + group.getName() + " for VM Id: " + vm.getId());
                }

                List<Long> groupVMIds = _affinityGroupVMMapDao.listVmIdsByAffinityGroup(group.getId());
                groupVMIds.remove(vm.getId());

                for (Long groupVMId : groupVMIds) {
                    VMInstanceVO groupVM = _vmInstanceDao.findById(groupVMId);
                    if (groupVM != null && !groupVM.isRemoved()) {
                        if (groupVM.getHostId() != null) {
                            avoid.addHost(groupVM.getHostId());
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Added host " + groupVM.getHostId() + " to avoid set, since VM "
                                        + groupVM.getId() + " is present on the host");
                            }
                        } else if (VirtualMachine.State.Stopped.equals(groupVM.getState())
                                && groupVM.getLastHostId() != null) {
                            long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - groupVM.getUpdateTime().getTime()) / 1000;
                            if (secondsSinceLastUpdate < _vmCapacityReleaseInterval) {
                                avoid.addHost(groupVM.getLastHostId());
                                if (s_logger.isDebugEnabled()) {
                                    s_logger.debug("Added host " + groupVM.getLastHostId() + " to avoid set, since VM "
                                            + groupVM.getId() + " is present on the host, in Stopped state but has reserved capacity");
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _vmCapacityReleaseInterval = NumbersUtil.parseInt(_configDao.getValue(Config.CapacitySkipcountingHours.key()),3600);
        return true;
    }

}
