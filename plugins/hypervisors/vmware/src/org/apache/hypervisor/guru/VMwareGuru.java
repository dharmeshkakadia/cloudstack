
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
package org.apache.hypervisor.guru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.api.BackupSnapshotCommand;
import org.apache.agent.api.Command;
import org.apache.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import org.apache.agent.api.CreatePrivateTemplateFromVolumeCommand;
import org.apache.agent.api.CreateVolumeFromSnapshotCommand;
import org.apache.agent.api.UnregisterVMCommand;
import org.apache.agent.api.storage.CopyVolumeCommand;
import org.apache.agent.api.storage.CreateVolumeOVACommand;
import org.apache.agent.api.storage.PrepareOVAPackingCommand;
import org.apache.agent.api.storage.PrimaryStorageDownloadCommand;
import org.apache.agent.api.to.DataObjectType;
import org.apache.agent.api.to.DataStoreTO;
import org.apache.agent.api.to.DataTO;
import org.apache.agent.api.to.NicTO;
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cluster.ClusterManager;
import org.apache.configuration.Config;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.host.dao.HostDetailsDao;
import org.apache.hypervisor.HypervisorGuru;
import org.apache.hypervisor.HypervisorGuruBase;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.hypervisor.vmware.manager.VmwareManager;
import org.apache.hypervisor.vmware.mo.VirtualEthernetCardType;
import org.apache.log4j.Logger;
import org.apache.network.NetworkModel;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkVO;
import org.apache.secstorage.CommandExecLogDao;
import org.apache.secstorage.CommandExecLogVO;
import org.apache.storage.DataStoreRole;
import org.apache.storage.GuestOSVO;
import org.apache.storage.dao.GuestOSDao;
import org.apache.storage.secondary.SecondaryStorageVmManager;
import org.apache.template.VirtualMachineTemplate.BootloaderType;
import org.apache.utils.Pair;
import org.apache.utils.db.DB;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.net.NetUtils;
import org.apache.vm.ConsoleProxyVO;
import org.apache.vm.DomainRouterVO;
import org.apache.vm.NicProfile;
import org.apache.vm.SecondaryStorageVmVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.VmDetailConstants;
import org.springframework.stereotype.Component;

@Local(value=HypervisorGuru.class)
public class VMwareGuru extends HypervisorGuruBase implements HypervisorGuru {
    private static final Logger s_logger = Logger.getLogger(VMwareGuru.class);

    @Inject NetworkDao _networkDao;
    @Inject GuestOSDao _guestOsDao;
    @Inject HostDao _hostDao;
    @Inject HostDetailsDao _hostDetailsDao;
    @Inject CommandExecLogDao _cmdExecLogDao;
    @Inject ClusterManager _clusterMgr;
    @Inject VmwareManager _vmwareMgr;
    @Inject SecondaryStorageVmManager _secStorageMgr;
    @Inject NetworkModel _networkMgr;
    @Inject ConfigurationDao _configDao;

    protected VMwareGuru() {
        super();
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.VMware;
    }

