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

import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.Networks;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.Network.Capability;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.element.NetworkElement;
import org.apache.network.ovs.OvsTunnelManager;
import org.apache.offering.NetworkOffering;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

@Local(value = {NetworkElement.class})
public class OvsElement extends AdapterBase implements NetworkElement {
    @Inject
    OvsTunnelManager _ovsTunnelMgr;

    @Override
    public boolean destroy(Network network, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return null;
    }

    @Override
    public Provider getProvider() {
        return null;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering,
            DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        //Consider actually implementing the network here
    	return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        if (nic.getBroadcastType() != Networks.BroadcastDomainType.Vswitch) {
            return true;
        }

        if (nic.getTrafficType() != Networks.TrafficType.Guest) {
            return true;
        }

        _ovsTunnelMgr.VmCheckAndCreateTunnel(vm, network, dest);
        //_ovsTunnelMgr.applyDefaultFlow(vm.getVirtualMachine(), dest);

        return true;
    }

    @Override
    public boolean release(Network network, NicProfile nic,
            VirtualMachineProfile<? extends VirtualMachine> vm,
            ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        if (nic.getBroadcastType() != Networks.BroadcastDomainType.Vswitch) {
            return true;
        }

        if (nic.getTrafficType() != Networks.TrafficType.Guest) {
            return true;
        }

        _ovsTunnelMgr.CheckAndDestroyTunnel(vm.getVirtualMachine(), network);
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup)
            throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
    	return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return false;
    }

    @Override
    public boolean verifyServicesCombination(Set<Service> services) {
        return true;
    }
}
