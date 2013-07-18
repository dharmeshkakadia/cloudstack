/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.network.guru;

import com.midokura.midonet.client.resource.Bridge;
import com.midokura.midonet.client.resource.BridgePort;

import org.apache.dc.DataCenter;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentPlan;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InsufficientVirtualNetworkCapcityException;
import org.apache.log4j.Logger;
import org.apache.network.*;
import org.apache.network.Networks.AddressFormat;
import org.apache.network.dao.NetworkVO;
import org.apache.network.dao.PhysicalNetworkVO;
import org.apache.network.element.MidoNetElement;
import org.apache.network.guru.GuestNetworkGuru;
import org.apache.network.guru.NetworkGuru;
import org.apache.offering.NetworkOffering;
import org.apache.user.Account;
import org.apache.user.AccountVO;
import org.apache.user.dao.AccountDao;
import org.apache.utils.net.NetUtils;
import org.apache.vm.*;
import org.apache.vm.Nic.ReservationStrategy;
import org.springframework.stereotype.Component;

import javax.ejb.Local;

import java.util.UUID;

import javax.inject.Inject;

@Component
@Local(value = NetworkGuru.class)
public class MidoNetGuestNetworkGuru extends GuestNetworkGuru {
    private static final Logger s_logger = Logger.getLogger(MidoNetGuestNetworkGuru.class);

    @Inject
    AccountDao _accountDao;

    public MidoNetGuestNetworkGuru() {
        super();
        _isolationMethods = new PhysicalNetwork.IsolationMethod[] { PhysicalNetwork.IsolationMethod.MIDO };
    }

    @Override
    protected boolean canHandle(NetworkOffering offering, NetworkType networkType,
                                PhysicalNetwork physicalNetwork) {
        // This guru handles only Guest Isolated network that supports Source nat service
        if (networkType == NetworkType.Advanced
                && isMyTrafficType(offering.getTrafficType())
                && offering.getGuestType() == Network.GuestType.Isolated
                && isMyIsolationMethod(physicalNetwork)) {
            return true;
        } else {
            s_logger.trace("We only take care of Guest networks of type   " + Network.GuestType.Isolated + " in zone of type " + NetworkType.Advanced + " using isolation method MIDO.");
            return false;
        }
    }


    @Override
    public Network design(NetworkOffering offering, DeploymentPlan plan,
                          Network userSpecified, Account owner) {
        s_logger.debug("design called");
        // Check if the isolation type of the related physical network is MIDO
        PhysicalNetworkVO physnet = _physicalNetworkDao.findById(plan.getPhysicalNetworkId());
        if (physnet == null || physnet.getIsolationMethods() == null || !physnet.getIsolationMethods().contains("MIDO")) {
            s_logger.debug("Refusing to design this network, the physical isolation type is not MIDO");
            return null;
        }

        s_logger.debug("Physical isolation type is MIDO, asking GuestNetworkGuru to design this network");
        NetworkVO networkObject = (NetworkVO) super.design(offering, plan, userSpecified, owner);
        if (networkObject == null) {
            return null;
        }
        // Override the broadcast domain type - do we need to do this?
        networkObject.setBroadcastDomainType(Networks.BroadcastDomainType.Mido);

        return networkObject;
    }

    @Override
    public Network implement(Network network, NetworkOffering offering,
                             DeployDestination dest, ReservationContext context)
            throws InsufficientVirtualNetworkCapcityException {
        assert (network.getState() == Network.State.Implementing) : "Why are we implementing " + network;
        s_logger.debug("implement called network: " + network.toString());

        long dcId = dest.getDataCenter().getId();

        //get physical network id
        long physicalNetworkId = _networkModel.findPhysicalNetworkId(dcId, offering.getTags(), offering.getTrafficType());

        NetworkVO implemented = new NetworkVO(network.getTrafficType(), network.getMode(), network.getBroadcastDomainType(), network.getNetworkOfferingId(), Network.State.Allocated,
                network.getDataCenterId(), physicalNetworkId);

        if (network.getGateway() != null) {
            implemented.setGateway(network.getGateway());
        }

        if (network.getCidr() != null) {
            implemented.setCidr(network.getCidr());
        }

        AccountVO acc = _accountDao.findById(network.getAccountId());
        String accountUUIDStr = acc.getUuid();
        String routerName = "";
        if (network.getVpcId() != null) {
            routerName = "VPC" + String.valueOf(network.getVpcId());
        } else {
            routerName = String.valueOf(network.getId());
        }

        String broadcastUriStr = accountUUIDStr + "."
                                 + String.valueOf(network.getId())
                                 + ":" + routerName;

        implemented.setBroadcastUri(Networks.BroadcastDomainType.Mido.toUri(broadcastUriStr));
        s_logger.debug("Broadcast URI set to " + broadcastUriStr);

        return implemented;
    }

    @Override
    public void reserve(NicProfile nic, Network network,
                        VirtualMachineProfile<? extends VirtualMachine> vm,
                        DeployDestination dest, ReservationContext context)
            throws InsufficientVirtualNetworkCapcityException,
            InsufficientAddressCapacityException {
        s_logger.debug("reserve called with network: " + network.toString() + " nic: " + nic.toString() + " vm: " + vm.toString());

        super.reserve(nic, network, vm, dest, context);
    }

    @Override
    public boolean release(NicProfile nic,
                           VirtualMachineProfile<? extends VirtualMachine> vm,
                           String reservationId) {
        s_logger.debug("release called with nic: " + nic.toString() + " vm: " + vm.toString());
        return super.release(nic, vm, reservationId);
    }

    @Override
    public void shutdown(NetworkProfile profile, NetworkOffering offering) {
        s_logger.debug("shutdown called");

        super.shutdown(profile, offering);
    }

    @Override
    public boolean trash(Network network, NetworkOffering offering,
                         Account owner) {
        s_logger.debug("trash called with network: " + network.toString());

        return super.trash(network, offering, owner);
    }
}