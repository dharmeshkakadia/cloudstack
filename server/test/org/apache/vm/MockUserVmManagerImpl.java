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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.agent.api.StopAnswer;
import org.apache.agent.api.VmDiskStatsEntry;
import org.apache.agent.api.VmStatsEntry;
import org.apache.agent.api.to.NicTO;
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.agent.manager.Commands;
import org.apache.api.query.vo.UserVmJoinVO;
import org.apache.cloudstack.api.BaseCmd.HTTPMethod;
import org.apache.cloudstack.api.command.admin.vm.AssignVMCmd;
import org.apache.cloudstack.api.command.admin.vm.RecoverVMCmd;
import org.apache.cloudstack.api.command.user.vm.AddNicToVMCmd;
import org.apache.cloudstack.api.command.user.vm.DeployVMCmd;
import org.apache.cloudstack.api.command.user.vm.DestroyVMCmd;
import org.apache.cloudstack.api.command.user.vm.RebootVMCmd;
import org.apache.cloudstack.api.command.user.vm.RemoveNicFromVMCmd;
import org.apache.cloudstack.api.command.user.vm.ResetVMPasswordCmd;
import org.apache.cloudstack.api.command.user.vm.ResetVMSSHKeyCmd;
import org.apache.cloudstack.api.command.user.vm.RestoreVMCmd;
import org.apache.cloudstack.api.command.user.vm.ScaleVMCmd;
import org.apache.cloudstack.api.command.user.vm.StartVMCmd;
import org.apache.cloudstack.api.command.user.vm.UpdateDefaultNicForVMCmd;
import org.apache.cloudstack.api.command.user.vm.UpdateVMCmd;
import org.apache.cloudstack.api.command.user.vm.UpgradeVMCmd;
import org.apache.cloudstack.api.command.user.vmgroup.CreateVMGroupCmd;
import org.apache.cloudstack.api.command.user.vmgroup.DeleteVMGroupCmd;
import org.apache.dc.DataCenter;
import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.ManagementServerException;
import org.apache.exception.PermissionDeniedException;
import org.apache.exception.ResourceAllocationException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.exception.StorageUnavailableException;
import org.apache.exception.VirtualMachineMigrationException;
import org.apache.host.Host;
import org.apache.hypervisor.Hypervisor;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.Network;
import org.apache.network.Network.IpAddresses;
import org.apache.offering.ServiceOffering;
import org.apache.projects.Project.ListProjectResourcesCriteria;
import org.apache.server.Criteria;
import org.apache.storage.StoragePool;
import org.apache.template.VirtualMachineTemplate;
import org.apache.user.Account;
import org.apache.uservm.UserVm;
import org.apache.utils.Pair;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.exception.ExecutionException;
import org.apache.vm.InstanceGroup;
import org.apache.vm.InstanceGroupVO;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.UserVmManager;
import org.apache.vm.UserVmService;
import org.apache.vm.UserVmVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.springframework.stereotype.Component;

@Component
@Local(value = { UserVmManager.class, UserVmService.class })
public class MockUserVmManagerImpl extends ManagerBase implements UserVmManager, UserVmService {

