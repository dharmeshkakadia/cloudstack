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
//
// Automatically generated by addcopyright.py at 01/29/2013
package org.apache.baremetal.networkservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.baremetal.manager.BaremetalManager;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.Network;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.Network.Capability;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.TrafficType;
import org.apache.network.element.IpDeployer;
import org.apache.network.element.NetworkElement;
import org.apache.network.element.UserDataServiceProvider;
import org.apache.offering.NetworkOffering;
import org.apache.uservm.UserVm;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

@Local(value = NetworkElement.class)
public class BaremetalUserdataElement extends AdapterBase implements NetworkElement, UserDataServiceProvider {
    private static Map<Service, Map<Capability, String>> capabilities;

    @Inject
    private BaremetalPxeManager pxeMgr;

    static {
        capabilities = new HashMap<Service, Map<Capability, String>>();
        capabilities.put(Service.UserData, null);
    }

    private boolean canHandle(DeployDestination dest)  {
        if (dest.getDataCenter().getNetworkType() == NetworkType.Basic && dest.getHost().getHypervisorType() == HypervisorType.BareMetal) {
            return true;
        }
        return false;
    }

    @Override
    public boolean addPasswordAndUserdata(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest,
            ReservationContext context) throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {
        if (!canHandle(dest)) {
            return false;
        }

        if (vm.getType() != VirtualMachine.Type.User) {
            return false;
        }

        return pxeMgr.addUserData(nic, (VirtualMachineProfile<UserVm>) vm);
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
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    @Override
    public Provider getProvider() {
        return BaremetalPxeManager.BAREMETAL_USERDATA_PROVIDER;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest,
            ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canEnableIndividualServices() {
        // TODO Auto-generated method stub
        return true;
    }


    @Override
    public boolean saveUserData(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean destroy(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean verifyServicesCombination(Set<Service> services) {
        return true;
    }

}