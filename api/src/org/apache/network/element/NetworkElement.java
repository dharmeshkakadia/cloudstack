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

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InsufficientNetworkCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.Network.Capability;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.offering.NetworkOffering;
import org.apache.utils.component.Adapter;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

/**
 * Represents one network element that exists in a network.
 */
public interface NetworkElement extends Adapter {

    Map<Service, Map<Capability, String>> getCapabilities();

    /**
     * NOTE:  
     * NetworkElement -> Network.Provider is a one-to-one mapping. While adding a new NetworkElement, one must add a new Provider name to Network.Provider.
     */
    Provider getProvider();

    /**
     * Implement the network configuration as specified. 
     * @param config fully specified network configuration.
     * @param offering network offering that originated the network configuration.
     * @return true if network configuration is now usable; false if not; null if not handled by this element.
     * @throws InsufficientNetworkCapacityException TODO
     */
    boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) 
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Prepare for a nic to be added into this network.
     * @param network
     * @param nic
     * @param vm
     * @param dest
     * @param context
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     * @throws InsufficientNetworkCapacityException
     */
    boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, 
            DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, 
            ResourceUnavailableException, InsufficientCapacityException;

    /**
     * A nic is released from this network.
     * @param network
     * @param nic
     * @param vm
     * @param context
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, 
            ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * The network is being shutdown.
     * @param network
     * @param context
     * @param cleanup TODO
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdown(Network network, ReservationContext context, boolean cleanup) 
            throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * The network is being destroyed.
     * @param network
     * @param context TODO
     * @return
     * @throws ConcurrentOperationException
     */
    boolean destroy(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * Check if the instances of this Element are configured to be used on the physical network referred by this provider.
     * @param provider
     * @return boolean true/false
     */
    boolean isReady(PhysicalNetworkServiceProvider provider);

    /**
     * The network service provider is being shutdown. This should shutdown all instances of this element deployed for this provider.
     * @param context
     * @param networkServiceProvider
     * @return boolean success/failure
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) 
            throws ConcurrentOperationException, ResourceUnavailableException;

    /**
     * This should return true if out of multiple services provided by this element, only some can be enabled. If all the services MUST be provided, this should return false. 
     * @return true/false
     */
    boolean canEnableIndividualServices();

    /**
     * Would return true if the service combination is supported by the provider
     * @param services
     * @return true/false
     */
    boolean verifyServicesCombination(Set<Service> services);
}