    @Override
    public <T extends VirtualMachine> VirtualMachineTO implement(VirtualMachineProfile<T> vm) {
        VirtualMachineTO to = toVirtualMachineTO(vm);
        to.setBootloader(BootloaderType.HVM);

        Map<String, String> details = to.getDetails();
        if(details == null)
            details = new HashMap<String, String>();

        String nicDeviceType = details.get(VmDetailConstants.NIC_ADAPTER);
        if(vm.getVirtualMachine() instanceof DomainRouterVO || vm.getVirtualMachine() instanceof ConsoleProxyVO 
                || vm.getVirtualMachine() instanceof SecondaryStorageVmVO) {

            if(nicDeviceType == null) {
                details.put(VmDetailConstants.NIC_ADAPTER, _vmwareMgr.getSystemVMDefaultNicAdapterType());
            } else {
                try {
                    VirtualEthernetCardType.valueOf(nicDeviceType);
                } catch (Exception e) {
                    s_logger.warn("Invalid NIC device type " + nicDeviceType + " is specified in VM details, switch to default E1000");
                    details.put(VmDetailConstants.NIC_ADAPTER, VirtualEthernetCardType.E1000.toString());
                }
            }
        } else {
            // for user-VM, use E1000 as default
            if(nicDeviceType == null) {
                details.put(VmDetailConstants.NIC_ADAPTER, VirtualEthernetCardType.E1000.toString());
            } else {
                try {
                    VirtualEthernetCardType.valueOf(nicDeviceType);
                } catch (Exception e) {
                    s_logger.warn("Invalid NIC device type " + nicDeviceType + " is specified in VM details, switch to default E1000");
                    details.put(VmDetailConstants.NIC_ADAPTER, VirtualEthernetCardType.E1000.toString());
                }
            }
        }
        
        String diskDeviceType = details.get(VmDetailConstants.ROOK_DISK_CONTROLLER);
        if (!(vm.getVirtualMachine() instanceof DomainRouterVO || vm.getVirtualMachine() instanceof ConsoleProxyVO 
            || vm.getVirtualMachine() instanceof SecondaryStorageVmVO)){
            // user vm
            if (diskDeviceType == null){
		    details.put(VmDetailConstants.ROOK_DISK_CONTROLLER, _vmwareMgr.getRootDiskController());
            }
        }

        List<NicProfile> nicProfiles = vm.getNics();

        for(NicProfile nicProfile : nicProfiles) {
            if(nicProfile.getTrafficType() == TrafficType.Guest) {
                if(_networkMgr.isProviderSupportServiceInNetwork(nicProfile.getNetworkId(), Service.Firewall, Provider.CiscoVnmc)) {
                    details.put("ConfigureVServiceInNexus", Boolean.TRUE.toString());
                }
                break;
            }
        }

        to.setDetails(details);

        if(vm.getVirtualMachine() instanceof DomainRouterVO) {

            NicProfile publicNicProfile = null;
            for(NicProfile nicProfile : nicProfiles) {
                if(nicProfile.getTrafficType() == TrafficType.Public) {
                    publicNicProfile = nicProfile;
                    break;
                }
            }

            if(publicNicProfile != null) {
                NicTO[] nics = to.getNics();

                // reserve extra NICs
                NicTO[] expandedNics = new NicTO[nics.length + _vmwareMgr.getRouterExtraPublicNics()];
                int i = 0;
                int deviceId = -1;
                for(i = 0; i < nics.length; i++) {
                    expandedNics[i] = nics[i];
                    if(nics[i].getDeviceId() > deviceId)
                        deviceId = nics[i].getDeviceId();
                }
                deviceId++;

                long networkId = publicNicProfile.getNetworkId();
                NetworkVO network = _networkDao.findById(networkId);

                for(; i < nics.length + _vmwareMgr.getRouterExtraPublicNics(); i++) {
                    NicTO nicTo = new NicTO();

                    nicTo.setDeviceId(deviceId++);
                    nicTo.setBroadcastType(publicNicProfile.getBroadcastType());
                    nicTo.setType(publicNicProfile.getTrafficType());
                    nicTo.setIp("0.0.0.0");
                    nicTo.setNetmask("255.255.255.255");

                    try {
                        String mac = _networkMgr.getNextAvailableMacAddressInNetwork(networkId);
                        nicTo.setMac(mac);
                    } catch (InsufficientAddressCapacityException e) {
                        throw new CloudRuntimeException("unable to allocate mac address on network: " + networkId);
                    }
                    nicTo.setDns1(publicNicProfile.getDns1());
                    nicTo.setDns2(publicNicProfile.getDns2());
                    if (publicNicProfile.getGateway() != null) {
                        nicTo.setGateway(publicNicProfile.getGateway());
                    } else {
                        nicTo.setGateway(network.getGateway());
                    }
                    nicTo.setDefaultNic(false);
                    nicTo.setBroadcastUri(publicNicProfile.getBroadCastUri());
                    nicTo.setIsolationuri(publicNicProfile.getIsolationUri());

                    Integer networkRate = _networkMgr.getNetworkRate(network.getId(), null);
                    nicTo.setNetworkRateMbps(networkRate);

                    expandedNics[i] = nicTo;
                }

                to.setNics(expandedNics);
            }

            StringBuffer sbMacSequence = new StringBuffer();
            for(NicTO nicTo : sortNicsByDeviceId(to.getNics())) {
                sbMacSequence.append(nicTo.getMac()).append("|");
            }
            sbMacSequence.deleteCharAt(sbMacSequence.length() - 1);
            String bootArgs = to.getBootArgs();
            to.setBootArgs(bootArgs + " nic_macs=" + sbMacSequence.toString());
            
        }
        
        // Don't do this if the virtual machine is one of the special types
        // Should only be done on user machines
        if(!(vm.getVirtualMachine() instanceof DomainRouterVO || vm.getVirtualMachine() instanceof ConsoleProxyVO 
                || vm.getVirtualMachine() instanceof SecondaryStorageVmVO)) {
            String nestedVirt = _configDao.getValue(Config.VmwareEnableNestedVirtualization.key());
            if (nestedVirt != null) {
                s_logger.debug("Nested virtualization requested, adding flag to vm configuration");
                details.put(VmDetailConstants.NESTED_VIRTUALIZATION_FLAG, nestedVirt);
                to.setDetails(details);
                
            }
        }
        // Determine the VM's OS description
        GuestOSVO guestOS = _guestOsDao.findById(vm.getVirtualMachine().getGuestOSId());
        to.setOs(guestOS.getDisplayName());
        return to;
    }

