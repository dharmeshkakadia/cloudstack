// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.network.element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.Network.Capability;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.element.NetworkElement;
import org.apache.offering.NetworkOffering;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

/**
 * @author ahuang
 *
 */
@Local(NetworkElement.class)
public class DnsNotifier extends AdapterBase implements NetworkElement {

    public DnsNotifier() {

    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        Map<Service, Map<Capability, String>> caps = new HashMap<Service, Map<Capability, String>>();
        caps.put(Service.Dns, new HashMap<Capability, String>());
        return caps;
    }

    @Override
    public Provider getProvider() {
        return null;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
    InsufficientCapacityException {
        return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException,
    ResourceUnavailableException, InsufficientCapacityException {
        // signal to the dns server that this vm is up and running and set the ip address to hostname mapping.
        vm.getHostName();
        nic.getIp4Address();
        nic.getIp6Address();
        return true;
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        vm.getHostName();
        nic.getIp4Address();
        nic.getIp6Address();
        // signal to the dns server that the vm is being shutdown and remove the mapping.
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean destroy(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return true;
    }

    @Override
    public boolean verifyServicesCombination(Set<Service> services) {
        return true;
    }

}
