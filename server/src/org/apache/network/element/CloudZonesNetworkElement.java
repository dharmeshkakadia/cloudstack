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
package org.apache.network.element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.AgentManager;
import org.apache.agent.AgentManager.OnError;
import org.apache.agent.api.Answer;
import org.apache.agent.api.routing.SavePasswordCommand;
import org.apache.agent.api.routing.VmDataCommand;
import org.apache.agent.manager.Commands;
import org.apache.configuration.ConfigurationManager;
import org.apache.configuration.ZoneConfig;
import org.apache.dc.DataCenterVO;
import org.apache.dc.dao.DataCenterDao;
import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.OperationTimedoutException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkModel;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.Network.Capability;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.NetworkDao;
import org.apache.network.element.NetworkElement;
import org.apache.network.element.UserDataServiceProvider;
import org.apache.offering.NetworkOffering;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.uservm.UserVm;
import org.apache.utils.PasswordGenerator;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.UserVmManager;
import org.apache.vm.UserVmVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.dao.DomainRouterDao;
import org.apache.vm.dao.UserVmDao;

@Local(value = NetworkElement.class)
public class CloudZonesNetworkElement extends AdapterBase implements NetworkElement, UserDataServiceProvider {
    private static final Logger s_logger = Logger.getLogger(CloudZonesNetworkElement.class);

    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();

    @Inject
    NetworkDao _networkConfigDao;
    @Inject
    NetworkModel _networkMgr;
    @Inject
    UserVmManager _userVmMgr;
    @Inject
    UserVmDao _userVmDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    AgentManager _agentManager;
    @Inject
    ServiceOfferingDao _serviceOfferingDao;

    private boolean canHandle(DeployDestination dest, TrafficType trafficType) {
        DataCenterVO dc = (DataCenterVO) dest.getDataCenter();

        if (dc.getDhcpProvider().equalsIgnoreCase(Provider.ExternalDhcpServer.getName())) {
            _dcDao.loadDetails(dc);
            String dhcpStrategy = dc.getDetail(ZoneConfig.DhcpStrategy.key());
            if ("external".equalsIgnoreCase(dhcpStrategy)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException, ConcurrentOperationException,
            InsufficientCapacityException {
        if (!canHandle(dest, offering.getTrafficType())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException,
            InsufficientCapacityException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context) {
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        return false; // assume that the agent will remove userdata etc
    }

    @Override
    public boolean destroy(Network config, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return false; // assume that the agent will remove userdata etc
    }

    @Override
    public Provider getProvider() {
        return Provider.ExternalDhcpServer;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    private static Map<Service, Map<Capability, String>> setCapabilities() {
        Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();

        capabilities.put(Service.UserData, null);

        return capabilities;
    }

    private VmDataCommand generateVmDataCommand(String vmPrivateIpAddress,
            String userData, String serviceOffering, String zoneName, String guestIpAddress, String vmName, String vmInstanceName, long vmId, String vmUuid, String publicKey) {
        VmDataCommand cmd = new VmDataCommand(vmPrivateIpAddress, vmName, _networkMgr.getExecuteInSeqNtwkElmtCmd());

        cmd.addVmData("userdata", "user-data", userData);
        cmd.addVmData("metadata", "service-offering", serviceOffering);
        cmd.addVmData("metadata", "availability-zone", zoneName);
        cmd.addVmData("metadata", "local-ipv4", guestIpAddress);
        cmd.addVmData("metadata", "local-hostname", vmName);
        cmd.addVmData("metadata", "public-ipv4", guestIpAddress);
        cmd.addVmData("metadata", "public-hostname", guestIpAddress);
        if (vmUuid == null) {
            setVmInstanceId(vmInstanceName, vmId, cmd);
        }  else {
            setVmInstanceId(vmUuid, cmd);
        }
        cmd.addVmData("metadata", "public-keys", publicKey);

        return cmd;
    }

        private void setVmInstanceId(String vmUuid, VmDataCommand cmd) {
            cmd.addVmData("metadata", "instance-id", vmUuid);
            cmd.addVmData("metadata", "vm-id", vmUuid);
        }

        private void setVmInstanceId(String vmInstanceName, long vmId, VmDataCommand cmd) {
            cmd.addVmData("metadata", "instance-id", vmInstanceName);
            cmd.addVmData("metadata", "vm-id", String.valueOf(vmId));
        }


    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return false;
    }

    @Override
    public boolean addPasswordAndUserdata(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (canHandle(dest, network.getTrafficType())) {

            if (vm.getType() != VirtualMachine.Type.User) {
                return false;
            }
            @SuppressWarnings("unchecked")
            VirtualMachineProfile<UserVm> uservm = (VirtualMachineProfile<UserVm>) vm;
            _userVmDao.loadDetails((UserVmVO) uservm.getVirtualMachine());
            String password = (String) uservm.getParameter(VirtualMachineProfile.Param.VmPassword);
            String userData = uservm.getVirtualMachine().getUserData();
            String sshPublicKey = uservm.getVirtualMachine().getDetail("SSH.PublicKey");

            Commands cmds = new Commands(OnError.Continue);
            if (password != null && nic.isDefaultNic()) {
                final String encodedPassword = PasswordGenerator.rot13(password);
                SavePasswordCommand cmd = new SavePasswordCommand(encodedPassword, nic.getIp4Address(), uservm.getVirtualMachine().getHostName(), _networkMgr.getExecuteInSeqNtwkElmtCmd());
                cmds.addCommand("password", cmd);
            }
            String serviceOffering = _serviceOfferingDao.findByIdIncludingRemoved(uservm.getServiceOfferingId()).getDisplayText();
            String zoneName = _dcDao.findById(network.getDataCenterId()).getName();

            cmds.addCommand(
                    "vmdata",
                    generateVmDataCommand(nic.getIp4Address(), userData, serviceOffering, zoneName, nic.getIp4Address(), uservm.getVirtualMachine().getHostName(),
                            uservm.getInstanceName(), uservm.getId(), uservm.getUuid(), sshPublicKey));
            try {
                _agentManager.send(dest.getHost().getId(), cmds);
            } catch (OperationTimedoutException e) {
                s_logger.debug("Unable to send vm data command to host " + dest.getHost());
                return false;
            }
            Answer dataAnswer = cmds.getAnswer("vmdata");
            if (dataAnswer != null && dataAnswer.getResult()) {
                s_logger.info("Sent vm data successfully to vm " + uservm.getVirtualMachine().getInstanceName());
                return true;
            }
            s_logger.info("Failed to send vm data to vm " + uservm.getVirtualMachine().getInstanceName());
            return false;
        }
        return false;
    }

    @Override
    public boolean savePassword(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveSSHKey(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, String SSHPublicKey) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveUserData(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean verifyServicesCombination(Set<Service> services) {
        return true;
    }

}
