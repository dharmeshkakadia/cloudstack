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
package org.apache.network.guru;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.AgentManager;
import org.apache.agent.api.CreateLogicalSwitchAnswer;
import org.apache.agent.api.CreateLogicalSwitchCommand;
import org.apache.agent.api.DeleteLogicalSwitchAnswer;
import org.apache.agent.api.DeleteLogicalSwitchCommand;
import org.apache.dc.DataCenter;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.dc.dao.DataCenterDao;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentPlan;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InsufficientVirtualNetworkCapcityException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.host.dao.HostDetailsDao;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkModel;
import org.apache.network.NetworkProfile;
import org.apache.network.NiciraNvpDeviceVO;
import org.apache.network.PhysicalNetwork;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Service;
import org.apache.network.Network.State;
import org.apache.network.Networks.BroadcastDomainType;
import org.apache.network.PhysicalNetwork.IsolationMethod;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkVO;
import org.apache.network.dao.NiciraNvpDao;
import org.apache.network.dao.PhysicalNetworkDao;
import org.apache.network.dao.PhysicalNetworkVO;
import org.apache.network.guru.GuestNetworkGuru;
import org.apache.network.guru.NetworkGuru;
import org.apache.offering.NetworkOffering;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.resource.ResourceManager;
import org.apache.user.Account;
import org.apache.user.dao.AccountDao;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

@Local(value=NetworkGuru.class)
public class NiciraNvpGuestNetworkGuru extends GuestNetworkGuru {
    private static final Logger s_logger = Logger.getLogger(NiciraNvpGuestNetworkGuru.class);
    
   
    @Inject 
    NetworkModel _networkModel;
    @Inject
    NetworkDao _networkDao;
    @Inject
    DataCenterDao _zoneDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    NiciraNvpDao _niciraNvpDao;
    @Inject
    HostDao _hostDao;
    @Inject
    ResourceManager _resourceMgr;    
    @Inject
    AgentManager _agentMgr;
    @Inject
    HostDetailsDao _hostDetailsDao;
    @Inject
    NetworkOfferingServiceMapDao _ntwkOfferingSrvcDao;
    
    public NiciraNvpGuestNetworkGuru() {
        super();
        _isolationMethods = new IsolationMethod[] { IsolationMethod.STT };
    }

    @Override
    protected boolean canHandle(NetworkOffering offering, final NetworkType networkType, final PhysicalNetwork physicalNetwork) {
        // This guru handles only Guest Isolated network that supports Source nat service
        if (networkType == NetworkType.Advanced 
                && isMyTrafficType(offering.getTrafficType()) 
                && offering.getGuestType() == Network.GuestType.Isolated
                && isMyIsolationMethod(physicalNetwork)
                && _ntwkOfferingSrvcDao.areServicesSupportedByNetworkOffering(offering.getId(), Service.Connectivity)) {
            return true;
        } else {
            s_logger.trace("We only take care of Guest networks of type   " + GuestType.Isolated + " in zone of type " + NetworkType.Advanced);
            return false;
        }
    }
    