    private NicTO[] sortNicsByDeviceId(NicTO[] nics) {

        List<NicTO> listForSort = new ArrayList<NicTO>();
        for (NicTO nic : nics) {
            listForSort.add(nic);
        }
        Collections.sort(listForSort, new Comparator<NicTO>() {

            @Override
            public int compare(NicTO arg0, NicTO arg1) {
                if (arg0.getDeviceId() < arg1.getDeviceId()) {
                    return -1;
                } else if (arg0.getDeviceId() == arg1.getDeviceId()) {
                    return 0;
                }

                return 1;
            }
        });

        return listForSort.toArray(new NicTO[0]);
    }

    @Override @DB
    public long getCommandHostDelegation(long hostId, Command cmd) {
        boolean needDelegation = false;

        if(cmd instanceof PrimaryStorageDownloadCommand || 
                cmd instanceof BackupSnapshotCommand ||
                cmd instanceof CreatePrivateTemplateFromVolumeCommand ||
                cmd instanceof CreatePrivateTemplateFromSnapshotCommand ||
                cmd instanceof CopyVolumeCommand ||
                cmd instanceof CreateVolumeOVACommand ||
                cmd instanceof PrepareOVAPackingCommand ||
                cmd instanceof CreateVolumeFromSnapshotCommand ||
                cmd instanceof CopyCommand) {
            if (cmd instanceof CopyCommand) {
                CopyCommand cpyCommand = (CopyCommand)cmd;
                DataTO srcData = cpyCommand.getSrcTO();
                DataStoreTO srcStoreTO = srcData.getDataStore();
                DataTO destData = cpyCommand.getDestTO();
                DataStoreTO destStoreTO = destData.getDataStore();
                
                if (destData.getObjectType() == DataObjectType.VOLUME && destStoreTO.getRole() == DataStoreRole.Primary &&
                        srcData.getObjectType() == DataObjectType.TEMPLATE && srcStoreTO.getRole() == DataStoreRole.Primary) {
                    needDelegation = false;
                } else {
                    needDelegation = true;
                }
            } else {
                needDelegation = true;
            }
            
        }
        /* Fang: remove this before checking in */
        // needDelegation = false;

        if (cmd instanceof PrepareOVAPackingCommand ||
                cmd instanceof CreateVolumeOVACommand	) {
                cmd.setContextParam("hypervisor", HypervisorType.VMware.toString());
        }
        if(needDelegation) {
            HostVO host = _hostDao.findById(hostId);
            assert(host != null);
            assert(host.getHypervisorType() == HypervisorType.VMware);
            long dcId = host.getDataCenterId();

            Pair<HostVO, SecondaryStorageVmVO> cmdTarget = _secStorageMgr.assignSecStorageVm(dcId, cmd);
            if(cmdTarget != null) {
                // TODO, we need to make sure agent is actually connected too
                cmd.setContextParam("hypervisor", HypervisorType.VMware.toString());
                Map<String, String> hostDetails = _hostDetailsDao.findDetails(hostId);
                cmd.setContextParam("guid", resolveNameInGuid(hostDetails.get("guid")));
                cmd.setContextParam("username", hostDetails.get("username"));
                cmd.setContextParam("password", hostDetails.get("password"));
                cmd.setContextParam("serviceconsole", _vmwareMgr.getServiceConsolePortGroupName());
                cmd.setContextParam("manageportgroup", _vmwareMgr.getManagementPortGroupName());

                CommandExecLogVO execLog = new CommandExecLogVO(cmdTarget.first().getId(), cmdTarget.second().getId(), cmd.getClass().getSimpleName(), 1);
                _cmdExecLogDao.persist(execLog);
                cmd.setContextParam("execid", String.valueOf(execLog.getId()));

                if(cmd instanceof BackupSnapshotCommand || 
                        cmd instanceof CreatePrivateTemplateFromVolumeCommand || 
                        cmd instanceof CreatePrivateTemplateFromSnapshotCommand ||
                        cmd instanceof CopyVolumeCommand ||
                        cmd instanceof CreateVolumeOVACommand ||
                        cmd instanceof PrepareOVAPackingCommand ||
                        cmd instanceof CreateVolumeFromSnapshotCommand) {

                    String workerName = _vmwareMgr.composeWorkerName();
                    long checkPointId = 1;
// FIXME: Fix                    long checkPointId = _checkPointMgr.pushCheckPoint(new VmwareCleanupMaid(hostDetails.get("guid"), workerName));
                    cmd.setContextParam("worker", workerName);
                    cmd.setContextParam("checkpoint", String.valueOf(checkPointId));

                    // some commands use 2 workers
                    String workerName2 = _vmwareMgr.composeWorkerName();
                    long checkPointId2 = 1;
// FIXME: Fix                    long checkPointId2 = _checkPointMgr.pushCheckPoint(new VmwareCleanupMaid(hostDetails.get("guid"), workerName2));
                    cmd.setContextParam("worker2", workerName2);
                    cmd.setContextParam("checkpoint2", String.valueOf(checkPointId2));
                }

                return cmdTarget.first().getId();
            }
        }

        return hostId;
    }

    @Override
    public boolean trackVmHostChange() {
        return true;
    }

    private static String resolveNameInGuid(String guid) {
        String tokens[] = guid.split("@");
        assert(tokens.length == 2);

        String vCenterIp = NetUtils.resolveToIp(tokens[1]);
        if(vCenterIp == null) {
            s_logger.error("Fatal : unable to resolve vCenter address " + tokens[1] + ", please check your DNS configuration");
            return guid;
        }

        if(vCenterIp.equals(tokens[1]))
            return guid;

        return tokens[0] + "@" + vCenterIp;
    }
    
    @Override
    public List<Command> finalizeExpunge(VirtualMachine vm) {
        UnregisterVMCommand unregisterVMCommand = new UnregisterVMCommand(vm.getInstanceName());
        List<Command> commands = new ArrayList<Command>();
        commands.add(unregisterVMCommand);
        return commands;
    }
}