    @Override
    public UserVmVO findByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVmVO findById(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVmVO persist(UserVmVO vm) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean finalizeVirtualMachineProfile(VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean finalizeDeployment(Commands cmds, VirtualMachineProfile<UserVmVO> profile, DeployDestination dest, ReservationContext context) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean finalizeStart(VirtualMachineProfile<UserVmVO> profile, long hostId, Commands cmds, ReservationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean finalizeCommandsOnStart(Commands cmds, VirtualMachineProfile<UserVmVO> profile) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void finalizeStop(VirtualMachineProfile<UserVmVO> profile, StopAnswer answer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void finalizeExpunge(UserVmVO vm) {
        // TODO Auto-generated method stub

    }

    @Override
    public Long convertToId(String vmName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends UserVm> getVirtualMachines(long hostId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVmVO getVirtualMachine(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean stopVirtualMachine(long userId, long vmId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(long hostId, String hostName, List<Long> vmIds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Long, List<VmDiskStatsEntry>> getVmDiskStatistics(long hostId, String hostName, List<Long> vmIds) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteVmGroup(long groupId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addInstanceToGroup(long userVmId, String group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public InstanceGroupVO getGroupForVm(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeInstanceFromInstanceGroup(long vmId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean expunge(UserVmVO vm, long callerUserId, Account caller) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Pair<List<UserVmJoinVO>, Integer> searchForUserVMs(Criteria c, Account caller, Long domainId, boolean isRecursive, List<Long> permittedAccounts, boolean listAll, ListProjectResourcesCriteria listProjectResourcesCriteria, Map<String, String> tags) {
        // TODO Auto-generated method stub
        return null;
    }

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
    public UserVm destroyVm(DestroyVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm destroyVm(long vmId) throws ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm resetVMPassword(ResetVMPasswordCmd cmd, String password) throws ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm startVirtualMachine(StartVMCmd cmd) throws StorageUnavailableException, ExecutionException, ConcurrentOperationException, ResourceUnavailableException,
    InsufficientCapacityException, ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm rebootVirtualMachine(RebootVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm updateVirtualMachine(UpdateVMCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm addNicToVirtualMachine(AddNicToVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm removeNicFromVirtualMachine(RemoveNicFromVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm updateDefaultNicForVirtualMachine(UpdateDefaultNicForVMCmd cmd) throws InvalidParameterValueException, CloudRuntimeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm recoverVirtualMachine(RecoverVMCmd cmd) throws ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm startVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InstanceGroup createVmGroup(CreateVMGroupCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteVmGroup(DeleteVMGroupCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UserVm upgradeVirtualMachine(UpgradeVMCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm stopVirtualMachine(long vmId, boolean forced) throws ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deletePrivateTemplateRecord(Long templateId) {
        // TODO Auto-generated method stub

    }

    @Override
    public HypervisorType getHypervisorTypeOfUserVM(long vmid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm createVirtualMachine(DeployVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, StorageUnavailableException,
    ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm getUserVm(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    public UserVm createBasicSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> securityGroupIdList, Account owner,
            String hostName, String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor,
	    HTTPMethod httpmethod, String userData, String sshKeyPair, Map<Long, IpAddresses> requestedIps,
            IpAddresses defaultIp, Boolean displayVm, String keyboard, List<Long> affinityGroupIdList)
	    throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException,
            ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm createAdvancedSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList,
            List<Long> securityGroupIdList, Account owner, String hostName, String displayName, Long diskOfferingId, Long diskSize,
	    String group, HypervisorType hypervisor, HTTPMethod httpmethod, String userData,
            String sshKeyPair, Map<Long, IpAddresses> requestedIps, IpAddresses defaultIps,
            Boolean displayVm, String keyboard, List<Long> affinityGroupIdList) throws InsufficientCapacityException,
	    ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm createAdvancedVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, Account owner, String hostName,
            String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor,
	    HTTPMethod httpmethod, String userData, String sshKeyPair, Map<Long, IpAddresses> requestedIps,
	    IpAddresses defaultIps, Boolean displayVm, String keyboard, List<Long> affinityGroupIdList) throws InsufficientCapacityException,
	    ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VirtualMachine migrateVirtualMachine(Long vmId, Host destinationHost) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
    VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VirtualMachine migrateVirtualMachineWithVolume(Long vmId, Host destinationHost, Map<String, String> volumeToPool)
            throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm moveVMToUser(AssignVMCmd moveUserVMCmd)
            throws ResourceAllocationException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VirtualMachine vmStorageMigration(Long vmId, StoragePool destPool) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm restoreVM(RestoreVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException{
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm upgradeVirtualMachine(ScaleVMCmd scaleVMCmd) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> startVirtualMachine(long vmId, Long hostId, Map<VirtualMachineProfile.Param, Object> additionalParams) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean upgradeVirtualMachine(Long id, Long serviceOfferingId) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepareStop(VirtualMachineProfile<UserVmVO> profile) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineGuru#plugNic(org.apache.network.Network, org.apache.agent.api.to.NicTO, org.apache.agent.api.to.VirtualMachineTO, org.apache.vm.ReservationContext, org.apache.deploy.DeployDestination)
     */
    @Override
    public boolean plugNic(Network network, NicTO nic, VirtualMachineTO vm, ReservationContext context, DeployDestination dest) throws ConcurrentOperationException, ResourceUnavailableException,
    InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.vm.VirtualMachineGuru#unplugNic(org.apache.network.Network, org.apache.agent.api.to.NicTO, org.apache.agent.api.to.VirtualMachineTO, org.apache.vm.ReservationContext, org.apache.deploy.DeployDestination)
     */
    @Override
    public boolean unplugNic(Network network, NicTO nic, VirtualMachineTO vm, ReservationContext context, DeployDestination dest) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UserVm resetVMSSHKey(ResetVMSSHKeyCmd cmd) throws ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public boolean setupVmForPvlan(boolean add, Long hostId, NicProfile nic) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public void collectVmDiskStatistics (UserVmVO userVm) {
        // TODO Auto-generated method stub
    }
}
