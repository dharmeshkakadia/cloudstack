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
package org.apache.cloudstack.network.guru;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.cloudstack.network.element.SspElement;
import org.apache.cloudstack.network.element.SspManager;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.deploy.DeployDestination;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InsufficientVirtualNetworkCapcityException;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkMigrationResponder;
import org.apache.network.NetworkProfile;
import org.apache.network.PhysicalNetwork;
import org.apache.network.PhysicalNetwork.IsolationMethod;
import org.apache.network.dao.NetworkDao;
import org.apache.network.guru.GuestNetworkGuru;
import org.apache.network.guru.NetworkGuru;
import org.apache.offering.NetworkOffering;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.ReservationContextImpl;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

/**
 * Stratosphere SDN Platform NetworkGuru
 */
@Local(value=NetworkGuru.class)
public class SspGuestNetworkGuru extends GuestNetworkGuru implements NetworkMigrationResponder {
    private static final Logger s_logger = Logger.getLogger(SspGuestNetworkGuru.class);

    @Inject
    SspManager _sspMgr;
    @Inject
    NetworkDao _networkDao;
    @Inject
    NetworkOfferingServiceMapDao _ntwkOfferingSrvcDao;

    public SspGuestNetworkGuru() {
        super();
        _isolationMethods = new IsolationMethod[] { IsolationMethod.SSP };
    }

    @Override
    protected boolean canHandle(NetworkOffering offering,
            NetworkType networkType, PhysicalNetwork physicalNetwork) {
        s_logger.trace("canHandle");

        String setting = null;
        if(physicalNetwork != null && physicalNetwork.getIsolationMethods().contains("SSP")){
            // Be careful, PhysicalNetwork#getIsolationMethods() returns List<String>, not List<IsolationMethod>
            setting = "physicalnetwork setting";
        }else if(_ntwkOfferingSrvcDao.isProviderForNetworkOffering(offering.getId(), Network.Provider.getProvider(SspElement.s_SSP_NAME))){
            setting = "network offering setting";
        }
        if(setting != null){
            if (networkType != NetworkType.Advanced){
                s_logger.info("SSP enebled by "+setting+" but not active because networkType was "+networkType);
            }else if(!isMyTrafficType(offering.getTrafficType())){
                s_logger.info("SSP enabled by "+setting+" but not active because traffic type not Guest");
            }else if(offering.getGuestType() != Network.GuestType.Isolated){
                s_logger.info("SSP works for network isolatation.");
            }else if(!_sspMgr.canHandle(physicalNetwork)){
                s_logger.info("SSP manager not ready");
            }else{
                return true;
            }
        }else{
            s_logger.debug("SSP not configured to be active");
        }
        return false;
    }

    /* (non-Javadoc)
     * FYI: What is done in parent class is allocateVnet(vlan).
     * Effective return object members are: cidr, broadcastUri, gateway, mode, physicalNetworkId
     * The other members will be silently ignored.
     * This method is called at DeployVMCmd#execute (running phase) - NetworkManagerImpl#prepare
     * @see org.apache.cloudstack.network.guru.GuestNetworkGuru#implement(org.apache.network.Network, org.apache.offering.NetworkOffering, org.apache.deploy.DeployDestination, org.apache.vm.ReservationContext)
     */
    @Override
    public Network implement(Network network, NetworkOffering offering,
            DeployDestination dest, ReservationContext context)
                    throws InsufficientVirtualNetworkCapcityException {
        s_logger.trace("implement "+network.toString());
        super.implement(network, offering, dest, context);
        _sspMgr.createNetwork(network, offering, dest, context);
        return network;
    }


    @Override
    public void shutdown(NetworkProfile profile, NetworkOffering offering) {
        s_logger.trace("shutdown "+profile.toString());
        _sspMgr.deleteNetwork(profile);
        super.shutdown(profile, offering);
    }

    @Override
    public void reserve(NicProfile nic, Network network,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            DeployDestination dest, ReservationContext context)
                    throws InsufficientVirtualNetworkCapcityException,
                    InsufficientAddressCapacityException {
        super.reserve(nic, network, vm, dest, context);
        _sspMgr.createNicEnv(network, nic, dest, context);
    }

    @Override
    public boolean release(NicProfile nic,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            String reservationId) {
        Network network = _networkDao.findById(nic.getNetworkId());
        _sspMgr.deleteNicEnv(network, nic, new ReservationContextImpl(reservationId, null, null));
        return super.release(nic, vm, reservationId);
    }

    @Override
    public void updateNicProfile(NicProfile profile, Network network) {
        super.updateNicProfile(profile, network);
    }

    @Override
    public boolean prepareMigration(NicProfile nic, Network network,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            DeployDestination dest, ReservationContext context) {
        try {
            reserve(nic, network, vm, dest, context);
        } catch (InsufficientVirtualNetworkCapcityException e) {
            s_logger.error("prepareForMigration failed", e);
            return false;
        } catch (InsufficientAddressCapacityException e) {
            s_logger.error("prepareForMigration failed", e);
            return false;
        }
        return true;
    }

    @Override
    public void rollbackMigration(NicProfile nic, Network network,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            ReservationContext src, ReservationContext dst) {
        release(nic, vm, dst.getReservationId());
    }

    @Override
    public void commitMigration(NicProfile nic, Network network,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            ReservationContext src, ReservationContext dst) {
        release(nic, vm, src.getReservationId());
    }
}