    @Override
    public Network design(NetworkOffering offering, DeploymentPlan plan,
            Network userSpecified, Account owner) {
        // Check of the isolation type of the related physical network is STT
        PhysicalNetworkVO physnet = _physicalNetworkDao.findById(plan.getPhysicalNetworkId());
        DataCenter dc = _dcDao.findById(plan.getDataCenterId());
        if (!canHandle(offering,dc.getNetworkType(),physnet)) {
            s_logger.debug("Refusing to design this network");
            return null;
        }

        List<NiciraNvpDeviceVO> devices = _niciraNvpDao.listByPhysicalNetwork(physnet.getId());
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + physnet.getName());
            return null;
        }
        s_logger.debug("Nicira Nvp " + devices.get(0).getUuid() + " found on physical network " + physnet.getId());

        s_logger.debug("Physical isolation type is STT, asking GuestNetworkGuru to design this network");        
        NetworkVO networkObject = (NetworkVO) super.design(offering, plan, userSpecified, owner);
        if (networkObject == null) {
            return null;
        }
        // Override the broadcast domain type
        networkObject.setBroadcastDomainType(BroadcastDomainType.Lswitch);
        
        return networkObject;
    }

    @Override
    public Network implement(Network network, NetworkOffering offering,
            DeployDestination dest, ReservationContext context)
            throws InsufficientVirtualNetworkCapcityException {
        assert (network.getState() == State.Implementing) : "Why are we implementing " + network;

        long dcId = dest.getDataCenter().getId();

        //get physical network id
        Long physicalNetworkId = network.getPhysicalNetworkId();
        
        // physical network id can be null in Guest Network in Basic zone, so locate the physical network
        if (physicalNetworkId == null) {        
            physicalNetworkId = _networkModel.findPhysicalNetworkId(dcId, offering.getTags(), offering.getTrafficType());
        }

        NetworkVO implemented = new NetworkVO(network.getTrafficType(), network.getMode(), network.getBroadcastDomainType(), network.getNetworkOfferingId(), State.Allocated,
                network.getDataCenterId(), physicalNetworkId);

        if (network.getGateway() != null) {
            implemented.setGateway(network.getGateway());
        }

        if (network.getCidr() != null) {
            implemented.setCidr(network.getCidr());
        }
        
        // Name is either the given name or the uuid
        String name = network.getName();
        if (name == null || name.isEmpty()) {
            name = ((NetworkVO)network).getUuid();
        }
        if (name.length() > 40 ) {
            name = name.substring(0, 39); // max length 40
        }
        
        List<NiciraNvpDeviceVO> devices = _niciraNvpDao.listByPhysicalNetwork(physicalNetworkId);
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + physicalNetworkId);
            return null;
        }
        NiciraNvpDeviceVO niciraNvpDevice = devices.get(0);
        HostVO niciraNvpHost = _hostDao.findById(niciraNvpDevice.getHostId());
        _hostDao.loadDetails(niciraNvpHost);
        String transportzoneuuid = niciraNvpHost.getDetail("transportzoneuuid");
        String transportzoneisotype = niciraNvpHost.getDetail("transportzoneisotype");
        
        CreateLogicalSwitchCommand cmd = new CreateLogicalSwitchCommand(transportzoneuuid, transportzoneisotype, name,
                context.getDomain().getName() + "-" + context.getAccount().getAccountName());
        CreateLogicalSwitchAnswer answer = (CreateLogicalSwitchAnswer) _agentMgr.easySend(niciraNvpHost.getId(), cmd);
        
        if (answer == null || !answer.getResult()) {
            s_logger.error ("CreateLogicalSwitchCommand failed");
            return null;
        }
        
        try {
            implemented.setBroadcastUri(new URI("lswitch", answer.getLogicalSwitchUuid(), null));
            implemented.setBroadcastDomainType(BroadcastDomainType.Lswitch);
            s_logger.info("Implemented OK, network linked to  = " + implemented.getBroadcastUri().toString());
        } catch (URISyntaxException e) {
            s_logger.error("Unable to store logical switch id in broadcast uri, uuid = " + implemented.getUuid(), e);
            return null;
        }
        
        return implemented;
    }

    @Override
    public void reserve(NicProfile nic, Network network,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            DeployDestination dest, ReservationContext context)
            throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException {
        // TODO Auto-generated method stub
        super.reserve(nic, network, vm, dest, context);
    }

    @Override
    public boolean release(NicProfile nic,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            String reservationId) {
        // TODO Auto-generated method stub
        return super.release(nic, vm, reservationId);
    }

    @Override
    public void shutdown(NetworkProfile profile, NetworkOffering offering) {    
        NetworkVO networkObject = _networkDao.findById(profile.getId());
        if (networkObject.getBroadcastDomainType() != BroadcastDomainType.Lswitch ||
                networkObject.getBroadcastUri() == null) {
            s_logger.warn("BroadcastUri is empty or incorrect for guestnetwork " + networkObject.getDisplayText());
            return;
        }
        
        List<NiciraNvpDeviceVO> devices = _niciraNvpDao.listByPhysicalNetwork(networkObject.getPhysicalNetworkId());
        if (devices.isEmpty()) {
            s_logger.error("No NiciraNvp Controller on physical network " + networkObject.getPhysicalNetworkId());
            return;
        }
        NiciraNvpDeviceVO niciraNvpDevice = devices.get(0);
        HostVO niciraNvpHost = _hostDao.findById(niciraNvpDevice.getHostId());
        
        DeleteLogicalSwitchCommand cmd = new DeleteLogicalSwitchCommand(networkObject.getBroadcastUri().getSchemeSpecificPart());
        DeleteLogicalSwitchAnswer answer = (DeleteLogicalSwitchAnswer) _agentMgr.easySend(niciraNvpHost.getId(), cmd);
        
        if (answer == null || !answer.getResult()) {
            s_logger.error ("DeleteLogicalSwitchCommand failed");
        }

        super.shutdown(profile, offering);
    }

    @Override
    public boolean trash(Network network, NetworkOffering offering,
            Account owner) {
        return super.trash(network, offering, owner);
    }
    
    
    
    
    
}
