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
package org.apache.vm;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.agent.api.to.NicTO;
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentPlan;
import org.apache.deploy.DeploymentPlanner;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InsufficientServerCapacityException;
import org.apache.exception.ManagementServerException;
import org.apache.exception.OperationTimedoutException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.exception.VirtualMachineMigrationException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.Network;
import org.apache.network.dao.NetworkVO;
import org.apache.offering.ServiceOffering;
import org.apache.service.ServiceOfferingVO;
import org.apache.storage.DiskOfferingVO;
import org.apache.storage.StoragePool;
import org.apache.storage.VMTemplateVO;
import org.apache.storage.VolumeVO;
import org.apache.user.Account;
import org.apache.user.User;
import org.apache.utils.Pair;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.fsm.NoTransitionException;
import org.apache.vm.NicProfile;
import org.apache.vm.NicVO;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineGuru;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.VirtualMachine.Event;
import org.apache.vm.VirtualMachine.Type;
import org.apache.vm.VirtualMachineProfile.Param;
import org.springframework.stereotype.Component;

@Component
@Local(value = VirtualMachineManager.class)
public class MockVirtualMachineManagerImpl extends ManagerBase implements VirtualMachineManager {

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> boolean stop(T vm, User caller, Account account) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean expunge(T vm, User caller, Account account) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> void registerGuru(Type type, VirtualMachineGuru<T> guru) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean stateTransitTo(VMInstanceVO vm, Event e, Long hostId) throws NoTransitionException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean advanceStop(T vm, boolean forced, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException,
    ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean advanceExpunge(T vm, User caller, Account account) throws ResourceUnavailableException, OperationTimedoutException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean remove(T vm, User caller, Account account) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> boolean destroy(T vm, User caller, Account account) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean migrateAway(Type type, long vmid, long hostId) throws InsufficientServerCapacityException, VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> T migrate(T vm, long srcHostId, DeployDestination dest) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
    VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T migrateWithStorage(T vm, long srcHostId, long destHostId,
            Map<VolumeVO, StoragePoolVO> volumeToPool) throws ResourceUnavailableException,
            ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VMInstanceVO findByIdAndType(Type type, long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isVirtualMachineUpgradable(VirtualMachine vm, ServiceOffering offering) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, Pair<? extends DiskOfferingVO, Long> rootDiskOffering,
            List<Pair<DiskOfferingVO, Long>> dataDiskOfferings, List<Pair<NetworkVO, NicProfile>> networks, Map<Param, Object> params, DeploymentPlan plan, HypervisorType hyperType, Account owner)
                    throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, Long rootSize, Pair<DiskOfferingVO, Long> dataDiskOffering,
            List<Pair<NetworkVO, NicProfile>> networks, DeploymentPlan plan, HypervisorType hyperType, Account owner) throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T allocate(T vm, VMTemplateVO template, ServiceOfferingVO serviceOffering, List<Pair<NetworkVO, NicProfile>> networkProfiles, DeploymentPlan plan,
            HypervisorType hyperType, Account owner) throws InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T start(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T start(T vm, Map<Param, Object> params, User caller, Account account, DeploymentPlan planToDeploy) throws InsufficientCapacityException,
    ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T advanceStart(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException,
    ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T advanceStart(T vm, Map<Param, Object> params, User caller, Account account, DeploymentPlan planToDeploy) throws InsufficientCapacityException,
    ResourceUnavailableException, ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T reboot(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T advanceReboot(T vm, Map<Param, Object> params, User caller, Account account) throws InsufficientCapacityException, ResourceUnavailableException,
    ConcurrentOperationException, OperationTimedoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T storageMigration(T vm,
            StoragePool storagePoolId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VMInstanceVO findById(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#checkIfCanUpgrade(org.apache.vm.VirtualMachine, long)
     */
    @Override
    public void checkIfCanUpgrade(VirtualMachine vmInstance, long newServiceOfferingId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#upgradeVmDb(long, long)
     */
    @Override
    public boolean upgradeVmDb(long vmId, long serviceOfferingId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#toNicTO(org.apache.vm.NicProfile, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public NicTO toNicTO(NicProfile nic, HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#toVmTO(org.apache.vm.VirtualMachineProfile)
     */
    @Override
    public VirtualMachineTO toVmTO(VirtualMachineProfile<? extends VMInstanceVO> profile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VMInstanceVO reConfigureVm(VMInstanceVO vm, ServiceOffering newServiceOffering, boolean sameHost) throws ResourceUnavailableException, ConcurrentOperationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VMInstanceVO findHostAndMigrate(Type vmType, VMInstanceVO vm, Long newSvcOfferingId, DeploymentPlanner.ExcludeList excludeHostList) throws InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException,
            VirtualMachineMigrationException, ManagementServerException{
        return null;
    }

    @Override
    public <T extends VMInstanceVO> T migrateForScale(T vm, long srcHostId, DeployDestination dest, Long newSvcOfferingId) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#addVmToNetwork(org.apache.vm.VirtualMachine, org.apache.network.Network, org.apache.vm.NicProfile)
     */
    @Override
    public NicProfile addVmToNetwork(VirtualMachine vm, Network network, NicProfile requested) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#removeVmFromNetwork(org.apache.vm.VirtualMachine, org.apache.network.Network, java.net.URI)
     */
    @Override
    public boolean removeNicFromVm(VirtualMachine vm, NicVO nic) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineManager#removeVmFromNetwork(org.apache.vm.VirtualMachine, org.apache.network.Network, java.net.URI)
     */
    @Override
    public boolean removeVmFromNetwork(VirtualMachine vm, Network network, URI broadcastUri) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

}
