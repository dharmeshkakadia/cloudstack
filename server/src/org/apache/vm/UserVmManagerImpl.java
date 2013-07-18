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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.AgentManager.OnError;
import org.apache.agent.api.Answer;
import org.apache.agent.api.GetVmDiskStatsAnswer;
import org.apache.agent.api.GetVmDiskStatsCommand;
import org.apache.agent.api.GetVmStatsAnswer;
import org.apache.agent.api.GetVmStatsCommand;
import org.apache.agent.api.PlugNicAnswer;
import org.apache.agent.api.PlugNicCommand;
import org.apache.agent.api.PvlanSetupCommand;
import org.apache.agent.api.StartAnswer;
import org.apache.agent.api.StopAnswer;
import org.apache.agent.api.UnPlugNicAnswer;
import org.apache.agent.api.UnPlugNicCommand;
import org.apache.agent.api.VmDiskStatsEntry;
import org.apache.agent.api.VmStatsEntry;
import org.apache.agent.api.to.DiskTO;
import org.apache.agent.api.to.NicTO;
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.agent.manager.Commands;
import org.apache.alert.AlertManager;
import org.apache.api.ApiDBUtils;
import org.apache.api.query.dao.UserVmJoinDao;
import org.apache.api.query.vo.UserVmJoinVO;
import org.apache.async.AsyncJobManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.configuration.Config;
import org.apache.configuration.ConfigurationManager;
import org.apache.configuration.Resource.ResourceType;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.ha.HighAvailabilityManager;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.HypervisorCapabilitiesVO;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.hypervisor.dao.HypervisorCapabilitiesDao;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.PhysicalNetwork;
import org.apache.network.Network.IpAddresses;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.FirewallRulesDao;
import org.apache.network.dao.IPAddressDao;
import org.apache.network.dao.IPAddressVO;
import org.apache.network.dao.LoadBalancerVMMapDao;
import org.apache.network.dao.LoadBalancerVMMapVO;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkServiceMapDao;
import org.apache.network.dao.NetworkVO;
import org.apache.network.dao.PhysicalNetworkDao;
import org.apache.network.element.UserDataServiceProvider;
import org.apache.network.guru.NetworkGuru;
import org.apache.network.lb.LoadBalancingRulesManager;
import org.apache.network.rules.FirewallManager;
import org.apache.network.rules.FirewallRuleVO;
import org.apache.network.rules.PortForwardingRuleVO;
import org.apache.network.rules.RulesManager;
import org.apache.network.rules.dao.PortForwardingRulesDao;
import org.apache.network.security.SecurityGroup;
import org.apache.network.security.SecurityGroupManager;
import org.apache.network.security.dao.SecurityGroupDao;
import org.apache.network.security.dao.SecurityGroupVMMapDao;
import org.apache.network.vpc.VpcManager;
import org.apache.network.vpc.dao.VpcDao;
import org.apache.offering.NetworkOffering;
import org.apache.offering.ServiceOffering;
import org.apache.offering.NetworkOffering.Availability;
import org.apache.offerings.NetworkOfferingVO;
import org.apache.offerings.dao.NetworkOfferingDao;
import org.apache.org.Cluster;
import org.apache.org.Grouping;
import org.apache.projects.ProjectManager;
import org.apache.projects.Project.ListProjectResourcesCriteria;
import org.apache.resource.ResourceManager;
import org.apache.resource.ResourceState;
import org.apache.server.ConfigurationServer;
import org.apache.server.Criteria;
import org.apache.service.ServiceOfferingVO;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.storage.DiskOfferingVO;
import org.apache.storage.GuestOSCategoryVO;
import org.apache.storage.GuestOSVO;
import org.apache.storage.SnapshotVO;
import org.apache.storage.Storage;
import org.apache.storage.StorageManager;
import org.apache.storage.StoragePool;
import org.apache.storage.StoragePoolStatus;
import org.apache.storage.VMTemplateVO;
import org.apache.storage.VMTemplateZoneVO;
import org.apache.storage.Volume;
import org.apache.storage.VolumeManager;
import org.apache.storage.VolumeVO;
import org.apache.storage.Storage.ImageFormat;
import org.apache.storage.Storage.TemplateType;
import org.apache.storage.dao.DiskOfferingDao;
import org.apache.storage.dao.GuestOSCategoryDao;
import org.apache.storage.dao.GuestOSDao;
import org.apache.storage.dao.SnapshotDao;
import org.apache.storage.dao.VMTemplateDao;
import org.apache.storage.dao.VMTemplateDetailsDao;
import org.apache.storage.dao.VMTemplateZoneDao;
import org.apache.storage.dao.VolumeDao;
import org.apache.storage.snapshot.SnapshotManager;
import org.apache.tags.dao.ResourceTagDao;
import org.apache.template.TemplateManager;
import org.apache.template.VirtualMachineTemplate;
import org.apache.template.VirtualMachineTemplate.BootloaderType;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.AccountService;
import org.apache.user.AccountVO;
import org.apache.user.ResourceLimitService;
import org.apache.user.SSHKeyPair;
import org.apache.user.SSHKeyPairVO;
import org.apache.user.User;
import org.apache.user.UserContext;
import org.apache.user.UserVO;
import org.apache.user.VmDiskStatisticsVO;
import org.apache.user.dao.AccountDao;
import org.apache.user.dao.SSHKeyPairDao;
import org.apache.user.dao.UserDao;
import org.apache.user.dao.VmDiskStatisticsDao;
import org.apache.uservm.UserVm;
import org.apache.utils.Journal;
import org.apache.utils.NumbersUtil;
import org.apache.utils.Pair;
import org.apache.utils.PasswordGenerator;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.concurrency.NamedThreadFactory;
import org.apache.utils.crypt.RSAHelper;
import org.apache.utils.db.DB;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GlobalLock;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.apache.utils.db.SearchCriteria.Func;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.exception.ExecutionException;
import org.apache.utils.fsm.NoTransitionException;
import org.apache.utils.net.NetUtils;
import org.apache.vm.InstanceGroupVMMapVO;
import org.apache.vm.InstanceGroupVO;
import org.apache.vm.ItWorkDao;
import org.apache.vm.Nic;
import org.apache.vm.NicProfile;
import org.apache.vm.NicVO;
import org.apache.vm.ReservationContext;
import org.apache.vm.UserVmCloneSettingVO;
import org.apache.vm.UserVmDetailVO;
import org.apache.vm.UserVmService;
import org.apache.vm.UserVmVO;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineName;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.VmDetailConstants;
import org.apache.vm.VirtualMachine.State;
import org.apache.vm.dao.InstanceGroupDao;
import org.apache.vm.dao.InstanceGroupVMMapDao;
import org.apache.vm.dao.NicDao;
import org.apache.vm.dao.SecondaryStorageVmDao;
import org.apache.vm.dao.UserVmCloneSettingDao;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.UserVmDetailsDao;
import org.apache.vm.dao.VMInstanceDao;
import org.apache.vm.snapshot.VMSnapshot;
import org.apache.vm.snapshot.VMSnapshotManager;
import org.apache.vm.snapshot.VMSnapshotVO;
import org.apache.vm.snapshot.dao.VMSnapshotDao;
import org.apache.capacity.CapacityManager;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.affinity.AffinityGroupVO;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
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
import org.apache.cloudstack.engine.cloud.entity.api.VirtualMachineEntity;
import org.apache.cloudstack.engine.service.api.OrchestrationService;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.dc.DataCenter;
import org.apache.dc.DataCenterVO;
import org.apache.dc.DedicatedResourceVO;
import org.apache.dc.HostPodVO;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.dc.dao.ClusterDao;
import org.apache.dc.dao.DataCenterDao;
import org.apache.dc.dao.DedicatedResourceDao;
import org.apache.dc.dao.HostPodDao;
import org.apache.deploy.DataCenterDeployment;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.domain.DomainVO;
import org.apache.domain.dao.DomainDao;
import org.apache.event.ActionEvent;
import org.apache.event.EventTypes;
import org.apache.event.UsageEventUtils;
import org.apache.event.dao.UsageEventDao;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.CloudException;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.ManagementServerException;
import org.apache.exception.OperationTimedoutException;
import org.apache.exception.PermissionDeniedException;
import org.apache.exception.ResourceAllocationException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.exception.StorageUnavailableException;
import org.apache.exception.VirtualMachineMigrationException;

@Local(value = { UserVmManager.class, UserVmService.class })
public class UserVmManagerImpl extends ManagerBase implements UserVmManager, UserVmService {
    private static final Logger s_logger = Logger
            .getLogger(UserVmManagerImpl.class);

    private static final int ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION = 3; // 3
    // seconds

    public enum UserVmCloneType {
        full,
        linked
    }

    @Inject
    protected HostDao _hostDao = null;
    @Inject
    protected ServiceOfferingDao _offeringDao = null;
    @Inject
    protected DiskOfferingDao _diskOfferingDao = null;
    @Inject
    protected VMTemplateDao _templateDao = null;
    @Inject
    protected VMTemplateDetailsDao _templateDetailsDao = null;
    @Inject
    protected VMTemplateZoneDao _templateZoneDao = null;
    @Inject
    protected DomainDao _domainDao = null;
    @Inject
    protected UserVmCloneSettingDao _vmCloneSettingDao = null;
    @Inject
    protected UserVmDao _vmDao = null;
    @Inject
    protected UserVmJoinDao _vmJoinDao = null;
    @Inject
    protected VolumeDao _volsDao = null;
    @Inject
    protected DataCenterDao _dcDao = null;
    @Inject
    protected FirewallRulesDao _rulesDao = null;
    @Inject
    protected LoadBalancerVMMapDao _loadBalancerVMMapDao = null;
    @Inject
    protected PortForwardingRulesDao _portForwardingDao;
    @Inject
    protected IPAddressDao _ipAddressDao = null;
    @Inject
    protected HostPodDao _podDao = null;
    @Inject
    protected NetworkModel _networkModel = null;
    @Inject
    protected NetworkManager _networkMgr = null;
    @Inject
    protected StorageManager _storageMgr = null;
    @Inject
    protected SnapshotManager _snapshotMgr = null;
    @Inject
    protected AgentManager _agentMgr = null;
    @Inject
    protected ConfigurationManager _configMgr = null;
    @Inject
    protected AccountDao _accountDao = null;
    @Inject
    protected UserDao _userDao = null;
    @Inject
    protected SnapshotDao _snapshotDao = null;
    @Inject
    protected GuestOSDao _guestOSDao = null;
    @Inject
    protected HighAvailabilityManager _haMgr = null;
    @Inject
    protected AlertManager _alertMgr = null;
    @Inject
    protected AccountManager _accountMgr;
    @Inject
    protected AccountService _accountService;
    @Inject
    protected AsyncJobManager _asyncMgr;
    @Inject
    protected ClusterDao _clusterDao;
    @Inject
    protected PrimaryDataStoreDao _storagePoolDao;
    @Inject
    protected SecurityGroupManager _securityGroupMgr;
    @Inject
    protected ServiceOfferingDao _serviceOfferingDao;
    @Inject
    protected NetworkOfferingDao _networkOfferingDao;
    @Inject
    protected InstanceGroupDao _vmGroupDao;
    @Inject
    protected InstanceGroupVMMapDao _groupVMMapDao;
    @Inject
    protected VirtualMachineManager _itMgr;
    @Inject
    protected NetworkDao _networkDao;
    @Inject
    protected NicDao _nicDao;
    @Inject
    protected VpcDao _vpcDao;
    @Inject
    protected RulesManager _rulesMgr;
    @Inject
    protected LoadBalancingRulesManager _lbMgr;
    @Inject
    protected SSHKeyPairDao _sshKeyPairDao;
    @Inject
    protected UserVmDetailsDao _vmDetailsDao;
    @Inject
    protected HypervisorCapabilitiesDao _hypervisorCapabilitiesDao;
    @Inject
    protected SecurityGroupDao _securityGroupDao;
    @Inject
    protected CapacityManager _capacityMgr;
    @Inject
    protected VMInstanceDao _vmInstanceDao;
    @Inject
    protected ResourceLimitService _resourceLimitMgr;
    @Inject
    protected FirewallManager _firewallMgr;
    @Inject
    protected ProjectManager _projectMgr;
    @Inject
    protected ResourceManager _resourceMgr;

    @Inject
    protected NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    SecurityGroupVMMapDao _securityGroupVMMapDao;
    @Inject
    protected ItWorkDao _workDao;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    VpcManager _vpcMgr;
    @Inject
    TemplateManager templateMgr;
    @Inject
    protected GuestOSCategoryDao _guestOSCategoryDao;
    @Inject
    UsageEventDao _usageEventDao;

    @Inject
    SecondaryStorageVmDao _secondaryDao;
    @Inject
    VmDiskStatisticsDao _vmDiskStatsDao;

    @Inject
    protected VMSnapshotDao _vmSnapshotDao;
    @Inject
    protected VMSnapshotManager _vmSnapshotMgr;

    @Inject
    AffinityGroupVMMapDao _affinityGroupVMMapDao;
    @Inject
    AffinityGroupDao _affinityGroupDao;
    @Inject
    TemplateDataFactory templateFactory;
    @Inject
    DedicatedResourceDao _dedicatedDao;
    @Inject
    ConfigurationServer _configServer;

    protected ScheduledExecutorService _executor = null;
    protected int _expungeInterval;
    protected int _expungeDelay;
    protected boolean _dailyOrHourly = false;

    protected String _name;
    protected String _instance;
    protected String _zone;
    protected boolean _instanceNameFlag;
    protected int _scaleRetry;

    @Inject ConfigurationDao _configDao;
    private int _createprivatetemplatefromvolumewait;
    private int _createprivatetemplatefromsnapshotwait;
    private final int MAX_VM_NAME_LEN = 80;
    private final int MAX_HTTP_GET_LENGTH = 2 * MAX_USER_DATA_LENGTH_BYTES;
    private final int MAX_HTTP_POST_LENGTH = 16 * MAX_USER_DATA_LENGTH_BYTES;

    @Inject
    protected OrchestrationService _orchSrvc;

    @Inject VolumeManager volumeMgr;

    @Override
    public UserVmVO getVirtualMachine(long vmId) {
        return _vmDao.findById(vmId);
    }

    @Override
    public List<? extends UserVm> getVirtualMachines(long hostId) {
        return _vmDao.listByHostId(hostId);
    }

    protected void resourceLimitCheck (Account owner, Long cpu, Long memory) throws ResourceAllocationException {
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.user_vm);
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.cpu, cpu);
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.memory, memory);
    }

    protected void resourceCountIncrement (long accountId, Long cpu, Long memory) {
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.user_vm);
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.cpu, cpu);
        _resourceLimitMgr.incrementResourceCount(accountId, ResourceType.memory, memory);
    }

    protected void resourceCountDecrement (long accountId, Long cpu, Long memory) {
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.user_vm);
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.cpu, cpu);
        _resourceLimitMgr.decrementResourceCount(accountId, ResourceType.memory, memory);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_RESETPASSWORD, eventDescription = "resetting Vm password", async = true)
    public UserVm resetVMPassword(ResetVMPasswordCmd cmd, String password)
            throws ResourceUnavailableException, InsufficientCapacityException {
        Account caller = UserContext.current().getCaller();
        Long vmId = cmd.getId();
        UserVmVO userVm = _vmDao.findById(cmd.getId());
        _vmDao.loadDetails(userVm);

        // Do parameters input validation
        if (userVm == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + cmd.getId());
        }

        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(userVm
                .getTemplateId());
        if (template == null || !template.getEnablePassword()) {
            throw new InvalidParameterValueException(
                    "Fail to reset password for the virtual machine, the template is not password enabled");
        }

        if (userVm.getState() == State.Error
                || userVm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm with id " + vmId
                    + " is not in the right state");
        }

        _accountMgr.checkAccess(caller, null, true, userVm);

        boolean result = resetVMPasswordInternal(vmId, password);

        if (result) {
            userVm.setPassword(password);
            // update the password in vm_details table too
            // Check if an SSH key pair was selected for the instance and if so
            // use it to encrypt & save the vm password
            encryptAndStorePassword(userVm, password);
        } else {
            throw new CloudRuntimeException(
                    "Failed to reset password for the virtual machine ");
        }

        return userVm;
    }

    private boolean resetVMPasswordInternal(Long vmId,
            String password) throws ResourceUnavailableException,
            InsufficientCapacityException {
        Long userId = UserContext.current().getCallerUserId();
        VMInstanceVO vmInstance = _vmDao.findById(vmId);

        if (password == null || password.equals("")) {
            return false;
        }

        VMTemplateVO template = _templateDao
                .findByIdIncludingRemoved(vmInstance.getTemplateId());
        if (template.getEnablePassword()) {
            Nic defaultNic = _networkModel.getDefaultNic(vmId);
            if (defaultNic == null) {
                s_logger.error("Unable to reset password for vm " + vmInstance
                        + " as the instance doesn't have default nic");
                return false;
            }

            Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
            NicProfile defaultNicProfile = new NicProfile(defaultNic, defaultNetwork, null, null, null, _networkModel.isSecurityGroupSupportedInNetwork(defaultNetwork), _networkModel.getNetworkTag(template.getHypervisorType(), defaultNetwork));
            VirtualMachineProfile<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(vmInstance);
            vmProfile.setParameter(VirtualMachineProfile.Param.VmPassword, password);

            UserDataServiceProvider element = _networkMgr.getPasswordResetProvider(defaultNetwork);
            if (element == null) {
                throw new CloudRuntimeException(
                        "Can't find network element for "
                                + Service.UserData.getName()
                                + " provider needed for password reset");
            }

            boolean result = element.savePassword(defaultNetwork,
                    defaultNicProfile, vmProfile);

            // Need to reboot the virtual machine so that the password gets
            // redownloaded from the DomR, and reset on the VM
            if (!result) {
                s_logger.debug("Failed to reset password for the virutal machine; no need to reboot the vm");
                return false;
            } else {
                if (vmInstance.getState() == State.Stopped) {
                    s_logger.debug("Vm "
                            + vmInstance
                            + " is stopped, not rebooting it as a part of password reset");
                    return true;
                }

                if (rebootVirtualMachine(userId, vmId) == null) {
                    s_logger.warn("Failed to reboot the vm " + vmInstance);
                    return false;
                } else {
                    s_logger.debug("Vm "
                            + vmInstance
                            + " is rebooted successfully as a part of password reset");
                    return true;
                }
            }
        } else {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Reset password called for a vm that is not using a password enabled template");
            }
            return false;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_RESETSSHKEY, eventDescription = "resetting Vm SSHKey", async = true)
    public UserVm resetVMSSHKey(ResetVMSSHKeyCmd cmd)
            throws ResourceUnavailableException, InsufficientCapacityException {

        Account caller = UserContext.current().getCaller();
        Account owner = _accountMgr.finalizeOwner(caller, cmd.getAccountName(), cmd.getDomainId(), cmd.getProjectId());
        Long vmId = cmd.getId();

        UserVmVO userVm = _vmDao.findById(cmd.getId());
        _vmDao.loadDetails(userVm);
        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(userVm.getTemplateId());

        // Do parameters input validation

        if (userVm == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine by id" + cmd.getId());
        }

        if (userVm.getState() == State.Error || userVm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm with specified id is not in the right state");
        }
        if (userVm.getState() != State.Stopped) {
            s_logger.error("vm is not in the right state: " + vmId);
            throw new InvalidParameterValueException("Vm " + userVm + " should be stopped to do SSH Key reset");
        }

        SSHKeyPairVO s = _sshKeyPairDao.findByName(owner.getAccountId(), owner.getDomainId(), cmd.getName());
        if (s == null) {
            throw new InvalidParameterValueException("A key pair with name '" + cmd.getName() + "' does not exist for account " + owner.getAccountName() + " in specified domain id");
        }

        _accountMgr.checkAccess(caller, null, true, userVm);
        String password = null;
        String sshPublicKey = s.getPublicKey();
        if (template != null && template.getEnablePassword()) {
            password = generateRandomPassword();
        }

        boolean result = resetVMSSHKeyInternal(vmId, sshPublicKey, password);

        if (result) {
            userVm.setDetail("SSH.PublicKey", sshPublicKey);
            if (template != null && template.getEnablePassword()) {
                userVm.setPassword(password);
                //update the encrypted password in vm_details table too
                encryptAndStorePassword(userVm, password);
            }
            _vmDao.saveDetails(userVm);
        } else {
            throw new CloudRuntimeException("Failed to reset SSH Key for the virtual machine ");
        }
        return userVm;
    }

    private boolean resetVMSSHKeyInternal(Long vmId, String SSHPublicKey, String password) throws ResourceUnavailableException, InsufficientCapacityException {
        Long userId = UserContext.current().getCallerUserId();
        VMInstanceVO vmInstance = _vmDao.findById(vmId);

        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vmInstance.getTemplateId());
        Nic defaultNic = _networkModel.getDefaultNic(vmId);
        if (defaultNic == null) {
            s_logger.error("Unable to reset SSH Key for vm " + vmInstance + " as the instance doesn't have default nic");
            return false;
        }

        Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
        NicProfile defaultNicProfile = new NicProfile(defaultNic, defaultNetwork, null, null, null,
                _networkModel.isSecurityGroupSupportedInNetwork(defaultNetwork),
                _networkModel.getNetworkTag(template.getHypervisorType(), defaultNetwork));

        VirtualMachineProfile<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(vmInstance);

        if (template != null && template.getEnablePassword()) {
            vmProfile.setParameter(VirtualMachineProfile.Param.VmPassword, password);
        }

        UserDataServiceProvider element = _networkMgr.getSSHKeyResetProvider(defaultNetwork);
        if (element == null) {
            throw new CloudRuntimeException("Can't find network element for " + Service.UserData.getName() + " provider needed for SSH Key reset");
        }
        boolean result = element.saveSSHKey(defaultNetwork, defaultNicProfile, vmProfile, SSHPublicKey);

        // Need to reboot the virtual machine so that the password gets redownloaded from the DomR, and reset on the VM
        if (!result) {
            s_logger.debug("Failed to reset SSH Key for the virutal machine; no need to reboot the vm");
            return false;
        } else {
            if (vmInstance.getState() == State.Stopped) {
                s_logger.debug("Vm " + vmInstance + " is stopped, not rebooting it as a part of SSH Key reset");
                return true;
            }
            if (rebootVirtualMachine(userId, vmId) == null) {
                s_logger.warn("Failed to reboot the vm " + vmInstance);
                return false;
            } else {
                s_logger.debug("Vm " + vmInstance + " is rebooted successfully as a part of SSH Key reset");
                return true;
            }
        }
    }


    @Override
    public boolean stopVirtualMachine(long userId, long vmId) {
        boolean status = false;
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Stopping vm=" + vmId);
        }
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null || vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is either removed or deleted.");
            }
            return true;
        }

        User user = _userDao.findById(userId);
        Account account = _accountDao.findById(user.getAccountId());

        try {
            VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());
            status = vmEntity.stop(new Long(userId).toString());
        } catch (ResourceUnavailableException e) {
            s_logger.debug("Unable to stop due to ", e);
            status = false;
        } catch (CloudException e) {
            throw new CloudRuntimeException(
                    "Unable to contact the agent to stop the virtual machine "
                            + vm, e);
        }

        if (status) {
            return status;
        } else {
            return status;
        }
    }

    private UserVm rebootVirtualMachine(long userId, long vmId)
            throws InsufficientCapacityException, ResourceUnavailableException {
        UserVmVO vm = _vmDao.findById(vmId);
        User caller = _accountMgr.getActiveUser(userId);
        Account owner = _accountMgr.getAccount(vm.getAccountId());

        if (vm == null || vm.getState() == State.Destroyed
                || vm.getState() == State.Expunging || vm.getRemoved() != null) {
            s_logger.warn("Vm id=" + vmId + " doesn't exist");
            return null;
        }

        if (vm.getState() == State.Running && vm.getHostId() != null) {
            collectVmDiskStatistics(vm);
            return _itMgr.reboot(vm, null, caller, owner);
        } else {
            s_logger.error("Vm id=" + vmId
                    + " is not in Running state, failed to reboot");
            return null;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_UPGRADE, eventDescription = "upgrading Vm")
    /*
     * TODO: cleanup eventually - Refactored API call
     */
    public UserVm upgradeVirtualMachine(UpgradeVMCmd cmd) throws ResourceAllocationException {
        Long vmId = cmd.getId();
        Long svcOffId = cmd.getServiceOfferingId();
        return upgradeStoppedVirtualMachine(vmId, svcOffId);
    }


    private UserVm upgradeStoppedVirtualMachine(Long vmId, Long svcOffId) throws ResourceAllocationException {
        Account caller = UserContext.current().getCaller();

        // Verify input parameters
        UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Check resource limits for CPU and Memory.
        ServiceOfferingVO newServiceOffering = _offeringDao.findById(svcOffId);
        ServiceOfferingVO currentServiceOffering = _offeringDao.findByIdIncludingRemoved(vmInstance.getServiceOfferingId());

        int newCpu = newServiceOffering.getCpu();
        int newMemory = newServiceOffering.getRamSize();
        int currentCpu = currentServiceOffering.getCpu();
        int currentMemory = currentServiceOffering.getRamSize();

        if (newCpu > currentCpu) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.cpu,
                    newCpu - currentCpu);
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.memory,
                    newMemory - currentMemory);
        }

        // Check that the specified service offering ID is valid
        _itMgr.checkIfCanUpgrade(vmInstance, svcOffId);

        // remove diskAndMemory VM snapshots
        List<VMSnapshotVO> vmSnapshots = _vmSnapshotDao.findByVm(vmId);
        for (VMSnapshotVO vmSnapshotVO : vmSnapshots) {
            if(vmSnapshotVO.getType() == VMSnapshot.Type.DiskAndMemory){
                if(!_vmSnapshotMgr.deleteAllVMSnapshots(vmId, VMSnapshot.Type.DiskAndMemory)){
                    String errMsg = "Failed to remove VM snapshot during upgrading, snapshot id " + vmSnapshotVO.getId();
                    s_logger.debug(errMsg);
                    throw new CloudRuntimeException(errMsg);
                }

            }
        }

        _itMgr.upgradeVmDb(vmId, svcOffId);

        // Increment or decrement CPU and Memory count accordingly.
        if (newCpu > currentCpu) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long (newCpu - currentCpu));
        } else if (currentCpu > newCpu) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long (currentCpu - newCpu));
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long (newMemory - currentMemory));
        } else if (currentMemory > newMemory) {
            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long (currentMemory - newMemory));
        }

        return _vmDao.findById(vmInstance.getId());

    }


    @Override
    public UserVm addNicToVirtualMachine(AddNicToVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        Long vmId = cmd.getVmId();
        Long networkId = cmd.getNetworkId();
        String ipAddress = cmd.getIpAddress();
        Account caller = UserContext.current().getCaller();

        UserVmVO vmInstance = _vmDao.findById(vmId);
        if(vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }
        NetworkVO network = _networkDao.findById(networkId);
        if(network == null) {
            throw new InvalidParameterValueException("unable to find a network with id " + networkId);
        }
        List<NicVO> allNics = _nicDao.listByVmId(vmInstance.getId());
        for(NicVO nic : allNics){
            if(nic.getNetworkId() == network.getId())
                throw new CloudRuntimeException("A NIC already exists for VM:" + vmInstance.getInstanceName() + " in network: " + network.getUuid());
        }

        NicProfile profile = new NicProfile(null, null);
        if(ipAddress != null) {
            profile = new NicProfile(ipAddress, null);
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        DataCenterVO dc = _dcDao.findById(vmInstance.getDataCenterId());
        if (dc.getNetworkType() == DataCenter.NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't add a new NIC to a VM on a Basic Network");
        }

        // Perform account permission check on network
        if (network.getGuestType() != Network.GuestType.Shared) {
            // Check account permissions
            List<NetworkVO> networkMap = _networkDao.listBy(caller.getId(), network.getId());
            if ((networkMap == null || networkMap.isEmpty() ) && caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                throw new PermissionDeniedException("Unable to modify a vm using network with id " + network.getId() + ", permission denied");
            }
        }

        //ensure network belongs in zone
        if (network.getDataCenterId() != vmInstance.getDataCenterId()) {
            throw new CloudRuntimeException(vmInstance + " is in zone:" + vmInstance.getDataCenterId() + " but " + network + " is in zone:" + network.getDataCenterId());
        }

        if(_networkModel.getNicInNetwork(vmInstance.getId(),network.getId()) != null){
            s_logger.debug(vmInstance + " already in " + network + " going to add another NIC");
        } else {
            //* get all vms hostNames in the network
            List<String> hostNames = _vmInstanceDao.listDistinctHostNames(network.getId());
            //* verify that there are no duplicates
            if (hostNames.contains(vmInstance.getHostName())) {
                throw new CloudRuntimeException(network + " already has a vm with host name: '" + vmInstance.getHostName());
            }
        }

        NicProfile guestNic = null;

        try {
            guestNic = _itMgr.addVmToNetwork(vmInstance, network, profile);
        } catch (ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to add NIC to " + vmInstance + ": " + e);
        } catch (InsufficientCapacityException e) {
            throw new CloudRuntimeException("Insufficient capacity when adding NIC to " + vmInstance + ": " + e);
        } catch (ConcurrentOperationException e) {
            throw new CloudRuntimeException("Concurrent operations on adding NIC to " + vmInstance + ": " +e);
        }
        if (guestNic == null) {
            throw new CloudRuntimeException("Unable to add NIC to " + vmInstance);
        }

        s_logger.debug("Successful addition of " + network + " from " + vmInstance);
        return _vmDao.findById(vmInstance.getId());
    }

    @Override
    public UserVm removeNicFromVirtualMachine(RemoveNicFromVMCmd cmd) throws InvalidParameterValueException, PermissionDeniedException, CloudRuntimeException {
        Long vmId = cmd.getVmId();
        Long nicId = cmd.getNicId();
        Account caller = UserContext.current().getCaller();

        UserVmVO vmInstance = _vmDao.findById(vmId);
        if(vmInstance == null) {
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }
        NicVO nic = _nicDao.findById(nicId);
        if (nic == null){
            throw new InvalidParameterValueException("unable to find a nic with id " + nicId);
        }
        NetworkVO network = _networkDao.findById(nic.getNetworkId());
        if(network == null) {
            throw new InvalidParameterValueException("unable to find a network with id " + nic.getNetworkId());
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        DataCenterVO dc = _dcDao.findById(vmInstance.getDataCenterId());
        if (dc.getNetworkType() == DataCenter.NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't remove a NIC from a VM on a Basic Network");
        }

        //check to see if nic is attached to VM
        if (nic.getInstanceId() != vmId) {
            throw new InvalidParameterValueException(nic + " is not a nic on  " + vmInstance);
        }

        // Perform account permission check on network
        if (network.getGuestType() != Network.GuestType.Shared) {
            // Check account permissions
            List<NetworkVO> networkMap = _networkDao.listBy(caller.getId(), network.getId());
            if ((networkMap == null || networkMap.isEmpty() ) && caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                throw new PermissionDeniedException("Unable to modify a vm using network with id " + network.getId() + ", permission denied");
            }
        }

        boolean nicremoved = false;

        try {
            nicremoved = _itMgr.removeNicFromVm(vmInstance, nic);
        } catch (ResourceUnavailableException e) {
            throw new CloudRuntimeException("Unable to remove " + network + " from " + vmInstance +": " + e);

        } catch (ConcurrentOperationException e) {
            throw new CloudRuntimeException("Concurrent operations on removing " + network + " from " + vmInstance + ": " + e);
        }

        if (!nicremoved) {
            throw new CloudRuntimeException("Unable to remove " + network +  " from " + vmInstance );
        }

        s_logger.debug("Successful removal of " + network + " from " + vmInstance);
        return _vmDao.findById(vmInstance.getId());


    }

    @Override
    public UserVm updateDefaultNicForVirtualMachine(UpdateDefaultNicForVMCmd cmd) throws InvalidParameterValueException, CloudRuntimeException {
        Long vmId = cmd.getVmId();
        Long nicId = cmd.getNicId();
        Account caller = UserContext.current().getCaller();

        UserVmVO vmInstance = _vmDao.findById(vmId);
        if (vmInstance == null){
            throw new InvalidParameterValueException("unable to find a virtual machine with id " + vmId);
        }
        NicVO nic = _nicDao.findById(nicId);
        if (nic == null){
            throw new InvalidParameterValueException("unable to find a nic with id " + nicId);
        }
        NetworkVO network = _networkDao.findById(nic.getNetworkId());
        if (network == null){
            throw new InvalidParameterValueException("unable to find a network with id " + nic.getNetworkId());
        }

        // Perform permission check on VM
        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Verify that zone is not Basic
        DataCenterVO dc = _dcDao.findById(vmInstance.getDataCenterId());
        if (dc.getNetworkType() == DataCenter.NetworkType.Basic) {
            throw new CloudRuntimeException("Zone " + vmInstance.getDataCenterId() + ", has a NetworkType of Basic. Can't change default NIC on a Basic Network");
        }

        // no need to check permissions for network, we'll enumerate the ones they already have access to
        Network existingdefaultnet = _networkModel.getDefaultNetworkForVm(vmId);

        //check to see if nic is attached to VM
        if (nic.getInstanceId() != vmId) {
            throw new InvalidParameterValueException(nic + " is not a nic on  " + vmInstance);
        }
        // if current default equals chosen new default, Throw an exception
        if (nic.isDefaultNic()){
            throw new CloudRuntimeException("refusing to set default nic because chosen nic is already the default");
        }

        //make sure the VM is Running or Stopped
        if ((vmInstance.getState() != State.Running) && (vmInstance.getState() != State.Stopped)) {
            throw new CloudRuntimeException("refusing to set default " + vmInstance + " is not Running or Stopped");
        }

        NicProfile existing = null;
        List<NicProfile> nicProfiles = _networkMgr.getNicProfiles(vmInstance);
        for (NicProfile nicProfile : nicProfiles) {
            if(nicProfile.isDefaultNic() && nicProfile.getNetworkId() == existingdefaultnet.getId()){
                existing = nicProfile;
                continue;
            }
        }

        if (existing == null){
            s_logger.warn("Failed to update default nic, no nic profile found for existing default network");
            throw new CloudRuntimeException("Failed to find a nic profile for the existing default network. This is bad and probably means some sort of configuration corruption");
        }

        Network oldDefaultNetwork = null;
        oldDefaultNetwork = _networkModel.getDefaultNetworkForVm(vmId);
        String oldNicIdString = Long.toString(_networkModel.getDefaultNic(vmId).getId());
        long oldNetworkOfferingId = -1L;

        if(oldDefaultNetwork!=null) {
            oldNetworkOfferingId = oldDefaultNetwork.getNetworkOfferingId();
        }
        NicVO existingVO = _nicDao.findById(existing.id);
        Integer chosenID = nic.getDeviceId();
        Integer existingID = existing.getDeviceId();

        nic.setDefaultNic(true);
        nic.setDeviceId(existingID);
        existingVO.setDefaultNic(false);
        existingVO.setDeviceId(chosenID);

        nic = _nicDao.persist(nic);
        existingVO = _nicDao.persist(existingVO);

        Network newdefault = null;
        newdefault = _networkModel.getDefaultNetworkForVm(vmId);

        if (newdefault == null){
            nic.setDefaultNic(false);
            nic.setDeviceId(chosenID);
            existingVO.setDefaultNic(true);
            existingVO.setDeviceId(existingID);

            nic = _nicDao.persist(nic);
            existingVO = _nicDao.persist(existingVO);

            newdefault = _networkModel.getDefaultNetworkForVm(vmId);
            if (newdefault.getId() == existingdefaultnet.getId()) {
                throw new CloudRuntimeException("Setting a default nic failed, and we had no default nic, but we were able to set it back to the original");
            }
            throw new CloudRuntimeException("Failed to change default nic to " + nic + " and now we have no default");
        } else if (newdefault.getId() == nic.getNetworkId()) {
            s_logger.debug("successfully set default network to " + network + " for " + vmInstance);
            String nicIdString = Long.toString(nic.getId());
            long newNetworkOfferingId = network.getNetworkOfferingId();
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_REMOVE, vmInstance.getAccountId(), vmInstance.getDataCenterId(),
                    vmInstance.getId(), oldNicIdString, oldNetworkOfferingId, null, 1L, VirtualMachine.class.getName(), vmInstance.getUuid());
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_ASSIGN, vmInstance.getAccountId(), vmInstance.getDataCenterId(),
                     vmInstance.getId(), nicIdString, newNetworkOfferingId, null, 1L, VirtualMachine.class.getName(), vmInstance.getUuid());
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_REMOVE, vmInstance.getAccountId(), vmInstance.getDataCenterId(),
                    vmInstance.getId(), nicIdString, newNetworkOfferingId, null, 0L, VirtualMachine.class.getName(), vmInstance.getUuid());
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_ASSIGN, vmInstance.getAccountId(), vmInstance.getDataCenterId(),
                     vmInstance.getId(), oldNicIdString, oldNetworkOfferingId, null, 0L, VirtualMachine.class.getName(), vmInstance.getUuid());
            return _vmDao.findById(vmInstance.getId());
        }

        throw new CloudRuntimeException("something strange happened, new default network(" + newdefault.getId() + ") is not null, and is not equal to the network(" + nic.getNetworkId() + ") of the chosen nic");
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_SCALE, eventDescription = "scaling Vm")
    public UserVm
    upgradeVirtualMachine(ScaleVMCmd cmd) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException{

        Long vmId = cmd.getId();
        Long newServiceOfferingId = cmd.getServiceOfferingId();
        boolean  result = upgradeVirtualMachine(vmId, newServiceOfferingId);
        if(result){
            return _vmDao.findById(vmId);
        }else{
            return null;
        }

    }

    @Override
    public HashMap<Long, List<VmDiskStatsEntry>> getVmDiskStatistics(long hostId, String hostName, List<Long> vmIds) throws CloudRuntimeException {
        HashMap<Long, List<VmDiskStatsEntry>> vmDiskStatsById = new HashMap<Long, List<VmDiskStatsEntry>>();

        if (vmIds.isEmpty()) {
            return vmDiskStatsById;
        }

        List<String> vmNames = new ArrayList<String>();

        for (Long vmId : vmIds) {
            UserVmVO vm = _vmDao.findById(vmId);
            vmNames.add(vm.getInstanceName());
        }

        Answer answer = _agentMgr.easySend(hostId, new GetVmDiskStatsCommand(vmNames, _hostDao.findById(hostId).getGuid(), hostName));
        if (answer == null || !answer.getResult()) {
            s_logger.warn("Unable to obtain VM disk statistics.");
            return null;
        } else {
            HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsByName = ((GetVmDiskStatsAnswer)answer).getVmDiskStatsMap();

            if (vmDiskStatsByName == null) {
                s_logger.warn("Unable to obtain VM disk statistics.");
                return null;
            }

            for (String vmName : vmDiskStatsByName.keySet()) {
                vmDiskStatsById.put(vmIds.get(vmNames.indexOf(vmName)), vmDiskStatsByName.get(vmName));
            }
        }

        return vmDiskStatsById;
    }

    @Override
    public boolean upgradeVirtualMachine(Long vmId, Long newServiceOfferingId) throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException, VirtualMachineMigrationException{
        Account caller = UserContext.current().getCaller();

        // Verify input parameters
        VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
        if(vmInstance.getHypervisorType() != HypervisorType.XenServer && vmInstance.getHypervisorType() != HypervisorType.VMware){
            throw new InvalidParameterValueException("This operation not permitted for this hypervisor of the vm");
        }

        if(vmInstance.getState().equals(State.Stopped)){
            upgradeStoppedVirtualMachine(vmId, newServiceOfferingId);
            return true;
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // Check that the specified service offering ID is valid
        _itMgr.checkIfCanUpgrade(vmInstance, newServiceOfferingId);

        //Check if its a scale "up"
        ServiceOffering newServiceOffering = _configMgr.getServiceOffering(newServiceOfferingId);
        ServiceOffering currentServiceOffering = _configMgr.getServiceOffering(vmInstance.getServiceOfferingId());
        int newCpu = newServiceOffering.getCpu();
        int newMemory = newServiceOffering.getRamSize();
        int newSpeed = newServiceOffering.getSpeed();
        int currentCpu = currentServiceOffering.getCpu();
        int currentMemory = currentServiceOffering.getRamSize();
        int currentSpeed = currentServiceOffering.getSpeed();

        // Don't allow to scale when (Any of the new values less than current values) OR (All current and new values are same)
        if( (newSpeed < currentSpeed || newMemory < currentMemory || newCpu < currentCpu)
                ||  ( newSpeed == currentSpeed && newMemory == currentMemory && newCpu == currentCpu)){
            throw new InvalidParameterValueException("Only scaling up the vm is supported, new service offering should have both cpu and memory greater than the old values");
        }

        // Check resource limits
        if (newCpu > currentCpu) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.cpu,
                    newCpu - currentCpu);
        }
        if (newMemory > currentMemory) {
            _resourceLimitMgr.checkResourceLimit(caller, ResourceType.memory,
                    newMemory - currentMemory);
        }

        // Dynamically upgrade the running vms
        boolean success = false;
        if(vmInstance.getState().equals(State.Running)){
            int retry = _scaleRetry;
            ExcludeList excludes = new ExcludeList();
            boolean enableDynamicallyScaleVm = Boolean.parseBoolean(_configServer.getConfigValue(Config.EnableDynamicallyScaleVm.key(), Config.ConfigurationParameterScope.zone.toString(), vmInstance.getDataCenterId()));
            if(!enableDynamicallyScaleVm){
               throw new PermissionDeniedException("Dynamically scaling virtual machines is disabled for this zone, please contact your admin");
            }

            while (retry-- != 0) { // It's != so that it can match -1.
                try{
                    boolean existingHostHasCapacity = false;

                    // Increment CPU and Memory count accordingly.
                    if (newCpu > currentCpu) {
                        _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long (newCpu - currentCpu));
                    }
                    if (newMemory > currentMemory) {
                        _resourceLimitMgr.incrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long (newMemory - currentMemory));
                    }

                    // #1 Check existing host has capacity
                    if( !excludes.shouldAvoid(ApiDBUtils.findHostById(vmInstance.getHostId())) ){
                        existingHostHasCapacity = _capacityMgr.checkIfHostHasCapacity(vmInstance.getHostId(), newServiceOffering.getSpeed() - currentServiceOffering.getSpeed(),
                                (newServiceOffering.getRamSize() - currentServiceOffering.getRamSize()) * 1024L * 1024L, false, ApiDBUtils.getCpuOverprovisioningFactor(), 1f, false); // TO DO fill it with mem.
                        excludes.addHost(vmInstance.getHostId());
                    }

                    // #2 migrate the vm if host doesn't have capacity or is in avoid set
                    if (!existingHostHasCapacity){
                        vmInstance = _itMgr.findHostAndMigrate(vmInstance.getType(), vmInstance, newServiceOfferingId, excludes);
                    }

                    // #3 scale the vm now
                    _itMgr.upgradeVmDb(vmId, newServiceOfferingId);
                    vmInstance = _vmInstanceDao.findById(vmId);
                    vmInstance = _itMgr.reConfigureVm(vmInstance, currentServiceOffering, existingHostHasCapacity);
                    success = true;
                    return success;
                }catch(InsufficientCapacityException e ){
                    s_logger.warn("Received exception while scaling ",e);
                } catch (ResourceUnavailableException e) {
                    s_logger.warn("Received exception while scaling ",e);
                } catch (ConcurrentOperationException e) {
                    s_logger.warn("Received exception while scaling ",e);
                } catch (VirtualMachineMigrationException e) {
                    s_logger.warn("Received exception while scaling ",e);
                } catch (ManagementServerException e) {
                    s_logger.warn("Received exception while scaling ",e);
                } catch (Exception e) {
                    s_logger.warn("Received exception while scaling ",e);
                }
                finally{
                    if(!success){
                        _itMgr.upgradeVmDb(vmId, currentServiceOffering.getId()); // rollback
                        // Decrement CPU and Memory count accordingly.
                        if (newCpu > currentCpu) {
                            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.cpu, new Long (newCpu - currentCpu));
                    }
                        if (newMemory > currentMemory) {
                            _resourceLimitMgr.decrementResourceCount(caller.getAccountId(), ResourceType.memory, new Long (newMemory - currentMemory));
                        }
                    }


                }
            }
        }

        return success;
    }


    @Override
    public HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(long hostId,
            String hostName, List<Long> vmIds) throws CloudRuntimeException {
        HashMap<Long, VmStatsEntry> vmStatsById = new HashMap<Long, VmStatsEntry>();

        if (vmIds.isEmpty()) {
            return vmStatsById;
        }

        List<String> vmNames = new ArrayList<String>();

        for (Long vmId : vmIds) {
            UserVmVO vm = _vmDao.findById(vmId);
            vmNames.add(vm.getInstanceName());
        }

        Answer answer = _agentMgr.easySend(hostId, new GetVmStatsCommand(
                vmNames, _hostDao.findById(hostId).getGuid(), hostName));
        if (answer == null || !answer.getResult()) {
            s_logger.warn("Unable to obtain VM statistics.");
            return null;
        } else {
            HashMap<String, VmStatsEntry> vmStatsByName = ((GetVmStatsAnswer) answer)
                    .getVmStatsMap();

            if (vmStatsByName == null) {
                s_logger.warn("Unable to obtain VM statistics.");
                return null;
            }

            for (String vmName : vmStatsByName.keySet()) {
                vmStatsById.put(vmIds.get(vmNames.indexOf(vmName)),
                        vmStatsByName.get(vmName));
            }
        }

        return vmStatsById;
    }

    @Override
    @DB
    public UserVm recoverVirtualMachine(RecoverVMCmd cmd)
            throws ResourceAllocationException, CloudRuntimeException {

        Long vmId = cmd.getId();
        Account caller = UserContext.current().getCaller();

        // Verify input parameters
        UserVmVO vm = _vmDao.findById(vmId.longValue());

        if (vm == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + vmId);
        }

        // check permissions
        _accountMgr.checkAccess(caller, null, true, vm);

        if (vm.getRemoved() != null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Unable to find vm or vm is removed: " + vmId);
            }
            throw new InvalidParameterValueException("Unable to find vm by id "
                    + vmId);
        }

        if (vm.getState() != State.Destroyed) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("vm is not in the right state: " + vmId);
            }
            throw new InvalidParameterValueException("Vm with id " + vmId
                    + " is not in the right state");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Recovering vm " + vmId);
        }

        Transaction txn = Transaction.currentTxn();
        AccountVO account = null;
        txn.start();

        account = _accountDao.lockRow(vm.getAccountId(), true);

        // if the account is deleted, throw error
        if (account.getRemoved() != null) {
            throw new CloudRuntimeException(
                    "Unable to recover VM as the account is deleted");
        }

        // Get serviceOffering for Virtual Machine
        ServiceOfferingVO serviceOffering = _serviceOfferingDao.findById(vm.getServiceOfferingId());

        // First check that the maximum number of UserVMs, CPU and Memory limit for the given
        // accountId will not be exceeded
        resourceLimitCheck(account, new Long(serviceOffering.getCpu()), new Long(serviceOffering.getRamSize()));

        _haMgr.cancelDestroy(vm, vm.getHostId());

        try {
            if (!_itMgr.stateTransitTo(vm,
                    VirtualMachine.Event.RecoveryRequested, null)) {
                s_logger.debug("Unable to recover the vm because it is not in the correct state: "
                        + vmId);
                throw new InvalidParameterValueException(
                        "Unable to recover the vm because it is not in the correct state: "
                                + vmId);
            }
        } catch (NoTransitionException e) {
            throw new InvalidParameterValueException(
                    "Unable to recover the vm because it is not in the correct state: "
                            + vmId);
        }

        // Recover the VM's disks
        List<VolumeVO> volumes = _volsDao.findByInstance(vmId);
        for (VolumeVO volume : volumes) {
            if (volume.getVolumeType().equals(Volume.Type.ROOT)) {
                // Create an event
                Long templateId = volume.getTemplateId();
                Long diskOfferingId = volume.getDiskOfferingId();
                Long offeringId = null;
                if (diskOfferingId != null) {
                    DiskOfferingVO offering = _diskOfferingDao
                            .findById(diskOfferingId);
                    if (offering != null
                            && (offering.getType() == DiskOfferingVO.Type.Disk)) {
                        offeringId = offering.getId();
                    }
                }
                UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_CREATE, volume.getAccountId(),
                        volume.getDataCenterId(), volume.getId(), volume.getName(), offeringId, templateId,
                        volume.getSize(), Volume.class.getName(), volume.getUuid());
            }
        }

        //Update Resource Count for the given account
        _resourceLimitMgr.incrementResourceCount(account.getId(),
                ResourceType.volume, new Long(volumes.size()));
        resourceCountIncrement(account.getId(), new Long(serviceOffering.getCpu()),
                new Long(serviceOffering.getRamSize()));
        txn.commit();

        return _vmDao.findById(vmId);
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {
        _name = name;

        if (_configDao == null) {
            throw new ConfigurationException(
                    "Unable to get the configuration dao.");
        }

        Map<String, String> configs = _configDao.getConfiguration(
                "AgentManager", params);

        _instance = configs.get("instance.name");
        if (_instance == null) {
            _instance = "DEFAULT";
        }

        String value = _configDao
                .getValue(Config.CreatePrivateTemplateFromVolumeWait.toString());
        _createprivatetemplatefromvolumewait = NumbersUtil.parseInt(value,
                Integer.parseInt(Config.CreatePrivateTemplateFromVolumeWait
                        .getDefaultValue()));

        value = _configDao
                .getValue(Config.CreatePrivateTemplateFromSnapshotWait
                        .toString());
        _createprivatetemplatefromsnapshotwait = NumbersUtil.parseInt(value,
                Integer.parseInt(Config.CreatePrivateTemplateFromSnapshotWait
                        .getDefaultValue()));

        String workers = configs.get("expunge.workers");
        int wrks = NumbersUtil.parseInt(workers, 10);

        String time = configs.get("expunge.interval");
        _expungeInterval = NumbersUtil.parseInt(time, 86400);
        time = configs.get("expunge.delay");
        _expungeDelay = NumbersUtil.parseInt(time, _expungeInterval);

        _executor = Executors.newScheduledThreadPool(wrks, new NamedThreadFactory("UserVm-Scavenger"));

        String aggregationRange = configs.get("usage.stats.job.aggregation.range");
        int _usageAggregationRange  = NumbersUtil.parseInt(aggregationRange, 1440);
        int HOURLY_TIME = 60;
        final int DAILY_TIME = 60 * 24;
        if (_usageAggregationRange == DAILY_TIME) {
            _dailyOrHourly = true;
        } else if (_usageAggregationRange == HOURLY_TIME) {
            _dailyOrHourly = true;
        } else {
            _dailyOrHourly = false;
        }

        _itMgr.registerGuru(VirtualMachine.Type.User, this);

        VirtualMachine.State.getStateMachine().registerListener(
                new UserVmStateListener(_usageEventDao, _networkDao, _nicDao));

        value = _configDao.getValue(Config.SetVmInternalNameUsingDisplayName.key());

        if(value == null) {
            _instanceNameFlag = false;
        }
        else
        {
            _instanceNameFlag = Boolean.parseBoolean(value);
        }

       _scaleRetry = NumbersUtil.parseInt(configs.get(Config.ScaleRetry.key()), 2);

        s_logger.info("User VM Manager is configured.");

        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        _executor.scheduleWithFixedDelay(new ExpungeTask(), _expungeInterval,
                _expungeInterval, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        _executor.shutdown();
        return true;
    }

    protected UserVmManagerImpl() {
    }

    public String getRandomPrivateTemplateName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long convertToId(String vmName) {
        if (!VirtualMachineName.isValidVmName(vmName, _instance)) {
            return null;
        }
        return VirtualMachineName.getVmId(vmName);
    }

    @Override
    public boolean expunge(UserVmVO vm, long callerUserId, Account caller) {
        UserContext ctx = UserContext.current();
        ctx.setAccountId(vm.getAccountId());

        try {
            // expunge the vm
            if (!_itMgr.advanceExpunge(vm, _accountMgr.getSystemUser(), caller)) {
                s_logger.info("Did not expunge " + vm);
                return false;
            }

            // Only if vm is not expunged already, cleanup it's resources
            if (vm != null && vm.getRemoved() == null) {
                // Cleanup vm resources - all the PF/LB/StaticNat rules
                // associated with vm
                s_logger.debug("Starting cleaning up vm " + vm
                        + " resources...");
                if (cleanupVmResources(vm.getId())) {
                    s_logger.debug("Successfully cleaned up vm " + vm
                            + " resources as a part of expunge process");
                } else {
                    s_logger.warn("Failed to cleanup resources as a part of vm "
                            + vm + " expunge");
                    return false;
                }

                _itMgr.remove(vm, _accountMgr.getSystemUser(), caller);
            }

            return true;

        } catch (ResourceUnavailableException e) {
            s_logger.warn("Unable to expunge  " + vm, e);
            return false;
        } catch (OperationTimedoutException e) {
            s_logger.warn("Operation time out on expunging " + vm, e);
            return false;
        } catch (ConcurrentOperationException e) {
            s_logger.warn("Concurrent operations on expunging " + vm, e);
            return false;
        }
    }

    private boolean cleanupVmResources(long vmId) {
        boolean success = true;
        // Remove vm from security groups
        _securityGroupMgr.removeInstanceFromGroups(vmId);

        // Remove vm from instance group
        removeInstanceFromInstanceGroup(vmId);

        // cleanup firewall rules
        if (_firewallMgr.revokeFirewallRulesForVm(vmId)) {
            s_logger.debug("Firewall rules are removed successfully as a part of vm id="
                    + vmId + " expunge");
        } else {
            success = false;
            s_logger.warn("Fail to remove firewall rules as a part of vm id="
                    + vmId + " expunge");
        }

        // cleanup port forwarding rules
        if (_rulesMgr.revokePortForwardingRulesForVm(vmId)) {
            s_logger.debug("Port forwarding rules are removed successfully as a part of vm id="
                    + vmId + " expunge");
        } else {
            success = false;
            s_logger.warn("Fail to remove port forwarding rules as a part of vm id="
                    + vmId + " expunge");
        }

        // cleanup load balancer rules
        if (_lbMgr.removeVmFromLoadBalancers(vmId)) {
            s_logger.debug("Removed vm id=" + vmId
                    + " from all load balancers as a part of expunge process");
        } else {
            success = false;
            s_logger.warn("Fail to remove vm id=" + vmId
                    + " from load balancers as a part of expunge process");
        }

        // If vm is assigned to static nat, disable static nat for the ip
        // address and disassociate ip if elasticIP is enabled
        IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(vmId);
        try {
            if (ip != null) {
                if (_rulesMgr.disableStaticNat(ip.getId(),
                        _accountMgr.getAccount(Account.ACCOUNT_ID_SYSTEM),
                        User.UID_SYSTEM, true)) {
                    s_logger.debug("Disabled 1-1 nat for ip address " + ip
                            + " as a part of vm id=" + vmId + " expunge");
                } else {
                    s_logger.warn("Failed to disable static nat for ip address "
                            + ip + " as a part of vm id=" + vmId + " expunge");
                    success = false;
                }
            }
        } catch (ResourceUnavailableException e) {
            success = false;
            s_logger.warn("Failed to disable static nat for ip address " + ip
                    + " as a part of vm id=" + vmId
                    + " expunge because resource is unavailable", e);
        }

        return success;
    }

    @Override
    public void deletePrivateTemplateRecord(Long templateId) {
        if (templateId != null) {
            _templateDao.remove(templateId);
        }
    }

    // used for vm transitioning to error state
    private void updateVmStateForFailedVmCreation(Long vmId, Long hostId) {

        UserVmVO vm = _vmDao.findById(vmId);

        if (vm != null) {
            if (vm.getState().equals(State.Stopped)) {
                s_logger.debug("Destroying vm " + vm + " as it failed to create on Host with Id:" + hostId);
                try {
                    _itMgr.stateTransitTo(vm,
                            VirtualMachine.Event.OperationFailedToError, null);
                } catch (NoTransitionException e1) {
                    s_logger.warn(e1.getMessage());
                }
                // destroy associated volumes for vm in error state
                // get all volumes in non destroyed state
                List<VolumeVO> volumesForThisVm = _volsDao
                        .findUsableVolumesForInstance(vm.getId());
                for (VolumeVO volume : volumesForThisVm) {
                    if (volume.getState() != Volume.State.Destroy) {
                        volumeMgr.destroyVolume(volume);
                    }
                }
                String msg = "Failed to deploy Vm with Id: " + vmId + ", on Host with Id: " + hostId;
                _alertMgr.sendAlert(AlertManager.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);

                // Get serviceOffering for Virtual Machine
                ServiceOfferingVO offering = _serviceOfferingDao.findById(vm.getServiceOfferingId());

                // Update Resource Count for the given account
                resourceCountDecrement(vm.getAccountId(), new Long(offering.getCpu()),
                        new Long(offering.getRamSize()));
            }
        }
    }

    protected class ExpungeTask implements Runnable {
        public ExpungeTask() {
        }

        @Override
        public void run() {
            UserContext.registerContext(_accountMgr.getSystemUser().getId(), _accountMgr.getSystemAccount(), null, false);
            GlobalLock scanLock = GlobalLock.getInternLock("UserVMExpunge");
            try {
                if (scanLock.lock(ACQUIRE_GLOBAL_LOCK_TIMEOUT_FOR_COOPERATION)) {
                    try {
                        List<UserVmVO> vms = _vmDao.findDestroyedVms(new Date(
                                System.currentTimeMillis()
                                - ((long) _expungeDelay << 10)));
                        if (s_logger.isInfoEnabled()) {
                            if (vms.size() == 0) {
                                s_logger.trace("Found " + vms.size()
                                        + " vms to expunge.");
                            } else {
                                s_logger.info("Found " + vms.size()
                                        + " vms to expunge.");
                            }
                        }
                        for (UserVmVO vm : vms) {
                            try {
                                expunge(vm,
                                        _accountMgr.getSystemUser().getId(),
                                        _accountMgr.getSystemAccount());
                            } catch (Exception e) {
                                s_logger.warn("Unable to expunge " + vm, e);
                            }
                        }
                    } catch (Exception e) {
                        s_logger.error("Caught the following Exception", e);
                    } finally {
                        scanLock.unlock();
                    }
                }
            } finally {
                scanLock.releaseRef();
                UserContext.unregisterContext();
            }
        }
    }

    private static boolean isAdmin(short accountType) {
        return ((accountType == Account.ACCOUNT_TYPE_ADMIN)
                || (accountType == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN)
                || (accountType == Account.ACCOUNT_TYPE_DOMAIN_ADMIN) || (accountType == Account.ACCOUNT_TYPE_READ_ONLY_ADMIN));
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_UPDATE, eventDescription = "updating Vm")
    public UserVm updateVirtualMachine(UpdateVMCmd cmd)
            throws ResourceUnavailableException, InsufficientCapacityException {
        String displayName = cmd.getDisplayName();
        String group = cmd.getGroup();
        Boolean ha = cmd.getHaEnable();
        Boolean isDisplayVmEnabled = cmd.getDisplayVm();
        Long id = cmd.getId();
        Long osTypeId = cmd.getOsTypeId();
        String userData = cmd.getUserData();
        Boolean isDynamicallyScalable = cmd.isDynamicallyScalable();
        Account caller = UserContext.current().getCaller();

        // Input validation
        UserVmVO vmInstance = null;

        // Verify input parameters
        vmInstance = _vmDao.findById(id.longValue());

        if (vmInstance == null) {
            throw new InvalidParameterValueException(
                    "unable to find virtual machine with id " + id);
        }

        ServiceOffering offering = _serviceOfferingDao.findById(vmInstance
                .getServiceOfferingId());
        if (!offering.getOfferHA() && ha != null && ha) {
            throw new InvalidParameterValueException(
                    "Can't enable ha for the vm as it's created from the Service offering having HA disabled");
        }

        _accountMgr.checkAccess(UserContext.current().getCaller(), null, true,
                vmInstance);

        if (displayName == null) {
            displayName = vmInstance.getDisplayName();
        }

        if (ha == null) {
            ha = vmInstance.isHaEnabled();
        }

        if (isDisplayVmEnabled == null) {
            isDisplayVmEnabled = vmInstance.isDisplayVm();
        } else{
            if(!_accountMgr.isRootAdmin(caller.getType())){
                throw new PermissionDeniedException( "Cannot update parameter displayvm, only admin permitted ");
            }
        }

        UserVmVO vm = _vmDao.findById(id);
        if (vm == null) {
            throw new CloudRuntimeException(
                    "Unable to find virual machine with id " + id);
        }

        if (vm.getState() == State.Error || vm.getState() == State.Expunging) {
            s_logger.error("vm is not in the right state: " + id);
            throw new InvalidParameterValueException("Vm with id " + id
                    + " is not in the right state");
        }

        boolean updateUserdata = false;
        if (userData != null) {
            // check and replace newlines
            userData = userData.replace("\\n", "");
            validateUserData(userData, cmd.getHttpMethod());
            // update userData on domain router.
            updateUserdata = true;
        } else {
            userData = vmInstance.getUserData();
        }

        String description = "";

        if (displayName != null && !displayName.equals(vmInstance.getDisplayName())) {
            description += "New display name: " + displayName + ". ";
        }

        if (ha != vmInstance.isHaEnabled()) {
            if (ha) {
                description += "Enabled HA. ";
            } else {
                description += "Disabled HA. ";
            }
        }
        if (osTypeId == null) {
            osTypeId = vmInstance.getGuestOSId();
        } else {
            description += "Changed Guest OS Type to " + osTypeId + ". ";
        }

        if (group != null) {
            if (addInstanceToGroup(id, group)) {
                description += "Added to group: " + group + ".";
            }
        }

        if (isDynamicallyScalable != null) {
            UserVmDetailVO vmDetailVO = _vmDetailsDao.findDetail(vm.getId(), VirtualMachine.IsDynamicScalingEnabled);
            if (vmDetailVO == null) {
                vmDetailVO = new UserVmDetailVO(vm.getId(), VirtualMachine.IsDynamicScalingEnabled, isDynamicallyScalable.toString());
                _vmDetailsDao.persist(vmDetailVO);
            } else {
                vmDetailVO.setValue(isDynamicallyScalable.toString());
                _vmDetailsDao.update(vmDetailVO.getId(), vmDetailVO);
            }
        }

        _vmDao.updateVM(id, displayName, ha, osTypeId, userData, isDisplayVmEnabled);

        if (updateUserdata) {
            boolean result = updateUserDataInternal(_vmDao.findById(id));
            if (result) {
                s_logger.debug("User data successfully updated for vm id="+id);
            } else {
                throw new CloudRuntimeException("Failed to reset userdata for the virtual machine ");
            }
        }

        return _vmDao.findById(id);
    }

    private boolean updateUserDataInternal(UserVm vm)
            throws ResourceUnavailableException, InsufficientCapacityException {
        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm.getTemplateId());
        Nic defaultNic = _networkModel.getDefaultNic(vm.getId());
        if (defaultNic == null) {
            s_logger.error("Unable to update userdata for vm id=" + vm.getId() + " as the instance doesn't have default nic");
            return false;
        }

        Network defaultNetwork = _networkDao.findById(defaultNic.getNetworkId());
        NicProfile defaultNicProfile = new NicProfile(defaultNic, defaultNetwork, null, null, null,
                _networkModel.isSecurityGroupSupportedInNetwork(defaultNetwork),
                _networkModel.getNetworkTag(template.getHypervisorType(), defaultNetwork));

        VirtualMachineProfile<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>((VMInstanceVO)vm);

        UserDataServiceProvider element = _networkModel.getUserDataUpdateProvider(defaultNetwork);
        if (element == null) {
            throw new CloudRuntimeException("Can't find network element for " + Service.UserData.getName() + " provider needed for UserData update");
        }
        boolean result = element.saveUserData(defaultNetwork, defaultNicProfile, vmProfile);

        return true;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_START, eventDescription = "starting Vm", async = true)
    public UserVm startVirtualMachine(StartVMCmd cmd)
            throws ExecutionException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        return startVirtualMachine(cmd.getId(), cmd.getHostId(), null).first();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_REBOOT, eventDescription = "rebooting Vm", async = true)
    public UserVm rebootVirtualMachine(RebootVMCmd cmd)
            throws InsufficientCapacityException, ResourceUnavailableException {
        Account caller = UserContext.current().getCaller();
        Long vmId = cmd.getId();

        // Verify input parameters
        UserVmVO vmInstance = _vmDao.findById(vmId.longValue());
        if (vmInstance == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(caller, null, true, vmInstance);

        // If the VM is Volatile in nature, on reboot discard the VM's root disk and create a new root disk for it: by calling restoreVM
        long serviceOfferingId = vmInstance.getServiceOfferingId();
        ServiceOfferingVO offering = _serviceOfferingDao.findById(serviceOfferingId);
        if(offering != null && offering.getRemoved() == null) {
            if(offering.getVolatileVm()){
                return restoreVMInternal(caller, vmInstance, null);
            }
        } else {
            throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId + " corresponding to the vm");
        }

        return rebootVirtualMachine(UserContext.current().getCallerUserId(),
                vmId);
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_DESTROY, eventDescription = "destroying Vm", async = true)
    public UserVm destroyVm(DestroyVMCmd cmd)
            throws ResourceUnavailableException, ConcurrentOperationException {
        return destroyVm(cmd.getId());
    }

    @Override
    @DB
    public InstanceGroupVO createVmGroup(CreateVMGroupCmd cmd) {
        Account caller = UserContext.current().getCaller();
        Long domainId = cmd.getDomainId();
        String accountName = cmd.getAccountName();
        String groupName = cmd.getGroupName();
        Long projectId = cmd.getProjectId();

        Account owner = _accountMgr.finalizeOwner(caller, accountName,
                domainId, projectId);
        long accountId = owner.getId();

        // Check if name is already in use by this account
        boolean isNameInUse = _vmGroupDao.isNameInUse(accountId, groupName);

        if (isNameInUse) {
            throw new InvalidParameterValueException(
                    "Unable to create vm group, a group with name " + groupName
                    + " already exisits for account " + accountId);
        }

        return createVmGroup(groupName, accountId);
    }

    @DB
    protected InstanceGroupVO createVmGroup(String groupName, long accountId) {
        Account account = null;
        final Transaction txn = Transaction.currentTxn();
        txn.start();
        try {
            account = _accountDao.acquireInLockTable(accountId); // to ensure
            // duplicate
            // vm group
            // names are
            // not
            // created.
            if (account == null) {
                s_logger.warn("Failed to acquire lock on account");
                return null;
            }
            InstanceGroupVO group = _vmGroupDao.findByAccountAndName(accountId,
                    groupName);
            if (group == null) {
                group = new InstanceGroupVO(groupName, accountId);
                group = _vmGroupDao.persist(group);
            }
            return group;
        } finally {
            if (account != null) {
                _accountDao.releaseFromLockTable(accountId);
            }
            txn.commit();
        }
    }

    @Override
    public boolean deleteVmGroup(DeleteVMGroupCmd cmd) {
        Account caller = UserContext.current().getCaller();
        Long groupId = cmd.getId();

        // Verify input parameters
        InstanceGroupVO group = _vmGroupDao.findById(groupId);
        if ((group == null) || (group.getRemoved() != null)) {
            throw new InvalidParameterValueException(
                    "unable to find a vm group with id " + groupId);
        }

        _accountMgr.checkAccess(caller, null, true, group);

        return deleteVmGroup(groupId);
    }

    @Override
    public boolean deleteVmGroup(long groupId) {
        // delete all the mappings from group_vm_map table
        List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao
                .listByGroupId(groupId);
        for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
            SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao
                    .createSearchCriteria();
            sc.addAnd("instanceId", SearchCriteria.Op.EQ,
                    groupMap.getInstanceId());
            _groupVMMapDao.expunge(sc);
        }

        if (_vmGroupDao.remove(groupId)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @DB
    public boolean addInstanceToGroup(long userVmId, String groupName) {
        UserVmVO vm = _vmDao.findById(userVmId);

        InstanceGroupVO group = _vmGroupDao.findByAccountAndName(
                vm.getAccountId(), groupName);
        // Create vm group if the group doesn't exist for this account
        if (group == null) {
            group = createVmGroup(groupName, vm.getAccountId());
        }

        if (group != null) {
            final Transaction txn = Transaction.currentTxn();
            txn.start();
            UserVm userVm = _vmDao.acquireInLockTable(userVmId);
            if (userVm == null) {
                s_logger.warn("Failed to acquire lock on user vm id="
                        + userVmId);
            }
            try {
                // don't let the group be deleted when we are assigning vm to
                // it.
                InstanceGroupVO ngrpLock = _vmGroupDao.lockRow(group.getId(),
                        false);
                if (ngrpLock == null) {
                    s_logger.warn("Failed to acquire lock on vm group id="
                            + group.getId() + " name=" + group.getName());
                    txn.rollback();
                    return false;
                }

                // Currently don't allow to assign a vm to more than one group
                if (_groupVMMapDao.listByInstanceId(userVmId) != null) {
                    // Delete all mappings from group_vm_map table
                    List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao
                            .listByInstanceId(userVmId);
                    for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
                        SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao
                                .createSearchCriteria();
                        sc.addAnd("instanceId", SearchCriteria.Op.EQ,
                                groupMap.getInstanceId());
                        _groupVMMapDao.expunge(sc);
                    }
                }
                InstanceGroupVMMapVO groupVmMapVO = new InstanceGroupVMMapVO(
                        group.getId(), userVmId);
                _groupVMMapDao.persist(groupVmMapVO);

                txn.commit();
                return true;
            } finally {
                if (userVm != null) {
                    _vmDao.releaseFromLockTable(userVmId);
                }
            }
        }
        return false;
    }

    @Override
    public InstanceGroupVO getGroupForVm(long vmId) {
        // TODO - in future releases vm can be assigned to multiple groups; but
        // currently return just one group per vm
        try {
            List<InstanceGroupVMMapVO> groupsToVmMap = _groupVMMapDao
                    .listByInstanceId(vmId);

            if (groupsToVmMap != null && groupsToVmMap.size() != 0) {
                InstanceGroupVO group = _vmGroupDao.findById(groupsToVmMap.get(
                        0).getGroupId());
                return group;
            } else {
                return null;
            }
        } catch (Exception e) {
            s_logger.warn("Error trying to get group for a vm: ", e);
            return null;
        }
    }

    @Override
    public void removeInstanceFromInstanceGroup(long vmId) {
        try {
            List<InstanceGroupVMMapVO> groupVmMaps = _groupVMMapDao
                    .listByInstanceId(vmId);
            for (InstanceGroupVMMapVO groupMap : groupVmMaps) {
                SearchCriteria<InstanceGroupVMMapVO> sc = _groupVMMapDao
                        .createSearchCriteria();
                sc.addAnd("instanceId", SearchCriteria.Op.EQ,
                        groupMap.getInstanceId());
                _groupVMMapDao.expunge(sc);
            }
        } catch (Exception e) {
            s_logger.warn("Error trying to remove vm from group: ", e);
        }
    }

    protected boolean validPassword(String password) {
        if (password == null || password.length() == 0) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) == ' ') {
                return false;
            }
        }
        return true;
    }

    @Override
    public UserVm createBasicSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> securityGroupIdList, Account owner,
            String hostName, String displayName, Long diskOfferingId, Long diskSize, String group,
	    HypervisorType hypervisor, HTTPMethod httpmethod, String userData, String sshKeyPair,
	    Map<Long, IpAddresses> requestedIps, IpAddresses defaultIps, Boolean displayVm, String keyboard,
	    List<Long> affinityGroupIdList)
        throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {

        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();

        // Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, null, true, owner);

        // Get default guest network in Basic zone
        Network defaultNetwork = _networkModel.getExclusiveGuestNetwork(zone.getId());

        if (defaultNetwork == null) {
            throw new InvalidParameterValueException(
                    "Unable to find a default network to start a vm");
        } else {
            networkList.add(_networkDao.findById(defaultNetwork.getId()));
        }

        boolean isVmWare = (template.getHypervisorType() == HypervisorType.VMware || (hypervisor != null && hypervisor == HypervisorType.VMware));

        if (securityGroupIdList != null && isVmWare) {
            throw new InvalidParameterValueException("Security group feature is not supported for vmWare hypervisor");
        } else if (!isVmWare && _networkModel.isSecurityGroupSupportedInNetwork(defaultNetwork) && _networkModel.canAddDefaultSecurityGroup()) {
            //add the default securityGroup only if no security group is specified
            if (securityGroupIdList == null || securityGroupIdList.isEmpty()) {
                if (securityGroupIdList == null) {
                    securityGroupIdList = new ArrayList<Long>();
                }
                SecurityGroup defaultGroup = _securityGroupMgr
                        .getDefaultSecurityGroup(owner.getId());
                if (defaultGroup != null) {
                    securityGroupIdList.add(defaultGroup.getId());
                } else {
                    // create default security group for the account
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Couldn't find default security group for the account "
                                + owner + " so creating a new one");
                    }
                    defaultGroup = _securityGroupMgr.createSecurityGroup(
                            SecurityGroupManager.DEFAULT_GROUP_NAME,
                            SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION,
                            owner.getDomainId(), owner.getId(),
                            owner.getAccountName());
                    securityGroupIdList.add(defaultGroup.getId());
                }
            }
        }

        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId,
                diskSize, networkList, securityGroupIdList, group, httpmethod, userData, sshKeyPair, hypervisor,
		caller, requestedIps, defaultIps, displayVm, keyboard, affinityGroupIdList);

    }

    @Override
    public UserVm createAdvancedSecurityGroupVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList,
            List<Long> securityGroupIdList, Account owner, String hostName, String displayName, Long diskOfferingId,
	        Long diskSize, String group, HypervisorType hypervisor, HTTPMethod httpmethod, String userData,
            String sshKeyPair, Map<Long, IpAddresses> requestedIps, IpAddresses defaultIps, Boolean displayVm, String keyboard,
	        List<Long> affinityGroupIdList) throws InsufficientCapacityException, ConcurrentOperationException,
	        ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {

        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();
        boolean isSecurityGroupEnabledNetworkUsed = false;
        boolean isVmWare = (template.getHypervisorType() == HypervisorType.VMware || (hypervisor != null && hypervisor == HypervisorType.VMware));

        // Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, null, true, owner);

        // If no network is specified, find system security group enabled network
        if (networkIdList == null || networkIdList.isEmpty()) {
            Network networkWithSecurityGroup = _networkModel.getNetworkWithSecurityGroupEnabled(zone.getId());
            if (networkWithSecurityGroup == null) {
                throw new InvalidParameterValueException("No network with security enabled is found in zone id=" + zone.getId());
            }

            networkList.add(_networkDao.findById(networkWithSecurityGroup.getId()));
            isSecurityGroupEnabledNetworkUsed = true;

        } else if (securityGroupIdList != null && !securityGroupIdList.isEmpty()) {
            if (isVmWare) {
                throw new InvalidParameterValueException("Security group feature is not supported for vmWare hypervisor");
            }
            // Only one network can be specified, and it should be security group enabled
            if (networkIdList.size() > 1) {
                throw new InvalidParameterValueException("Only support one network per VM if security group enabled");
            }

            NetworkVO network = _networkDao.findById(networkIdList.get(0).longValue());

            if (network == null) {
                throw new InvalidParameterValueException(
                        "Unable to find network by id "
                                + networkIdList.get(0).longValue());
            }

            if (!_networkModel.isSecurityGroupSupportedInNetwork(network)) {
                throw new InvalidParameterValueException("Network is not security group enabled: " + network.getId());
            }

            networkList.add(network);
            isSecurityGroupEnabledNetworkUsed = true;

        } else {
            // Verify that all the networks are Shared/Guest; can't create combination of SG enabled and disabled networks
            for (Long networkId : networkIdList) {
                NetworkVO network = _networkDao.findById(networkId);

                if (network == null) {
                    throw new InvalidParameterValueException("Unable to find network by id " + networkIdList.get(0).longValue());
                }

                boolean isSecurityGroupEnabled = _networkModel.isSecurityGroupSupportedInNetwork(network);
                if (isSecurityGroupEnabled) {
                    if (networkIdList.size() > 1) {
                        throw new InvalidParameterValueException("Can't create a vm with multiple networks one of" +
                                " which is Security Group enabled");
                    }

                    isSecurityGroupEnabledNetworkUsed = true;
                }

                if (!(network.getTrafficType() == TrafficType.Guest && network.getGuestType() == Network.GuestType.Shared)) {
                    throw new InvalidParameterValueException("Can specify only Shared Guest networks when" +
                            " deploy vm in Advance Security Group enabled zone");
                }

                // Perform account permission check
                if (network.getAclType() == ACLType.Account) {
                    _accountMgr.checkAccess(caller, AccessType.UseNetwork, false, network);
                }
                networkList.add(network);
            }
        }

        // if network is security group enabled, and no security group is specified, then add the default security group automatically
        if (isSecurityGroupEnabledNetworkUsed && !isVmWare && _networkModel.canAddDefaultSecurityGroup()) {

            //add the default securityGroup only if no security group is specified
            if(securityGroupIdList == null || securityGroupIdList.isEmpty()){
                if (securityGroupIdList == null) {
                    securityGroupIdList = new ArrayList<Long>();
                }

                SecurityGroup defaultGroup = _securityGroupMgr
                        .getDefaultSecurityGroup(owner.getId());
                if (defaultGroup != null) {
                    securityGroupIdList.add(defaultGroup.getId());
                } else {
                    // create default security group for the account
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Couldn't find default security group for the account "
                                + owner + " so creating a new one");
                    }
                    defaultGroup = _securityGroupMgr.createSecurityGroup(
                            SecurityGroupManager.DEFAULT_GROUP_NAME,
                            SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION,
                            owner.getDomainId(), owner.getId(),
                            owner.getAccountName());
                    securityGroupIdList.add(defaultGroup.getId());
                }
            }
        }

        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId,
                diskSize, networkList, securityGroupIdList, group, httpmethod, userData, sshKeyPair, hypervisor,
		        caller, requestedIps, defaultIps, displayVm, keyboard, affinityGroupIdList);
    }

    @Override
    public UserVm createAdvancedVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, List<Long> networkIdList, Account owner, String hostName,
        String displayName, Long diskOfferingId, Long diskSize, String group, HypervisorType hypervisor,
	    HTTPMethod httpmethod, String userData, String sshKeyPair, Map<Long, IpAddresses> requestedIps,
	    IpAddresses defaultIps, Boolean displayvm, String keyboard, List<Long> affinityGroupIdList)
        throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, StorageUnavailableException, ResourceAllocationException {

        Account caller = UserContext.current().getCaller();
        List<NetworkVO> networkList = new ArrayList<NetworkVO>();

        // Verify that caller can perform actions in behalf of vm owner
        _accountMgr.checkAccess(caller, null, true, owner);

        List<HypervisorType> vpcSupportedHTypes = _vpcMgr
                .getSupportedVpcHypervisors();
        if (networkIdList == null || networkIdList.isEmpty()) {
            NetworkVO defaultNetwork = null;

            // if no network is passed in
            // Check if default virtual network offering has
            // Availability=Required. If it's true, search for corresponding
            // network
            // * if network is found, use it. If more than 1 virtual network is
            // found, throw an error
            // * if network is not found, create a new one and use it

            List<NetworkOfferingVO> requiredOfferings = _networkOfferingDao
                    .listByAvailability(Availability.Required, false);
            if (requiredOfferings.size() < 1) {
                throw new InvalidParameterValueException(
                        "Unable to find network offering with availability="
                                + Availability.Required
                                + " to automatically create the network as a part of vm creation");
            }

            if (requiredOfferings.get(0).getState() == NetworkOffering.State.Enabled) {
                // get Virtual networks
                List<? extends Network> virtualNetworks = _networkModel.listNetworksForAccount(owner.getId(), zone.getId(), Network.GuestType.Isolated);
                if (virtualNetworks.isEmpty()) {
                    long physicalNetworkId = _networkModel.findPhysicalNetworkId(zone.getId(), requiredOfferings.get(0).getTags(), requiredOfferings.get(0).getTrafficType());
                    // Validate physical network
                    PhysicalNetwork physicalNetwork = _physicalNetworkDao
                            .findById(physicalNetworkId);
                    if (physicalNetwork == null) {
                        throw new InvalidParameterValueException("Unable to find physical network with id: "+physicalNetworkId   + " and tag: " +requiredOfferings.get(0).getTags());
                    }
                    s_logger.debug("Creating network for account " + owner + " from the network offering id=" +requiredOfferings.get(0).getId() + " as a part of deployVM process");
                    Network newNetwork = _networkMgr.createGuestNetwork(requiredOfferings.get(0).getId(),
                            owner.getAccountName() + "-network", owner.getAccountName() + "-network", null, null,
                            null, null, owner, null, physicalNetwork, zone.getId(), ACLType.Account, null, null, null, null, true, null);
                    defaultNetwork = _networkDao.findById(newNetwork.getId());
                } else if (virtualNetworks.size() > 1) {
                    throw new InvalidParameterValueException(
                            "More than 1 default Isolated networks are found for account "
                                    + owner + "; please specify networkIds");
                } else {
                    defaultNetwork = _networkDao.findById(virtualNetworks.get(0).getId());
                }
            } else {
                throw new InvalidParameterValueException(
                        "Required network offering id="
                                + requiredOfferings.get(0).getId()
                                + " is not in " + NetworkOffering.State.Enabled);
            }

            networkList.add(defaultNetwork);

        } else {
            for (Long networkId : networkIdList) {
                NetworkVO network = _networkDao.findById(networkId);
                if (network == null) {
                    throw new InvalidParameterValueException(
                            "Unable to find network by id "
                                    + networkIdList.get(0).longValue());
                }
                if (network.getVpcId() != null) {
                    // Only ISOs, XenServer, KVM, and VmWare template types are
                    // supported for vpc networks
                    if (template.getFormat() != ImageFormat.ISO
                            && !vpcSupportedHTypes.contains(template
                                    .getHypervisorType())) {
                        throw new InvalidParameterValueException(
                                "Can't create vm from template with hypervisor "
                                        + template.getHypervisorType()
                                        + " in vpc network " + network);
                    }

                    // Only XenServer, KVM, and VMware hypervisors are supported
                    // for vpc networks
                    if (!vpcSupportedHTypes.contains(hypervisor)) {
                        throw new InvalidParameterValueException(
                                "Can't create vm of hypervisor type "
                                        + hypervisor + " in vpc network");
                    }

                }

                _networkModel.checkNetworkPermissions(owner, network);

                // don't allow to use system networks
                NetworkOffering networkOffering = _configMgr
                        .getNetworkOffering(network.getNetworkOfferingId());
                if (networkOffering.isSystemOnly()) {
                    throw new InvalidParameterValueException(
                            "Network id="
                                    + networkId
                                    + " is system only and can't be used for vm deployment");
                }
                networkList.add(network);
            }
        }

        return createVirtualMachine(zone, serviceOffering, template, hostName, displayName, owner, diskOfferingId,
		diskSize, networkList, null, group, httpmethod, userData, sshKeyPair, hypervisor, caller, requestedIps,
		defaultIps, displayvm, keyboard, affinityGroupIdList);
    }


    public void checkNameForRFCCompliance(String name) {
        if (!NetUtils.verifyDomainNameLabel(name, true)) {
            throw new InvalidParameterValueException("Invalid name. Vm name can contain ASCII letters 'a' through 'z', the digits '0' through '9', "
                    + "and the hyphen ('-'), must be between 1 and 63 characters long, and can't start or end with \"-\" and can't start with digit");
        }
    }

    @DB @ActionEvent(eventType = EventTypes.EVENT_VM_CREATE, eventDescription = "deploying Vm", create = true)
    protected UserVm createVirtualMachine(DataCenter zone, ServiceOffering serviceOffering, VirtualMachineTemplate template, String hostName, String displayName, Account owner, Long diskOfferingId,
        Long diskSize, List<NetworkVO> networkList, List<Long> securityGroupIdList, String group, HTTPMethod httpmethod,
	    String userData, String sshKeyPair, HypervisorType hypervisor, Account caller, Map<Long, IpAddresses> requestedIps,
	    IpAddresses defaultIps, Boolean isDisplayVmEnabled, String keyboard, List<Long> affinityGroupIdList)
                    throws InsufficientCapacityException, ResourceUnavailableException, ConcurrentOperationException, StorageUnavailableException, ResourceAllocationException {

        _accountMgr.checkAccess(caller, null, true, owner);

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException(
                    "The owner of vm to deploy is disabled: " + owner);
        }

        long accountId = owner.getId();

        assert !(requestedIps != null && (defaultIps.getIp4Address() != null || defaultIps.getIp6Address() != null)) : "requestedIp list and defaultNetworkIp should never be specified together";

        if (Grouping.AllocationState.Disabled == zone.getAllocationState()
                && !_accountMgr.isRootAdmin(caller.getType())) {
            throw new PermissionDeniedException(
                    "Cannot perform this operation, Zone is currently disabled: "
                            + zone.getId());
        }

        boolean isExplicit = false;
        // check affinity group type Explicit dedication
        if (affinityGroupIdList != null) {
            for (Long affinityGroupId : affinityGroupIdList) {
                AffinityGroupVO ag = _affinityGroupDao.findById(affinityGroupId);
                String agType = ag.getType();
                if (agType.equals("ExplicitDedication")) {
                    isExplicit = true;
                }
            }
        }
        // check if zone is dedicated
        DedicatedResourceVO dedicatedZone = _dedicatedDao.findByZoneId(zone.getId());
        if (isExplicit && dedicatedZone != null) {
            DomainVO domain = _domainDao.findById(dedicatedZone.getDomainId());
            if (domain == null) {
                throw new CloudRuntimeException("Unable to find the domain "
                        + zone.getDomainId() + " for the zone: " + zone);
            }
            // check that caller can operate with domain
            _configMgr.checkZoneAccess(caller, zone);
            // check that vm owner can create vm in the domain
            _configMgr.checkZoneAccess(owner, zone);
        }

        ServiceOfferingVO offering = _serviceOfferingDao.findById(serviceOffering.getId());

        // check if account/domain is with in resource limits to create a new vm
        boolean isIso = Storage.ImageFormat.ISO == template.getFormat();
        // For baremetal, size can be null
        Long tmp = _templateDao.findById(template.getId()).getSize();
        long size = 0;
        if (tmp != null) {
        	size = tmp;
        }
        if (diskOfferingId != null) {
            size += _diskOfferingDao.findById(diskOfferingId).getDiskSize();
        }
        resourceLimitCheck(owner, new Long(offering.getCpu()), new Long(offering.getRamSize()));
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.volume, (isIso
                || diskOfferingId == null ? 1 : 2));
        _resourceLimitMgr.checkResourceLimit(owner, ResourceType.primary_storage, new Long (size));

        // verify security group ids
        if (securityGroupIdList != null) {
            for (Long securityGroupId : securityGroupIdList) {
                SecurityGroup sg = _securityGroupDao.findById(securityGroupId);
                if (sg == null) {
                    throw new InvalidParameterValueException(
                            "Unable to find security group by id "
                                    + securityGroupId);
                } else {
                    // verify permissions
                    _accountMgr.checkAccess(caller, null, true, owner, sg);
                }
            }
        }

        // check that the affinity groups exist
        if (affinityGroupIdList != null) {
            for (Long affinityGroupId : affinityGroupIdList) {
                AffinityGroupVO ag = _affinityGroupDao.findById(affinityGroupId);
                if (ag == null) {
                    throw new InvalidParameterValueException("Unable to find affinity group by id " + affinityGroupId);
                } else {
                    // verify permissions
                    _accountMgr.checkAccess(caller, null, true, owner, ag);
                    // Root admin has access to both VM and AG by default, but
                    // make sure the owner of these entities is same
                    if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || _accountMgr.isRootAdmin(caller.getType())) {
                        if (ag.getAccountId() != owner.getAccountId()) {
                            throw new PermissionDeniedException("Affinity Group " + ag
                                    + " does not belong to the VM's account");
                        }
                    }
                }
            }
        }

        if (template.getHypervisorType() != null && template.getHypervisorType() != HypervisorType.BareMetal) {
            // check if we have available pools for vm deployment
            long availablePools = _storagePoolDao.countPoolsByStatus(StoragePoolStatus.Up);
            if (availablePools < 1) {
                throw new StorageUnavailableException("There are no available pools in the UP state for vm deployment", -1);
            }
        }

        if (template.getTemplateType().equals(TemplateType.SYSTEM)) {
            throw new InvalidParameterValueException(
                    "Unable to use system template " + template.getId()
                    + " to deploy a user vm");
        }
        List<VMTemplateZoneVO> listZoneTemplate = _templateZoneDao
                .listByZoneTemplate(zone.getId(), template.getId());
        if (listZoneTemplate == null || listZoneTemplate.isEmpty()) {
            throw new InvalidParameterValueException("The template "
                    + template.getId() + " is not available for use");
        }

        if (isIso && !template.isBootable()) {
            throw new InvalidParameterValueException(
                    "Installing from ISO requires an ISO that is bootable: "
                            + template.getId());
        }

        // Check templates permissions
        if (!template.isPublicTemplate()) {
            Account templateOwner = _accountMgr.getAccount(template
                    .getAccountId());
            _accountMgr.checkAccess(owner, null, true, templateOwner);
        }

        // check if the user data is correct
        validateUserData(userData, httpmethod);

        // Find an SSH public key corresponding to the key pair name, if one is
        // given
        String sshPublicKey = null;
        if (sshKeyPair != null && !sshKeyPair.equals("")) {
            SSHKeyPair pair = _sshKeyPairDao.findByName(owner.getAccountId(),
                    owner.getDomainId(), sshKeyPair);
            if (pair == null) {
                throw new InvalidParameterValueException(
                        "A key pair with name '" + sshKeyPair
                        + "' was not found.");
            }

            sshPublicKey = pair.getPublicKey();
        }

        List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>();

        Map<String, NicProfile> networkNicMap = new HashMap<String, NicProfile>();

        short defaultNetworkNumber = 0;
        boolean securityGroupEnabled = false;
        boolean vpcNetwork = false;
        for (NetworkVO network : networkList) {
            if (network.getDataCenterId() != zone.getId()) {
                throw new InvalidParameterValueException("Network id="
                        + network.getId() + " doesn't belong to zone "
                        + zone.getId());
            }

            IpAddresses requestedIpPair = null;
            if (requestedIps != null && !requestedIps.isEmpty()) {
                requestedIpPair = requestedIps.get(network.getId());
            }

            if (requestedIpPair == null) {
                requestedIpPair = new IpAddresses(null, null);
            } else {
                _networkModel.checkRequestedIpAddresses(network.getId(), requestedIpPair.getIp4Address(), requestedIpPair.getIp6Address());
            }

            NicProfile profile = new NicProfile(requestedIpPair.getIp4Address(), requestedIpPair.getIp6Address());

            if (defaultNetworkNumber == 0) {
                defaultNetworkNumber++;
                // if user requested specific ip for default network, add it
                if (defaultIps.getIp4Address() != null || defaultIps.getIp6Address() != null) {
                    _networkModel.checkRequestedIpAddresses(network.getId(), defaultIps.getIp4Address(), defaultIps.getIp6Address());
                    profile = new NicProfile(defaultIps.getIp4Address(), defaultIps.getIp6Address());
                }

                profile.setDefaultNic(true);
            }

            networks.add(new Pair<NetworkVO, NicProfile>(network, profile));

            if (_networkModel.isSecurityGroupSupportedInNetwork(network)) {
                securityGroupEnabled = true;
            }

            // vm can't be a part of more than 1 VPC network
            if (network.getVpcId() != null) {
                if (vpcNetwork) {
                    throw new InvalidParameterValueException(
                            "Vm can't be a part of more than 1 VPC network");
                }
                vpcNetwork = true;
            }

            networkNicMap.put(network.getUuid(), profile);
        }

        if (securityGroupIdList != null && !securityGroupIdList.isEmpty()
                && !securityGroupEnabled) {
            throw new InvalidParameterValueException(
                    "Unable to deploy vm with security groups as SecurityGroup service is not enabled for the vm's network");
        }

        // Verify network information - network default network has to be set;
        // and vm can't have more than one default network
        // This is a part of business logic because default network is required
        // by Agent Manager in order to configure default
        // gateway for the vm
        if (defaultNetworkNumber == 0) {
            throw new InvalidParameterValueException(
                    "At least 1 default network has to be specified for the vm");
        } else if (defaultNetworkNumber > 1) {
            throw new InvalidParameterValueException(
                    "Only 1 default network per vm is supported");
        }

        long id = _vmDao.getNextInSequence(Long.class, "id");

        String instanceName;
        if (_instanceNameFlag && displayName != null) {
            // Check if the displayName conforms to RFC standards.
            checkNameForRFCCompliance(displayName);
            instanceName = VirtualMachineName.getVmName(id, owner.getId(), displayName);
            if (instanceName.length() > MAX_VM_NAME_LEN) {
                throw new InvalidParameterValueException("Specified display name " + displayName + " causes VM name to exceed 80 characters in length");
            }
            // Search whether there is already an instance with the same instance name
            // that is not in the destroyed or expunging state.
            VMInstanceVO vm = _vmInstanceDao.findVMByInstanceName(instanceName);
            if (vm != null && vm.getState() != VirtualMachine.State.Expunging) {
                throw new InvalidParameterValueException("There already exists a VM by the display name supplied");
            }
        } else {
            instanceName = VirtualMachineName.getVmName(id, owner.getId(), _instance);
        }

        String uuidName = UUID.randomUUID().toString();

        // verify hostname information
        if (hostName == null) {
            hostName = uuidName;
        } else {
            //1) check is hostName is RFC compliant
            checkNameForRFCCompliance(hostName);
            // 2) hostName has to be unique in the network domain
            Map<String, List<Long>> ntwkDomains = new HashMap<String, List<Long>>();
            for (NetworkVO network : networkList) {
                String ntwkDomain = network.getNetworkDomain();
                if (!ntwkDomains.containsKey(ntwkDomain)) {
                    List<Long> ntwkIds = new ArrayList<Long>();
                    ntwkIds.add(network.getId());
                    ntwkDomains.put(ntwkDomain, ntwkIds);
                } else {
                    List<Long> ntwkIds = ntwkDomains.get(ntwkDomain);
                    ntwkIds.add(network.getId());
                    ntwkDomains.put(ntwkDomain, ntwkIds);
                }
            }

            for (String ntwkDomain : ntwkDomains.keySet()) {
                for (Long ntwkId : ntwkDomains.get(ntwkDomain)) {
                    // * get all vms hostNames in the network
                    List<String> hostNames = _vmInstanceDao
                            .listDistinctHostNames(ntwkId);
                    // * verify that there are no duplicates
                    if (hostNames.contains(hostName)) {
                        throw new InvalidParameterValueException("The vm with hostName " + hostName
                                + " already exists in the network domain: " + ntwkDomain + "; network="
                                + _networkModel.getNetwork(ntwkId));
                    }
                }
            }
        }

        HypervisorType hypervisorType = null;
        if (template == null || template.getHypervisorType() == null
                || template.getHypervisorType() == HypervisorType.None) {
            hypervisorType = hypervisor;
        } else {
            hypervisorType = template.getHypervisorType();
        }

        Transaction txn = Transaction.currentTxn();
        txn.start();
        UserVmVO vm = new UserVmVO(id, instanceName, displayName,
                template.getId(), hypervisorType, template.getGuestOSId(),
                offering.getOfferHA(), offering.getLimitCpuUse(),
                owner.getDomainId(), owner.getId(), offering.getId(), userData,
                hostName, diskOfferingId);
        vm.setUuid(uuidName);
        vm.setDetail(VirtualMachine.IsDynamicScalingEnabled, template.isDynamicallyScalable().toString());

        if (sshPublicKey != null) {
            vm.setDetail("SSH.PublicKey", sshPublicKey);
        }

        if (keyboard != null && !keyboard.isEmpty())
            vm.setDetail(VmDetailConstants.KEYBOARD, keyboard);

        if (isIso) {
            vm.setIsoId(template.getId());
        }

        if(isDisplayVmEnabled != null){
            if(!_accountMgr.isRootAdmin(caller.getType())){
                throw new PermissionDeniedException( "Cannot update parameter displayvm, only admin permitted ");
            }
            vm.setDisplayVm(isDisplayVmEnabled);
        }else {
            vm.setDisplayVm(true);
        }

        // If hypervisor is vSphere, check for clone type setting.
        if (hypervisorType.equals(HypervisorType.VMware)) {
            // retrieve clone flag.
            UserVmCloneType cloneType = UserVmCloneType.linked;
            String value = _configDao.getValue(Config.VmwareCreateFullClone.key());
            if (value != null) {
                if (Boolean.parseBoolean(value) == true)
                    cloneType = UserVmCloneType.full;
            }
            UserVmCloneSettingVO vmCloneSettingVO = new UserVmCloneSettingVO(id, cloneType.toString());
            _vmCloneSettingDao.persist(vmCloneSettingVO);
        }

        long guestOSId = template.getGuestOSId();
        GuestOSVO guestOS = _guestOSDao.findById(guestOSId);
        long guestOSCategoryId = guestOS.getCategoryId();
        GuestOSCategoryVO guestOSCategory = _guestOSCategoryDao.findById(guestOSCategoryId);


        // If hypervisor is vSphere and OS is OS X, set special settings.
        if (hypervisorType.equals(HypervisorType.VMware)) {
            if (guestOS.getDisplayName().toLowerCase().contains("apple mac os")){
                vm.setDetail("smc.present", "TRUE");
                vm.setDetail(VmDetailConstants.ROOK_DISK_CONTROLLER, "scsi");
                vm.setDetail("firmware", "efi");
                s_logger.info("guestOS is OSX : overwrite root disk controller to scsi, use smc and efi");
            }
       }

        _vmDao.persist(vm);
        _vmDao.saveDetails(vm);

        s_logger.debug("Allocating in the DB for vm");
        DataCenterDeployment plan = new DataCenterDeployment(zone.getId());

        List<String> computeTags = new ArrayList<String>();
        computeTags.add(offering.getHostTag());

        List<String> rootDiskTags =	new ArrayList<String>();
        rootDiskTags.add(offering.getTags());

        if(isIso){
            VirtualMachineEntity vmEntity = _orchSrvc.createVirtualMachineFromScratch(vm.getUuid(), new Long(owner.getAccountId()).toString(), vm.getIsoId().toString(), hostName, displayName, hypervisor.name(), guestOSCategory.getName(), offering.getCpu(), offering.getSpeed(), offering.getRamSize(), diskSize,  computeTags, rootDiskTags, networkNicMap, plan);
        }else {
            VirtualMachineEntity vmEntity = _orchSrvc.createVirtualMachine(vm.getUuid(), new Long(owner.getAccountId()).toString(), new Long(template.getId()).toString(), hostName, displayName, hypervisor.name(), offering.getCpu(),  offering.getSpeed(), offering.getRamSize(), diskSize, computeTags, rootDiskTags, networkNicMap, plan);
        }



        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Successfully allocated DB entry for " + vm);
        }
        UserContext.current().setEventDetails("Vm Id: " + vm.getId());

        UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VM_CREATE, accountId, zone.getId(), vm.getId(),
                vm.getHostName(), offering.getId(), template.getId(), hypervisorType.toString(),
                VirtualMachine.class.getName(), vm.getUuid());

        //Update Resource Count for the given account
        resourceCountIncrement(accountId, new Long(offering.getCpu()),
                new Long(offering.getRamSize()));

        txn.commit();

        // Assign instance to the group
        try {
            if (group != null) {
                boolean addToGroup = addInstanceToGroup(Long.valueOf(id), group);
                if (!addToGroup) {
                    throw new CloudRuntimeException(
                            "Unable to assign Vm to the group " + group);
                }
            }
        } catch (Exception ex) {
            throw new CloudRuntimeException("Unable to assign Vm to the group "
                    + group);
        }

        _securityGroupMgr.addInstanceToGroups(vm.getId(), securityGroupIdList);

        if (affinityGroupIdList != null && !affinityGroupIdList.isEmpty()) {
            _affinityGroupVMMapDao.updateMap(vm.getId(), affinityGroupIdList);
        }

        return vm;
    }

    private void validateUserData(String userData, HTTPMethod httpmethod) {
        byte[] decodedUserData = null;
        if (userData != null) {
            if (!Base64.isBase64(userData)) {
                throw new InvalidParameterValueException(
                        "User data is not base64 encoded");
            }
            // If GET, use 4K. If POST, support upto 32K.
            if (httpmethod.equals(HTTPMethod.GET)) {
                if (userData.length() >= MAX_HTTP_GET_LENGTH) {
                    throw new InvalidParameterValueException(
                            "User data is too long for an http GET request");
                }
                decodedUserData = Base64.decodeBase64(userData.getBytes());
                if (decodedUserData.length > MAX_HTTP_GET_LENGTH) {
                    throw new InvalidParameterValueException(
                        "User data is too long for GET request");
                }
            } else if (httpmethod.equals(HTTPMethod.POST)) {
                if (userData.length() >= MAX_HTTP_POST_LENGTH) {
                    throw new InvalidParameterValueException(
                            "User data is too long for an http POST request");
                }
                decodedUserData = Base64.decodeBase64(userData.getBytes());
                if (decodedUserData.length > MAX_HTTP_POST_LENGTH) {
                    throw new InvalidParameterValueException(
                        "User data is too long for POST request");
                }
            }

            if (decodedUserData.length < 1) {
                throw new InvalidParameterValueException(
                        "User data is too short");
            }
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_CREATE, eventDescription = "starting Vm", async = true)
    public UserVm startVirtualMachine(DeployVMCmd cmd)
            throws ResourceUnavailableException, InsufficientCapacityException,
            ConcurrentOperationException {
        return startVirtualMachine(cmd, null);
    }

    protected UserVm startVirtualMachine(DeployVMCmd cmd,
            Map<VirtualMachineProfile.Param, Object> additonalParams)
                    throws ResourceUnavailableException, InsufficientCapacityException,
                    ConcurrentOperationException {

        long vmId = cmd.getEntityId();
        Long hostId = cmd.getHostId();
        UserVmVO vm = _vmDao.findById(vmId);

        Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> vmParamPair = null;
        try {
            vmParamPair = startVirtualMachine(vmId, hostId, additonalParams);
            vm = vmParamPair.first();
        } finally {
            updateVmStateForFailedVmCreation(vm.getId(), hostId);
        }

        // Check that the password was passed in and is valid
        VMTemplateVO template = _templateDao.findByIdIncludingRemoved(vm
                .getTemplateId());
        if (template.getEnablePassword()) {
            // this value is not being sent to the backend; need only for api
            // display purposes
            vm.setPassword((String) vmParamPair.second().get(
                    VirtualMachineProfile.Param.VmPassword));
        }

        return vm;
    }

    @Override
    public boolean finalizeVirtualMachineProfile(
            VirtualMachineProfile<UserVmVO> profile, DeployDestination dest,
            ReservationContext context) {
        UserVmVO vm = profile.getVirtualMachine();
        Map<String, String> details = _vmDetailsDao.findDetails(vm.getId());
        vm.setDetails(details);

        if (vm.getIsoId() != null) {
            TemplateInfo template = this.templateMgr.prepareIso(vm.getIsoId(), vm.getDataCenterId());
            if (template == null){
                s_logger.error("Failed to prepare ISO on secondary or cache storage");
                throw new CloudRuntimeException("Failed to prepare ISO on secondary or cache storage");
            }
            if (template.isBootable()) {
                profile.setBootLoaderType(BootloaderType.CD);
            }

            GuestOSVO guestOS = _guestOSDao.findById(template.getGuestOSId());
            String displayName = null;
            if (guestOS != null) {
                displayName = guestOS.getDisplayName();
            }

            TemplateObjectTO iso = (TemplateObjectTO)template.getTO();
            iso.setGuestOsType(displayName);
            DiskTO disk = new DiskTO(iso, 3L, null, Volume.Type.ISO);
            profile.addDisk(disk);
        } else {
            TemplateObjectTO iso = new TemplateObjectTO();
            iso.setFormat(ImageFormat.ISO);
            DiskTO disk = new DiskTO(iso, 3L, null, Volume.Type.ISO);
            profile.addDisk(disk);
        }

        return true;
    }

    @Override
    public boolean setupVmForPvlan(boolean add, Long hostId, NicProfile nic) {
        if (!nic.getBroadCastUri().getScheme().equals("pvlan")) {
    		return false;
    	}
        String op = "add";
        if (!add) {
        	// "delete" would remove all the rules(if using ovs) related to this vm
        	op = "delete";
        }
        Network network = _networkDao.findById(nic.getNetworkId());
        Host host = _hostDao.findById(hostId);
        String networkTag = _networkModel.getNetworkTag(host.getHypervisorType(), network);
    	PvlanSetupCommand cmd = PvlanSetupCommand.createVmSetup(op, nic.getBroadCastUri(), networkTag, nic.getMacAddress());
        Answer answer = null;
        try {
            answer = _agentMgr.send(hostId, cmd);
        } catch (OperationTimedoutException e) {
            s_logger.warn("Timed Out", e);
            return false;
        } catch (AgentUnavailableException e) {
            s_logger.warn("Agent Unavailable ", e);
            return false;
        }

        boolean result = true;
        if (answer == null || !answer.getResult()) {
        	result = false;
        }
        return result;
    }

    @Override
    public boolean finalizeDeployment(Commands cmds,
            VirtualMachineProfile<UserVmVO> profile, DeployDestination dest,
            ReservationContext context) {
        UserVmVO userVm = profile.getVirtualMachine();
        List<NicVO> nics = _nicDao.listByVmId(userVm.getId());
        for (NicVO nic : nics) {
            NetworkVO network = _networkDao.findById(nic.getNetworkId());
            if (network.getTrafficType() == TrafficType.Guest
                    || network.getTrafficType() == TrafficType.Public) {
                userVm.setPrivateIpAddress(nic.getIp4Address());
                userVm.setPrivateMacAddress(nic.getMacAddress());
            }
        }

        List<VolumeVO> volumes = _volsDao.findByInstance(userVm.getId());
        VmDiskStatisticsVO diskstats = null;
        for (VolumeVO volume : volumes) {
               diskstats = _vmDiskStatsDao.findBy(userVm.getAccountId(), userVm.getDataCenterId(),userVm.getId(), volume.getId());
            if (diskstats == null) {
               diskstats = new VmDiskStatisticsVO(userVm.getAccountId(), userVm.getDataCenterId(),userVm.getId(), volume.getId());
               _vmDiskStatsDao.persist(diskstats);
            }
        }

        return true;
    }

    @Override
    public boolean finalizeCommandsOnStart(Commands cmds,
            VirtualMachineProfile<UserVmVO> profile) {
        return true;
    }

    @Override
    public boolean finalizeStart(VirtualMachineProfile<UserVmVO> profile,
            long hostId, Commands cmds, ReservationContext context) {
        UserVmVO vm = profile.getVirtualMachine();

        Answer[] answersToCmds = cmds.getAnswers();
        if (answersToCmds == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Returning from finalizeStart() since there are no answers to read");
            }
            return true;
        }
        Answer startAnswer = cmds.getAnswer(StartAnswer.class);
        String returnedIp = null;
        String originalIp = null;
        if (startAnswer != null) {
            StartAnswer startAns = (StartAnswer) startAnswer;
            VirtualMachineTO vmTO = startAns.getVirtualMachine();
            for (NicTO nicTO : vmTO.getNics()) {
                if (nicTO.getType() == TrafficType.Guest) {
                    returnedIp = nicTO.getIp();
                }
            }
        }

        List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        NicVO guestNic = null;
        NetworkVO guestNetwork = null;
        for (NicVO nic : nics) {
            NetworkVO network = _networkDao.findById(nic.getNetworkId());
            long isDefault = (nic.isDefaultNic()) ? 1 : 0;
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_NETWORK_OFFERING_ASSIGN, vm.getAccountId(),
                    vm.getDataCenterId(), vm.getId(), Long.toString(nic.getId()), network.getNetworkOfferingId(),
                    null, isDefault, VirtualMachine.class.getName(), vm.getUuid());
            if (network.getTrafficType() == TrafficType.Guest) {
                originalIp = nic.getIp4Address();
                guestNic = nic;
                guestNetwork = network;
                // In vmware, we will be effecting pvlan settings in portgroups in StartCommand.
                if (profile.getHypervisorType() != HypervisorType.VMware) {
                if (nic.getBroadcastUri().getScheme().equals("pvlan")) {
                	NicProfile nicProfile = new NicProfile(nic, network, nic.getBroadcastUri(), nic.getIsolationUri(), 0, false, "pvlan-nic");
                	if (!setupVmForPvlan(true, hostId, nicProfile)) {
                		return false;
                	}
                }
            }
        }
        }
        boolean ipChanged = false;
        if (originalIp != null && !originalIp.equalsIgnoreCase(returnedIp)) {
            if (returnedIp != null && guestNic != null) {
                guestNic.setIp4Address(returnedIp);
                ipChanged = true;
            }
        }
        if (returnedIp != null && !returnedIp.equalsIgnoreCase(originalIp)) {
            if (guestNic != null) {
                guestNic.setIp4Address(returnedIp);
                ipChanged = true;
            }
        }
        if (ipChanged) {
            DataCenterVO dc = _dcDao.findById(vm.getDataCenterId());
            UserVmVO userVm = profile.getVirtualMachine();
            // dc.getDhcpProvider().equalsIgnoreCase(Provider.ExternalDhcpServer.getName())
            if (_ntwkSrvcDao.canProviderSupportServiceInNetwork(
                    guestNetwork.getId(), Service.Dhcp,
                    Provider.ExternalDhcpServer)) {
                _nicDao.update(guestNic.getId(), guestNic);
                userVm.setPrivateIpAddress(guestNic.getIp4Address());
                _vmDao.update(userVm.getId(), userVm);

                s_logger.info("Detected that ip changed in the answer, updated nic in the db with new ip "
                        + returnedIp);
            }
        }

        // get system ip and create static nat rule for the vm
        try {
            _rulesMgr.getSystemIpAndEnableStaticNatForVm(
                    profile.getVirtualMachine(), false);
        } catch (Exception ex) {
            s_logger.warn(
                    "Failed to get system ip and enable static nat for the vm "
                            + profile.getVirtualMachine()
                            + " due to exception ", ex);
            return false;
        }

        return true;
    }

    @Override
    public void finalizeExpunge(UserVmVO vm) {
    }

    @Override
    public UserVmVO persist(UserVmVO vm) {
        return _vmDao.persist(vm);
    }

    @Override
    public UserVmVO findById(long id) {
        return _vmDao.findById(id);
    }

    @Override
    public UserVmVO findByName(String name) {
        if (!VirtualMachineName.isValidVmName(name)) {
            return null;
        }
        return findById(VirtualMachineName.getVmId(name));
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_STOP, eventDescription = "stopping Vm", async = true)
    public UserVm stopVirtualMachine(long vmId, boolean forced)
            throws ConcurrentOperationException {
        // Input validation
        Account caller = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();

        // if account is removed, return error
        if (caller != null && caller.getRemoved() != null) {
            throw new PermissionDeniedException("The account " + caller.getId()
                    + " is removed");
        }

        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + vmId);
        }

        UserVO user = _userDao.findById(userId);
        boolean status = false;
        try {
            VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());
            status = vmEntity.stop(new Long(userId).toString());
            if (status) {
               return _vmDao.findById(vmId);
            } else {
               return null;
            }
        } catch (ResourceUnavailableException e) {
            throw new CloudRuntimeException(
                    "Unable to contact the agent to stop the virtual machine "
                            + vm, e);
        } catch (CloudException e) {
            throw new CloudRuntimeException(
                    "Unable to contact the agent to stop the virtual machine "
                            + vm, e);
        }
    }

    @Override
    public void finalizeStop(VirtualMachineProfile<UserVmVO> profile,
            StopAnswer answer) {
        // release elastic IP here
        IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(profile.getId());
        if (ip != null && ip.getSystem()) {
            UserContext ctx = UserContext.current();
            try {
                long networkId = ip.getAssociatedWithNetworkId();
                Network guestNetwork = _networkDao.findById(networkId);
                NetworkOffering offering = _configMgr.getNetworkOffering(guestNetwork.getNetworkOfferingId());
                assert (offering.getAssociatePublicIP() == true) : "User VM should not have system owned public IP associated with it when offering configured not to associate public IP.";
                _rulesMgr.disableStaticNat(ip.getId(), ctx.getCaller(), ctx.getCallerUserId(), true);
            } catch (Exception ex) {
                s_logger.warn(
                        "Failed to disable static nat and release system ip "
                                + ip + " as a part of vm "
                                + profile.getVirtualMachine()
                                + " stop due to exception ", ex);
            }
        }

        VMInstanceVO vm = profile.getVirtualMachine();
        List<NicVO> nics = _nicDao.listByVmId(vm.getId());
        for (NicVO nic : nics) {
            NetworkVO network = _networkDao.findById(nic.getNetworkId());
            if (network.getTrafficType() == TrafficType.Guest) {
                if (nic.getBroadcastUri() != null && nic.getBroadcastUri().getScheme().equals("pvlan")) {
                	NicProfile nicProfile = new NicProfile(nic, network, nic.getBroadcastUri(), nic.getIsolationUri(), 0, false, "pvlan-nic");
                	setupVmForPvlan(false, vm.getHostId(), nicProfile);
                }
            }
        }
    }

    public String generateRandomPassword() {
        return PasswordGenerator.generateRandomPassword(6);
    }

    @Override
    public Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> startVirtualMachine(
            long vmId, Long hostId,
            Map<VirtualMachineProfile.Param, Object> additionalParams)
                    throws ConcurrentOperationException, ResourceUnavailableException,
                    InsufficientCapacityException {
        // Input validation
        Account callerAccount = UserContext.current().getCaller();
        UserVO callerUser = _userDao.findById(UserContext.current()
                .getCallerUserId());

        // if account is removed, return error
        if (callerAccount != null && callerAccount.getRemoved() != null) {
            throw new InvalidParameterValueException("The account "
                    + callerAccount.getId() + " is removed");
        }

        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException(
                    "unable to find a virtual machine with id " + vmId);
        }

        _accountMgr.checkAccess(callerAccount, null, true, vm);

        Account owner = _accountDao.findById(vm.getAccountId());

        if (owner == null) {
            throw new InvalidParameterValueException("The owner of " + vm
                    + " does not exist: " + vm.getAccountId());
        }

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of " + vm
                    + " is disabled: " + vm.getAccountId());
        }

        Host destinationHost = null;
        if (hostId != null) {
            Account account = UserContext.current().getCaller();
            if (!_accountService.isRootAdmin(account.getType())) {
                throw new PermissionDeniedException(
                        "Parameter hostid can only be specified by a Root Admin, permission denied");
            }
            destinationHost = _hostDao.findById(hostId);
            if (destinationHost == null) {
                throw new InvalidParameterValueException(
                        "Unable to find the host to deploy the VM, host id="
                                + hostId);
            }
        }

        // check if vm is security group enabled
        if (_securityGroupMgr.isVmSecurityGroupEnabled(vmId) && _securityGroupMgr.getSecurityGroupsForVm(vmId).isEmpty() && !_securityGroupMgr.isVmMappedToDefaultSecurityGroup(vmId) && _networkModel.canAddDefaultSecurityGroup()) {
            // if vm is not mapped to security group, create a mapping
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Vm "
                        + vm
                        + " is security group enabled, but not mapped to default security group; creating the mapping automatically");
            }

            SecurityGroup defaultSecurityGroup = _securityGroupMgr
                    .getDefaultSecurityGroup(vm.getAccountId());
            if (defaultSecurityGroup != null) {
                List<Long> groupList = new ArrayList<Long>();
                groupList.add(defaultSecurityGroup.getId());
                _securityGroupMgr.addInstanceToGroups(vmId, groupList);
            }
        }

        DataCenterDeployment plan = null;
        if (destinationHost != null) {
            s_logger.debug("Destination Host to deploy the VM is specified, specifying a deployment plan to deploy the VM");
            plan = new DataCenterDeployment(vm.getDataCenterId(),
                    destinationHost.getPodId(), destinationHost.getClusterId(),
                    destinationHost.getId(), null, null);
        }

        // Set parameters
        Map<VirtualMachineProfile.Param, Object> params = null;
        VMTemplateVO template = null;
        if (vm.isUpdateParameters()) {
            _vmDao.loadDetails(vm);
            // Check that the password was passed in and is valid
            template = _templateDao
                    .findByIdIncludingRemoved(vm.getTemplateId());

            String password = "saved_password";
            if (template.getEnablePassword()) {
                password = generateRandomPassword();
            }

            if (!validPassword(password)) {
                throw new InvalidParameterValueException(
                        "A valid password for this virtual machine was not provided.");
            }

            // Check if an SSH key pair was selected for the instance and if so
            // use it to encrypt & save the vm password
            encryptAndStorePassword(vm, password);

            params = new HashMap<VirtualMachineProfile.Param, Object>();
            if (additionalParams != null) {
                params.putAll(additionalParams);
            }
            params.put(VirtualMachineProfile.Param.VmPassword, password);
        }

        VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());

        // Get serviceOffering for Virtual Machine
        ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getServiceOfferingId());
        String plannerName = offering.getDeploymentPlanner();
        if (plannerName == null) {
            if (vm.getHypervisorType() == HypervisorType.BareMetal) {
                plannerName = "BareMetalPlanner";
            } else {
                plannerName = _configDao.getValue(Config.VmDeploymentPlanner.key());
            }
        }

        String reservationId = vmEntity.reserve(plannerName, plan, new ExcludeList(), new Long(callerUser.getId()).toString());
        vmEntity.deploy(reservationId, new Long(callerUser.getId()).toString(), params);

        Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> vmParamPair = new Pair(vm, params);
        if (vm != null && vm.isUpdateParameters()) {
            // this value is not being sent to the backend; need only for api
            // display purposes
            if (template.getEnablePassword()) {
                vm.setPassword((String) vmParamPair.second().get(VirtualMachineProfile.Param.VmPassword));
                vm.setUpdateParameters(false);
                _vmDao.update(vm.getId(), vm);
            }
        }

        return vmParamPair;
    }

    @Override
    public UserVm destroyVm(long vmId) throws ResourceUnavailableException,
    ConcurrentOperationException {
        Account caller = UserContext.current().getCaller();
        Long userId = UserContext.current().getCallerUserId();

        // Verify input parameters
        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null || vm.getRemoved() != null) {
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "Unable to find a virtual machine with specified vmId");
            throw ex;
        }

        if (vm.getState() == State.Destroyed
                || vm.getState() == State.Expunging) {
            s_logger.trace("Vm id=" + vmId + " is already destroyed");
            return vm;
        }

        _accountMgr.checkAccess(caller, null, true, vm);
        User userCaller = _userDao.findById(userId);

        boolean status;
        State vmState = vm.getState();

        try {
            VirtualMachineEntity vmEntity = _orchSrvc.getVirtualMachine(vm.getUuid());
            status = vmEntity.destroy(new Long(userId).toString());
        } catch (CloudException e) {
            CloudRuntimeException ex = new CloudRuntimeException(
                    "Unable to destroy with specified vmId", e);
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (status) {
            // Mark the account's volumes as destroyed
            List<VolumeVO> volumes = _volsDao.findByInstance(vmId);
            for (VolumeVO volume : volumes) {
                if (volume.getVolumeType().equals(Volume.Type.ROOT)) {
                    UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_DELETE, volume.getAccountId(),
                            volume.getDataCenterId(), volume.getId(), volume.getName(), Volume.class.getName(),
                            volume.getUuid());
                }
            }

            if (vmState != State.Error) {
                // Get serviceOffering for Virtual Machine
                ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getServiceOfferingId());

                //Update Resource Count for the given account
                resourceCountDecrement(vm.getAccountId(), new Long(offering.getCpu()),
                        new Long(offering.getRamSize()));
            }
            return _vmDao.findById(vmId);
        } else {
            CloudRuntimeException ex = new CloudRuntimeException(
                    "Failed to destroy vm with specified vmId");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

    }

    @Override
    public void collectVmDiskStatistics (UserVmVO userVm) {
        // support KVM only util 2013.06.25
        if (!userVm.getHypervisorType().equals(HypervisorType.KVM))
            return;        
    	// Collect vm disk statistics from host before stopping Vm
    	long hostId = userVm.getHostId();
    	List<String> vmNames = new ArrayList<String>();
    	vmNames.add(userVm.getInstanceName());
    	HostVO host = _hostDao.findById(hostId);

    	GetVmDiskStatsAnswer diskStatsAnswer = null;
    	try {
    		diskStatsAnswer = (GetVmDiskStatsAnswer) _agentMgr.easySend(hostId, new GetVmDiskStatsCommand(vmNames, host.getGuid(), host.getName()));
    	} catch (Exception e) {
            s_logger.warn("Error while collecting disk stats for vm: " + userVm.getHostName() + " from host: " + host.getName(), e);
            return;
        }
        if (diskStatsAnswer != null) {
            if (!diskStatsAnswer.getResult()) {
                s_logger.warn("Error while collecting disk stats vm: " + userVm.getHostName() + " from host: " + host.getName() + "; details: " + diskStatsAnswer.getDetails());
                return;
            }
            Transaction txn = Transaction.open(Transaction.CLOUD_DB);
            try {
                txn.start();
                HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsByName = diskStatsAnswer.getVmDiskStatsMap();
                List<VmDiskStatsEntry> vmDiskStats = vmDiskStatsByName.get(userVm.getInstanceName());

                if (vmDiskStats == null)
		    return;

	        for (VmDiskStatsEntry vmDiskStat:vmDiskStats) {
                    SearchCriteria<VolumeVO> sc_volume = _volsDao.createSearchCriteria();
                    sc_volume.addAnd("path", SearchCriteria.Op.EQ, vmDiskStat.getPath());
                    VolumeVO volume = _volsDao.search(sc_volume, null).get(0);
	            VmDiskStatisticsVO previousVmDiskStats = _vmDiskStatsDao.findBy(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());
	            VmDiskStatisticsVO vmDiskStat_lock = _vmDiskStatsDao.lock(userVm.getAccountId(), userVm.getDataCenterId(), userVm.getId(), volume.getId());

	                if ((vmDiskStat.getIORead() == 0) && (vmDiskStat.getIOWrite() == 0) && (vmDiskStat.getBytesRead() == 0) && (vmDiskStat.getBytesWrite() == 0)) {
	                    s_logger.debug("Read/Write of IO and Bytes are both 0. Not updating vm_disk_statistics");
	                    continue;
	                }

	                if (vmDiskStat_lock == null) {
	                    s_logger.warn("unable to find vm disk stats from host for account: " + userVm.getAccountId() + " with vmId: " + userVm.getId()+ " and volumeId:" + volume.getId());
	                    continue;
	                }

	                if (previousVmDiskStats != null
	                        && ((previousVmDiskStats.getCurrentIORead() != vmDiskStat_lock.getCurrentIORead())
	                        || ((previousVmDiskStats.getCurrentIOWrite() != vmDiskStat_lock.getCurrentIOWrite())
	                        || (previousVmDiskStats.getCurrentBytesRead() != vmDiskStat_lock.getCurrentBytesRead())
	    	                || (previousVmDiskStats.getCurrentBytesWrite() != vmDiskStat_lock.getCurrentBytesWrite())))) {
	                    s_logger.debug("vm disk stats changed from the time GetVmDiskStatsCommand was sent. " +
	                            "Ignoring current answer. Host: " + host.getName() + " . VM: " + vmDiskStat.getVmName() +
	                            " IO Read: " + vmDiskStat.getIORead() + " IO Write: " + vmDiskStat.getIOWrite() +
	                            " Bytes Read: " + vmDiskStat.getBytesRead() + " Bytes Write: " + vmDiskStat.getBytesWrite());
	                    continue;
	                }

	                if (vmDiskStat_lock.getCurrentIORead() > vmDiskStat.getIORead()) {
	                    if (s_logger.isDebugEnabled()) {
	                        s_logger.debug("Read # of IO that's less than the last one.  " +
	                                "Assuming something went wrong and persisting it. Host: " + host.getName() + " . VM: " + vmDiskStat.getVmName() +
	                                " Reported: " + vmDiskStat.getIORead() + " Stored: " + vmDiskStat_lock.getCurrentIORead());
	                    }
	                    vmDiskStat_lock.setNetIORead(vmDiskStat_lock.getNetIORead() + vmDiskStat_lock.getCurrentIORead());
	                }
	                vmDiskStat_lock.setCurrentIORead(vmDiskStat.getIORead());
	                if (vmDiskStat_lock.getCurrentIOWrite() > vmDiskStat.getIOWrite()) {
	                    if (s_logger.isDebugEnabled()) {
	                        s_logger.debug("Write # of IO that's less than the last one.  " +
	                                "Assuming something went wrong and persisting it. Host: " + host.getName() + " . VM: " + vmDiskStat.getVmName() +
	                                " Reported: " + vmDiskStat.getIOWrite() + " Stored: " + vmDiskStat_lock.getCurrentIOWrite());
	                    }
	                    vmDiskStat_lock.setNetIOWrite(vmDiskStat_lock.getNetIOWrite() + vmDiskStat_lock.getCurrentIOWrite());
	                }
	                vmDiskStat_lock.setCurrentIOWrite(vmDiskStat.getIOWrite());
	                if (vmDiskStat_lock.getCurrentBytesRead() > vmDiskStat.getBytesRead()) {
	                    if (s_logger.isDebugEnabled()) {
	                        s_logger.debug("Read # of Bytes that's less than the last one.  " +
	                                "Assuming something went wrong and persisting it. Host: " + host.getName() + " . VM: " + vmDiskStat.getVmName() +
	                                " Reported: " + vmDiskStat.getBytesRead() + " Stored: " + vmDiskStat_lock.getCurrentBytesRead());
	                    }
	                    vmDiskStat_lock.setNetBytesRead(vmDiskStat_lock.getNetBytesRead() + vmDiskStat_lock.getCurrentBytesRead());
	                }
	                vmDiskStat_lock.setCurrentBytesRead(vmDiskStat.getBytesRead());
	                if (vmDiskStat_lock.getCurrentBytesWrite() > vmDiskStat.getBytesWrite()) {
	                    if (s_logger.isDebugEnabled()) {
	                        s_logger.debug("Write # of Bytes that's less than the last one.  " +
	                                "Assuming something went wrong and persisting it. Host: " + host.getName() + " . VM: " + vmDiskStat.getVmName() +
	                                " Reported: " + vmDiskStat.getBytesWrite() + " Stored: " + vmDiskStat_lock.getCurrentBytesWrite());
	                    }
	                    vmDiskStat_lock.setNetBytesWrite(vmDiskStat_lock.getNetBytesWrite() + vmDiskStat_lock.getCurrentBytesWrite());
	                }
	                vmDiskStat_lock.setCurrentBytesWrite(vmDiskStat.getBytesWrite());

	                if (! _dailyOrHourly) {
	                    //update agg bytes
	                	vmDiskStat_lock.setAggIORead(vmDiskStat_lock.getNetIORead() + vmDiskStat_lock.getCurrentIORead());
	                	vmDiskStat_lock.setAggIOWrite(vmDiskStat_lock.getNetIOWrite() + vmDiskStat_lock.getCurrentIOWrite());
	                	vmDiskStat_lock.setAggBytesRead(vmDiskStat_lock.getNetBytesRead() + vmDiskStat_lock.getCurrentBytesRead());
	                	vmDiskStat_lock.setAggBytesWrite(vmDiskStat_lock.getNetBytesWrite() + vmDiskStat_lock.getCurrentBytesWrite());
	                }

	                _vmDiskStatsDao.update(vmDiskStat_lock.getId(), vmDiskStat_lock);
	        	}
	        	txn.commit();
            } catch (Exception e) {
                txn.rollback();
                s_logger.warn("Unable to update vm disk statistics for vm: " + userVm.getId() + " from host: " + hostId, e);
            } finally {
                txn.close();
            }
        }
    }



    @Override
    public Pair<List<UserVmJoinVO>, Integer> searchForUserVMs(Criteria c, Account caller, Long domainId, boolean isRecursive,
            List<Long> permittedAccounts, boolean listAll, ListProjectResourcesCriteria listProjectResourcesCriteria, Map<String, String> tags) {
        Filter searchFilter = new Filter(UserVmJoinVO.class, c.getOrderBy(), c.getAscending(), c.getOffset(), c.getLimit());

        //first search distinct vm id by using query criteria and pagination
        SearchBuilder<UserVmJoinVO> sb = _vmJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct ids
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        Object id = c.getCriteria(Criteria.ID);
        Object name = c.getCriteria(Criteria.NAME);
        Object state = c.getCriteria(Criteria.STATE);
        Object notState = c.getCriteria(Criteria.NOTSTATE);
        Object zone = c.getCriteria(Criteria.DATACENTERID);
        Object pod = c.getCriteria(Criteria.PODID);
        Object hostId = c.getCriteria(Criteria.HOSTID);
        Object hostName = c.getCriteria(Criteria.HOSTNAME);
        Object keyword = c.getCriteria(Criteria.KEYWORD);
        Object isAdmin = c.getCriteria(Criteria.ISADMIN);
        assert c.getCriteria(Criteria.IPADDRESS) == null : "We don't support search by ip address on VM any more.  If you see this assert, it means we have to find a different way to search by the nic table.";
        Object groupId = c.getCriteria(Criteria.GROUPID);
        Object networkId = c.getCriteria(Criteria.NETWORKID);
        Object hypervisor = c.getCriteria(Criteria.HYPERVISOR);
        Object storageId = c.getCriteria(Criteria.STORAGE_ID);
        Object templateId = c.getCriteria(Criteria.TEMPLATE_ID);
        Object isoId = c.getCriteria(Criteria.ISO_ID);
        Object vpcId = c.getCriteria(Criteria.VPC_ID);

        sb.and("displayName", sb.entity().getDisplayName(),
                SearchCriteria.Op.LIKE);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getHostName(), SearchCriteria.Op.LIKE);
        sb.and("stateEQ", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("stateNEQ", sb.entity().getState(), SearchCriteria.Op.NEQ);
        sb.and("stateNIN", sb.entity().getState(), SearchCriteria.Op.NIN);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("hypervisorType", sb.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        sb.and("hostIdEQ", sb.entity().getHostId(), SearchCriteria.Op.EQ);
        sb.and("hostName", sb.entity().getHostName(), SearchCriteria.Op.LIKE);
        sb.and("templateId", sb.entity().getTemplateId(), SearchCriteria.Op.EQ);
        sb.and("isoId", sb.entity().getIsoId(), SearchCriteria.Op.EQ);
        sb.and("instanceGroupId", sb.entity().getInstanceGroupId(), SearchCriteria.Op.EQ);

        if (groupId != null && (Long) groupId != -1) {
            sb.and("instanceGroupId", sb.entity().getInstanceGroupId(), SearchCriteria.Op.EQ);
        }

        if (tags != null && !tags.isEmpty()) {
            for (int count=0; count < tags.size(); count++) {
                sb.or().op("key" + String.valueOf(count), sb.entity().getTagKey(), SearchCriteria.Op.EQ);
                sb.and("value" + String.valueOf(count), sb.entity().getTagValue(), SearchCriteria.Op.EQ);
                sb.cp();
            }
        }

        if (networkId != null) {
            sb.and("networkId", sb.entity().getNetworkId(), SearchCriteria.Op.EQ);
        }

        if(vpcId != null && networkId == null){
            sb.and("vpcId", sb.entity().getVpcId(), SearchCriteria.Op.EQ);
        }

        if (storageId != null) {
            sb.and("poolId", sb.entity().getPoolId(), SearchCriteria.Op.EQ);
        }

        // populate the search criteria with the values passed in
        SearchCriteria<UserVmJoinVO> sc = sb.create();

        // building ACL condition
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        if (tags != null && !tags.isEmpty()) {
            int count = 0;
            for (String key : tags.keySet()) {
                sc.setParameters("key" + String.valueOf(count), key);
                sc.setParameters("value" + String.valueOf(count), tags.get(key));
                count++;
            }
        }

        if (groupId != null && (Long)groupId != -1) {
            sc.setParameters("instanceGroupId", groupId);
        }

        if (keyword != null) {
            SearchCriteria<UserVmJoinVO> ssc = _vmJoinDao.createSearchCriteria();
            ssc.addOr("displayName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("hostName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("instanceName", SearchCriteria.Op.LIKE, "%" + keyword
                    + "%");
            ssc.addOr("state", SearchCriteria.Op.EQ, keyword);

            sc.addAnd("displayName", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (templateId != null) {
            sc.setParameters("templateId", templateId);
        }

        if (isoId != null) {
            sc.setParameters("isoId", isoId);
        }

        if (networkId != null) {
            sc.setParameters("networkId", networkId);
        }

        if(vpcId != null && networkId == null){
            sc.setParameters("vpcId", vpcId);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }

        if (state != null) {
            if (notState != null && (Boolean) notState == true) {
                sc.setParameters("stateNEQ", state);
            } else {
                sc.setParameters("stateEQ", state);
            }
        }

        if (hypervisor != null) {
            sc.setParameters("hypervisorType", hypervisor);
        }

        // Don't show Destroyed and Expunging vms to the end user
        if ((isAdmin != null) && ((Boolean) isAdmin != true)) {
            sc.setParameters("stateNIN", "Destroyed", "Expunging");
        }

        if (zone != null) {
            sc.setParameters("dataCenterId", zone);
        }
        if (pod != null) {
            sc.setParameters("podId", pod);

            if (state == null) {
                sc.setParameters("stateNEQ", "Destroyed");
            }
        }

        if (hostId != null) {
            sc.setParameters("hostIdEQ", hostId);
        } else {
            if (hostName != null) {
                sc.setParameters("hostName", hostName);
            }
        }

        if (storageId != null) {
            sc.setParameters("poolId", storageId);
        }

        // search vm details by ids
        Pair<List<UserVmJoinVO>, Integer> uniqueVmPair =  _vmJoinDao.searchAndCount(sc, searchFilter);
        Integer count = uniqueVmPair.second();
        if ( count.intValue() == 0 ){
            // handle empty result cases
            return uniqueVmPair;
        }
        List<UserVmJoinVO> uniqueVms = uniqueVmPair.first();
        Long[] vmIds = new Long[uniqueVms.size()];
        int i = 0;
        for (UserVmJoinVO v : uniqueVms ){
            vmIds[i++] = v.getId();
        }
        List<UserVmJoinVO> vms = _vmJoinDao.searchByIds(vmIds);
        return new Pair<List<UserVmJoinVO>, Integer>(vms, count);
    }

    @Override
    public HypervisorType getHypervisorTypeOfUserVM(long vmId) {
        UserVmVO userVm = _vmDao.findById(vmId);
        if (userVm == null) {
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "unable to find a virtual machine with specified id");
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }

        return userVm.getHypervisorType();
    }

    @Override
    public UserVm createVirtualMachine(DeployVMCmd cmd)
            throws InsufficientCapacityException, ResourceUnavailableException,
            ConcurrentOperationException, StorageUnavailableException,
            ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserVm getUserVm(long vmId) {
        return _vmDao.findById(vmId);
    }

    @Override
    public VirtualMachine vmStorageMigration(Long vmId, StoragePool destPool) {
        // access check - only root admin can migrate VM
        Account caller = UserContext.current().getCaller();
        if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException(
                    "No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException(
                    "Unable to find the VM by id=" + vmId);
        }

        if (vm.getState() != State.Stopped) {
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "VM is not Stopped, unable to migrate the vm having the specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (vm.getType() != VirtualMachine.Type.User) {
            throw new InvalidParameterValueException(
                    "can only do storage migration on user vm");
        }

        List<VolumeVO> vols = _volsDao.findByInstance(vm.getId());
        if (vols.size() > 1) {
            throw new InvalidParameterValueException(
                    "Data disks attached to the vm, can not migrate. Need to dettach data disks at first");
        }

        HypervisorType destHypervisorType = _clusterDao.findById(
                destPool.getClusterId()).getHypervisorType();
        if (vm.getHypervisorType() != destHypervisorType) {
            throw new InvalidParameterValueException(
                    "hypervisor is not compatible: dest: "
                            + destHypervisorType.toString() + ", vm: "
                            + vm.getHypervisorType().toString());
        }
        VMInstanceVO migratedVm = _itMgr.storageMigration(vm, destPool);
        return migratedVm;

    }

    private boolean isVMUsingLocalStorage(VMInstanceVO vm) {
        boolean usesLocalStorage = false;
        ServiceOfferingVO svcOffering = _serviceOfferingDao.findById(vm
                .getServiceOfferingId());
        if (svcOffering.getUseLocalStorage()) {
            usesLocalStorage = true;
        } else {
            List<VolumeVO> volumes = _volsDao.findByInstanceAndType(vm.getId(),
                    Volume.Type.DATADISK);
            for (VolumeVO vol : volumes) {
                DiskOfferingVO diskOffering = _diskOfferingDao.findById(vol
                        .getDiskOfferingId());
                if (diskOffering.getUseLocalStorage()) {
                    usesLocalStorage = true;
                    break;
                }
            }
        }
        return usesLocalStorage;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MIGRATE, eventDescription = "migrating VM", async = true)
    public VirtualMachine migrateVirtualMachine(Long vmId, Host destinationHost)
            throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException, VirtualMachineMigrationException {
        // access check - only root admin can migrate VM
        Account caller = UserContext.current().getCaller();
        if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException(
                    "No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException(
                    "Unable to find the VM by id=" + vmId);
        }
        // business logic
        if (vm.getState() != State.Running) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not Running, unable to migrate the vm "
                        + vm);
            }
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "VM is not Running, unable to migrate the vm with specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
        if (!vm.getHypervisorType().equals(HypervisorType.XenServer)
                && !vm.getHypervisorType().equals(HypervisorType.VMware)
                && !vm.getHypervisorType().equals(HypervisorType.KVM)
                && !vm.getHypervisorType().equals(HypervisorType.Ovm)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(vm
                        + " is not XenServer/VMware/KVM/Ovm, cannot migrate this VM.");
            }
            throw new InvalidParameterValueException(
                    "Unsupported Hypervisor Type for VM migration, we support XenServer/VMware/KVM only");
        }

        if (isVMUsingLocalStorage(vm)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(vm
                        + " is using Local Storage, cannot migrate this VM.");
            }
            throw new InvalidParameterValueException(
                    "Unsupported operation, VM uses Local storage, cannot migrate");
        }

        // check if migrating to same host
        long srcHostId = vm.getHostId();
        if (destinationHost.getId() == srcHostId) {
            throw new InvalidParameterValueException(
                    "Cannot migrate VM, VM is already presnt on this host, please specify valid destination host to migrate the VM");
        }

        // check if host is UP
        if (destinationHost.getStatus() != org.apache.host.Status.Up
                || destinationHost.getResourceState() != ResourceState.Enabled) {
            throw new InvalidParameterValueException(
                    "Cannot migrate VM, destination host is not in correct state, has status: "
                            + destinationHost.getStatus() + ", state: "
                            + destinationHost.getResourceState());
        }

        HostVO srcHost = _hostDao.findById(srcHostId);
        HostVO destHost = _hostDao.findById(destinationHost.getId());
        //if srcHost is dedicated and destination Host is not
        if (checkIfHostIsDedicated(srcHost) && !checkIfHostIsDedicated(destHost)) {
            //raise an alert
            String msg = "VM is migrated on a non-dedicated host " + destinationHost.getName();
            _alertMgr.sendAlert(AlertManager.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);
        }
        //if srcHost is non dedicated but destination Host is.
        if (!checkIfHostIsDedicated(srcHost) && checkIfHostIsDedicated(destHost)) {
            //raise an alert
            String msg = "VM is migrated on a dedicated host " + destinationHost.getName();
            _alertMgr.sendAlert(AlertManager.ALERT_TYPE_USERVM, vm.getDataCenterId(), vm.getPodIdToDeployIn(), msg, msg);
        }

        // call to core process
        DataCenterVO dcVO = _dcDao.findById(destinationHost.getDataCenterId());
        HostPodVO pod = _podDao.findById(destinationHost.getPodId());
        Cluster cluster = _clusterDao.findById(destinationHost.getClusterId());
        DeployDestination dest = new DeployDestination(dcVO, pod, cluster,
                destinationHost);

        // check max guest vm limit for the destinationHost
        HostVO destinationHostVO = _hostDao.findById(destinationHost.getId());
        if (_capacityMgr.checkIfHostReachMaxGuestLimit(destinationHostVO)) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Host name: "
                        + destinationHost.getName()
                        + ", hostId: "
                        + destinationHost.getId()
                        + " already has max Running VMs(count includes system VMs), cannot migrate to this host");
            }
            throw new VirtualMachineMigrationException(
                    "Destination host, hostId: "
                            + destinationHost.getId()
                            + " already has max Running VMs(count includes system VMs), cannot migrate to this host");
        }

        UserVmVO uservm = _vmDao.findById(vmId);
        if (uservm != null) {
            collectVmDiskStatistics(uservm);
        }
        VMInstanceVO migratedVm = _itMgr.migrate(vm, srcHostId, dest);
        return migratedVm;
    }

    private boolean checkIfHostIsDedicated(HostVO host) {
        long hostId = host.getId();
        DedicatedResourceVO dedicatedHost = _dedicatedDao.findByHostId(hostId);
        DedicatedResourceVO dedicatedClusterOfHost = _dedicatedDao.findByClusterId(host.getClusterId());
        DedicatedResourceVO dedicatedPodOfHost = _dedicatedDao.findByPodId(host.getPodId());
        if(dedicatedHost != null || dedicatedClusterOfHost != null || dedicatedPodOfHost != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MIGRATE, eventDescription = "migrating VM", async = true)
    public VirtualMachine migrateVirtualMachineWithVolume(Long vmId, Host destinationHost,
            Map<String, String> volumeToPool) throws ResourceUnavailableException, ConcurrentOperationException,
            ManagementServerException, VirtualMachineMigrationException {
        // Access check - only root administrator can migrate VM.
        Account caller = UserContext.current().getCaller();
        if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Caller is not a root admin, permission denied to migrate the VM");
            }
            throw new PermissionDeniedException("No permission to migrate VM, Only Root Admin can migrate a VM!");
        }

        VMInstanceVO vm = _vmInstanceDao.findById(vmId);
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find the vm by id " + vmId);
        }

        if (vm.getState() != State.Running) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is not Running, unable to migrate the vm " + vm);
            }
            CloudRuntimeException ex = new CloudRuntimeException("VM is not Running, unable to migrate the vm with" +
                    " specified id");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        if (!vm.getHypervisorType().equals(HypervisorType.XenServer) &&
                !vm.getHypervisorType().equals(HypervisorType.VMware) &&
                !vm.getHypervisorType().equals(HypervisorType.KVM) &&
                !vm.getHypervisorType().equals(HypervisorType.Ovm)) {
            throw new InvalidParameterValueException("Unsupported hypervisor type for vm migration, we support" +
                    " XenServer/VMware/KVM only");
        }

        long srcHostId = vm.getHostId();
        Host srcHost = _resourceMgr.getHost(srcHostId);
        // Check if src and destination hosts are valid and migrating to same host
        if (destinationHost.getId() == srcHostId) {
            throw new InvalidParameterValueException("Cannot migrate VM, VM is already present on this host, please" +
                    " specify valid destination host to migrate the VM");
        }

        // Check if the source and destination hosts are of the same type and support storage motion.
        if (!(srcHost.getHypervisorType().equals(destinationHost.getHypervisorType()) &&
            srcHost.getHypervisorVersion().equals(destinationHost.getHypervisorVersion()))) {
            throw new CloudRuntimeException("The source and destination hosts are not of the same type and version. " +
                "Source hypervisor type and version: " + srcHost.getHypervisorType().toString() + " " +
                srcHost.getHypervisorVersion() + ", Destination hypervisor type and version: " +
                destinationHost.getHypervisorType().toString() + " " + destinationHost.getHypervisorVersion());
        }

        HypervisorCapabilitiesVO capabilities = _hypervisorCapabilitiesDao.findByHypervisorTypeAndVersion(
                srcHost.getHypervisorType(), srcHost.getHypervisorVersion());
        if (!capabilities.isStorageMotionSupported()) {
            throw new CloudRuntimeException("Migration with storage isn't supported on hypervisor " +
                    srcHost.getHypervisorType() + " of version " + srcHost.getHypervisorVersion());
        }

        // Check if destination host is up.
        if (destinationHost.getStatus() != org.apache.host.Status.Up ||
                destinationHost.getResourceState() != ResourceState.Enabled){
            throw new CloudRuntimeException("Cannot migrate VM, destination host is not in correct state, has " +
                    "status: " + destinationHost.getStatus() + ", state: " + destinationHost.getResourceState());
        }

        List<VolumeVO> vmVolumes = _volsDao.findUsableVolumesForInstance(vm.getId());
        Map<VolumeVO, StoragePoolVO> volToPoolObjectMap = new HashMap<VolumeVO, StoragePoolVO>();
        if (!isVMUsingLocalStorage(vm) && destinationHost.getClusterId().equals(srcHost.getClusterId())) {
            if (volumeToPool.isEmpty()) {
                // If the destination host is in the same cluster and volumes do not have to be migrated across pools
                // then fail the call. migrateVirtualMachine api should have been used.
                throw new InvalidParameterValueException("Migration of the vm " + vm + "from host " + srcHost +
                        " to destination host " + destinationHost + " doesn't involve migrating the volumes.");
            }
        }

        if (!volumeToPool.isEmpty()) {
            // Check if all the volumes and pools passed as parameters are valid.
            for (Map.Entry<String, String> entry : volumeToPool.entrySet()) {
                VolumeVO volume = _volsDao.findByUuid(entry.getKey());
                StoragePoolVO pool = _storagePoolDao.findByUuid(entry.getValue());
                if (volume == null) {
                    throw new InvalidParameterValueException("There is no volume present with the given id " +
                            entry.getKey());
                } else if (pool == null) {
                    throw new InvalidParameterValueException("There is no storage pool present with the given id " +
                            entry.getValue());
                } else {
                    // Verify the volume given belongs to the vm.
                    if (!vmVolumes.contains(volume)) {
                        throw new InvalidParameterValueException("There volume " + volume + " doesn't belong to " +
                                "the virtual machine "+ vm + " that has to be migrated");
                    }
                    volToPoolObjectMap.put(volume, pool);
                }
            }
        }

        // Check if all the volumes are in the correct state.
        for (VolumeVO volume : vmVolumes) {
            if (volume.getState() != Volume.State.Ready) {
                throw new CloudRuntimeException("Volume " + volume + " of the VM is not in Ready state. Cannot " +
                        "migrate the vm with its volumes.");
            }
        }

        // Check max guest vm limit for the destinationHost.
        HostVO destinationHostVO = _hostDao.findById(destinationHost.getId());
        if(_capacityMgr.checkIfHostReachMaxGuestLimit(destinationHostVO)){
            throw new VirtualMachineMigrationException("Host name: " + destinationHost.getName() + ", hostId: " +
                    destinationHost.getId() + " already has max running vms (count includes system VMs). Cannot" +
                    " migrate to this host");
        }

        VMInstanceVO migratedVm = _itMgr.migrateWithStorage(vm, srcHostId, destinationHost.getId(), volToPoolObjectMap);
        return migratedVm;
}

    @DB
    @Override
    @ActionEvent(eventType = EventTypes.EVENT_VM_MOVE, eventDescription = "move VM to another user", async = false)
    public UserVm moveVMToUser(AssignVMCmd cmd)
            throws ResourceAllocationException, ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        // VERIFICATIONS and VALIDATIONS

        // VV 1: verify the two users
        Account caller = UserContext.current().getCaller();
        if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN
                && caller.getType() != Account.ACCOUNT_TYPE_DOMAIN_ADMIN) { // only
            // root
            // admin
            // can
            // assign
            // VMs
            throw new InvalidParameterValueException(
                    "Only domain admins are allowed to assign VMs and not "
                            + caller.getType());
        }

        // get and check the valid VM
        UserVmVO vm = _vmDao.findById(cmd.getVmId());
        if (vm == null) {
            throw new InvalidParameterValueException(
                    "There is no vm by that id " + cmd.getVmId());
        } else if (vm.getState() == State.Running) { // VV 3: check if vm is
            // running
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("VM is Running, unable to move the vm " + vm);
            }
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "VM is Running, unable to move the vm with specified vmId");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        Account oldAccount = _accountService.getActiveAccountById(vm
                .getAccountId());
        if (oldAccount == null) {
            throw new InvalidParameterValueException("Invalid account for VM "
                    + vm.getAccountId() + " in domain.");
        }
        // don't allow to move the vm from the project
        if (oldAccount.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "Specified Vm id belongs to the project and can't be moved");
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }
        Account newAccount = _accountService.getActiveAccountByName(
                cmd.getAccountName(), cmd.getDomainId());
        if (newAccount == null
                || newAccount.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            throw new InvalidParameterValueException("Invalid accountid="
                    + cmd.getAccountName() + " in domain " + cmd.getDomainId());
        }

        if (newAccount.getState() == Account.State.disabled) {
            throw new InvalidParameterValueException("The new account owner "
                    + cmd.getAccountName() + " is disabled.");
        }

        //check caller has access to both the old and new account
        _accountMgr.checkAccess(caller, null, true, oldAccount);
        _accountMgr.checkAccess(caller, null, true, newAccount);

        // make sure the accounts are not same
        if (oldAccount.getAccountId() == newAccount.getAccountId()) {
            throw new InvalidParameterValueException(
                    "The new account is the same as the old account. Account id ="
                            + oldAccount.getAccountId());
        }

        // don't allow to move the vm if there are existing PF/LB/Static Nat
        // rules, or vm is assigned to static Nat ip
        List<PortForwardingRuleVO> pfrules = _portForwardingDao.listByVm(cmd
                .getVmId());
        if (pfrules != null && pfrules.size() > 0) {
            throw new InvalidParameterValueException(
                    "Remove the Port forwarding rules for this VM before assigning to another user.");
        }
        List<FirewallRuleVO> snrules = _rulesDao
                .listStaticNatByVmId(vm.getId());
        if (snrules != null && snrules.size() > 0) {
            throw new InvalidParameterValueException(
                    "Remove the StaticNat rules for this VM before assigning to another user.");
        }
        List<LoadBalancerVMMapVO> maps = _loadBalancerVMMapDao
                .listByInstanceId(vm.getId());
        if (maps != null && maps.size() > 0) {
            throw new InvalidParameterValueException(
                    "Remove the load balancing rules for this VM before assigning to another user.");
        }
        // check for one on one nat
        IPAddressVO ip = _ipAddressDao.findByAssociatedVmId(cmd.getVmId());
        if (ip != null) {
            if (ip.isOneToOneNat()) {
                throw new InvalidParameterValueException(
                        "Remove the one to one nat rule for this VM for ip "
                                + ip.toString());
            }
        }

        DataCenterVO zone = _dcDao.findById(vm.getDataCenterId());

        // Get serviceOffering and Volumes for Virtual Machine
        ServiceOfferingVO offering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getServiceOfferingId());
        List<VolumeVO> volumes = _volsDao.findByInstance(cmd.getVmId());

        //Remove vm from instance group
        removeInstanceFromInstanceGroup(cmd.getVmId());

        // VV 2: check if account/domain is with in resource limits to create a new vm
        resourceLimitCheck(newAccount, new Long(offering.getCpu()), new Long(offering.getRamSize()));

        // VV 3: check if volumes and primary storage space are with in resource limits
        _resourceLimitMgr.checkResourceLimit(newAccount, ResourceType.volume,
                _volsDao.findByInstance(cmd.getVmId()).size());
        Long totalVolumesSize = (long) 0;
        for (VolumeVO volume : volumes) {
            totalVolumesSize += volume.getSize();
        }
        _resourceLimitMgr.checkResourceLimit(newAccount, ResourceType.primary_storage, totalVolumesSize);

        // VV 4: Check if new owner can use the vm template
        VirtualMachineTemplate template = _templateDao.findById(vm
                .getTemplateId());
        if (!template.isPublicTemplate()) {
            Account templateOwner = _accountMgr.getAccount(template
                    .getAccountId());
            _accountMgr.checkAccess(newAccount, null, true, templateOwner);
        }

        // VV 5: check the new account can create vm in the domain
        DomainVO domain = _domainDao.findById(cmd.getDomainId());
        _accountMgr.checkAccess(newAccount, domain);

        Transaction txn = Transaction.currentTxn();
        txn.start();
        //generate destroy vm event for usage
        UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VM_DESTROY, vm.getAccountId(), vm.getDataCenterId(), vm.getId(),
                vm.getHostName(), vm.getServiceOfferingId(), vm.getTemplateId(), vm.getHypervisorType().toString(),
                VirtualMachine.class.getName(), vm.getUuid());

        // update resource counts for old account
        resourceCountDecrement(oldAccount.getAccountId(), new Long(offering.getCpu()),
                new Long(offering.getRamSize()));

        // OWNERSHIP STEP 1: update the vm owner
        vm.setAccountId(newAccount.getAccountId());
        vm.setDomainId(cmd.getDomainId());
        _vmDao.persist(vm);

        // OS 2: update volume
        for (VolumeVO volume : volumes) {
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_DELETE, volume.getAccountId(),
                    volume.getDataCenterId(), volume.getId(), volume.getName(), Volume.class.getName(), volume.getUuid());
            _resourceLimitMgr.decrementResourceCount(oldAccount.getAccountId(), ResourceType.volume);
            _resourceLimitMgr.decrementResourceCount(oldAccount.getAccountId(), ResourceType.primary_storage,
                    new Long(volume.getSize()));
            volume.setAccountId(newAccount.getAccountId());
            volume.setDomainId(newAccount.getDomainId());
            _volsDao.persist(volume);
            _resourceLimitMgr.incrementResourceCount(newAccount.getAccountId(), ResourceType.volume);
            _resourceLimitMgr.incrementResourceCount(newAccount.getAccountId(), ResourceType.primary_storage,
                    new Long(volume.getSize()));
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VOLUME_CREATE, volume.getAccountId(),
                    volume.getDataCenterId(), volume.getId(), volume.getName(),
                    volume.getDiskOfferingId(), volume.getTemplateId(), volume.getSize(), Volume.class.getName(),
                    volume.getUuid());
            //snapshots: mark these removed in db
            List<SnapshotVO> snapshots = _snapshotDao.listByVolumeIdIncludingRemoved(volume.getId());
            for (SnapshotVO snapshot: snapshots){
                _snapshotDao.remove(snapshot.getId());
            }
        }

        //update resource count of new account
        resourceCountIncrement(newAccount.getAccountId(), new Long(offering.getCpu()), new Long(offering.getRamSize()));

        //generate usage events to account for this change
        UsageEventUtils.publishUsageEvent(EventTypes.EVENT_VM_CREATE, vm.getAccountId(), vm.getDataCenterId(), vm.getId(),
                vm.getHostName(), vm.getServiceOfferingId(), vm.getTemplateId(), vm.getHypervisorType().toString(),
                VirtualMachine.class.getName(), vm.getUuid());

        txn.commit();

        VMInstanceVO vmoi = _itMgr.findByIdAndType(vm.getType(), vm.getId());
        VirtualMachineProfileImpl<VMInstanceVO> vmOldProfile = new VirtualMachineProfileImpl<VMInstanceVO>(
                vmoi);

        // OS 3: update the network
        List<Long> networkIdList = cmd.getNetworkIds();
        List<Long> securityGroupIdList = cmd.getSecurityGroupIdList();

        if (zone.getNetworkType() == NetworkType.Basic) {
            if (networkIdList != null && !networkIdList.isEmpty()) {
                throw new InvalidParameterValueException(
                        "Can't move vm with network Ids; this is a basic zone VM");
            }
            // cleanup the old security groups
            _securityGroupMgr.removeInstanceFromGroups(cmd.getVmId());
            // cleanup the network for the oldOwner
            _networkMgr.cleanupNics(vmOldProfile);
            _networkMgr.expungeNics(vmOldProfile);
            // security groups will be recreated for the new account, when the
            // VM is started
            List<NetworkVO> networkList = new ArrayList<NetworkVO>();

            // Get default guest network in Basic zone
            Network defaultNetwork = _networkModel.getExclusiveGuestNetwork(zone.getId());

            if (defaultNetwork == null) {
                throw new InvalidParameterValueException(
                        "Unable to find a default network to start a vm");
            } else {
                networkList.add(_networkDao.findById(defaultNetwork.getId()));
            }

            boolean isVmWare = (template.getHypervisorType() == HypervisorType.VMware);

            if (securityGroupIdList != null && isVmWare) {
                throw new InvalidParameterValueException("Security group feature is not supported for vmWare hypervisor");
            } else if (!isVmWare && _networkModel.isSecurityGroupSupportedInNetwork(defaultNetwork) && _networkModel.canAddDefaultSecurityGroup()) {
                if (securityGroupIdList == null) {
                    securityGroupIdList = new ArrayList<Long>();
                }
                SecurityGroup defaultGroup = _securityGroupMgr
                        .getDefaultSecurityGroup(newAccount.getId());
                if (defaultGroup != null) {
                    // check if security group id list already contains Default
                    // security group, and if not - add it
                    boolean defaultGroupPresent = false;
                    for (Long securityGroupId : securityGroupIdList) {
                        if (securityGroupId.longValue() == defaultGroup.getId()) {
                            defaultGroupPresent = true;
                            break;
                        }
                    }

                    if (!defaultGroupPresent) {
                        securityGroupIdList.add(defaultGroup.getId());
                    }

                } else {
                    // create default security group for the account
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Couldn't find default security group for the account "
                                + newAccount + " so creating a new one");
                    }
                    defaultGroup = _securityGroupMgr.createSecurityGroup(
                            SecurityGroupManager.DEFAULT_GROUP_NAME,
                            SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION,
                            newAccount.getDomainId(), newAccount.getId(),
                            newAccount.getAccountName());
                    securityGroupIdList.add(defaultGroup.getId());
                }
            }

            List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>();
            NicProfile profile = new NicProfile();
            profile.setDefaultNic(true);
            networks.add(new Pair<NetworkVO, NicProfile>(networkList.get(0),
                    profile));

            VMInstanceVO vmi = _itMgr.findByIdAndType(vm.getType(), vm.getId());
            VirtualMachineProfileImpl<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(
                    vmi);
            _networkMgr.allocate(vmProfile, networks);

            _securityGroupMgr.addInstanceToGroups(vm.getId(),
                    securityGroupIdList);

            s_logger.debug("AssignVM: Basic zone, adding security groups no "
                    + securityGroupIdList.size() + " to "
                    + vm.getInstanceName());
        } else {
            if (zone.isSecurityGroupEnabled())  {
                throw new InvalidParameterValueException(
                        "Not yet implemented for SecurityGroupEnabled advanced networks.");
            } else {
                if (securityGroupIdList != null
                        && !securityGroupIdList.isEmpty()) {
                    throw new InvalidParameterValueException(
                            "Can't move vm with security groups; security group feature is not enabled in this zone");
                }
                // cleanup the network for the oldOwner
                _networkMgr.cleanupNics(vmOldProfile);
                _networkMgr.expungeNics(vmOldProfile);

                Set<NetworkVO> applicableNetworks = new HashSet<NetworkVO>();

                if (networkIdList != null && !networkIdList.isEmpty()) {
                    // add any additional networks
                    for (Long networkId : networkIdList) {
                        NetworkVO network = _networkDao.findById(networkId);
                        if (network == null) {
                            InvalidParameterValueException ex = new InvalidParameterValueException(
                                    "Unable to find specified network id");
                            ex.addProxyObject(networkId.toString(), "networkId");
                            throw ex;
                        }

                        _networkModel.checkNetworkPermissions(newAccount, network);

                        // don't allow to use system networks
                        NetworkOffering networkOffering = _configMgr
                                .getNetworkOffering(network
                                        .getNetworkOfferingId());
                        if (networkOffering.isSystemOnly()) {
                            InvalidParameterValueException ex = new InvalidParameterValueException(
                                    "Specified Network id is system only and can't be used for vm deployment");
                            ex.addProxyObject(network.getUuid(), "networkId");
                            throw ex;
                        }
                        applicableNetworks.add(network);
                    }
                } else {
                    NetworkVO defaultNetwork = null;
                    List<NetworkOfferingVO> requiredOfferings = _networkOfferingDao
                            .listByAvailability(Availability.Required, false);
                    if (requiredOfferings.size() < 1) {
                        throw new InvalidParameterValueException(
                                "Unable to find network offering with availability="
                                        + Availability.Required
                                        + " to automatically create the network as a part of vm creation");
                    }
                    if (requiredOfferings.get(0).getState() == NetworkOffering.State.Enabled) {
                        // get Virtual networks
                        List<? extends Network> virtualNetworks = _networkModel.listNetworksForAccount(newAccount.getId(), zone.getId(), Network.GuestType.Isolated);
                        if (virtualNetworks.isEmpty()) {
                            long physicalNetworkId = _networkModel.findPhysicalNetworkId(zone.getId(), requiredOfferings.get(0).getTags(), requiredOfferings.get(0).getTrafficType());
                            // Validate physical network
                            PhysicalNetwork physicalNetwork = _physicalNetworkDao
                                    .findById(physicalNetworkId);
                            if (physicalNetwork == null) {
                                throw new InvalidParameterValueException("Unable to find physical network with id: "+physicalNetworkId   + " and tag: " +requiredOfferings.get(0).getTags());
                            }
                            s_logger.debug("Creating network for account " + newAccount + " from the network offering id=" +
                                    requiredOfferings.get(0).getId() + " as a part of deployVM process");
                            Network newNetwork = _networkMgr.createGuestNetwork(requiredOfferings.get(0).getId(),
                                    newAccount.getAccountName() + "-network", newAccount.getAccountName() + "-network", null, null,
                                    null, null, newAccount, null, physicalNetwork, zone.getId(), ACLType.Account, null, null, null, null, true, null);
                            // if the network offering has persistent set to true, implement the network
                            if (requiredOfferings.get(0).getIsPersistent()) {
                                DeployDestination dest = new DeployDestination(zone, null, null, null);
                                UserVO callerUser = _userDao.findById(UserContext.current().getCallerUserId());
                                Journal journal = new Journal.LogJournal("Implementing " + newNetwork, s_logger);
                                ReservationContext context = new ReservationContextImpl(UUID.randomUUID().toString(),
                                        journal, callerUser, caller);
                                s_logger.debug("Implementing the network for account" + newNetwork + " as a part of" +
                                        " network provision for persistent networks");
                                try {
                                    Pair<NetworkGuru, NetworkVO> implementedNetwork = _networkMgr.implementNetwork(newNetwork.getId(), dest, context);
                                    if (implementedNetwork.first() == null) {
                                        s_logger.warn("Failed to implement the network " + newNetwork);
                                    }
                                    newNetwork = implementedNetwork.second();
                                } catch (Exception ex) {
                                    s_logger.warn("Failed to implement network " + newNetwork + " elements and" +
                                            " resources as a part of network provision for persistent network due to ", ex);
                                    CloudRuntimeException e = new CloudRuntimeException("Failed to implement network" +
                                            " (with specified id) elements and resources as a part of network provision");
                                    e.addProxyObject(newNetwork.getUuid(), "networkId");
                                    throw e;
                                }
                            }
                            defaultNetwork = _networkDao.findById(newNetwork.getId());
                        } else if (virtualNetworks.size() > 1) {
                            throw new InvalidParameterValueException(
                                    "More than 1 default Isolated networks are found "
                                            + "for account " + newAccount
                                            + "; please specify networkIds");
                        } else {
                            defaultNetwork = _networkDao.findById(virtualNetworks.get(0).getId());
                        }
                    } else {
                        throw new InvalidParameterValueException(
                                "Required network offering id="
                                        + requiredOfferings.get(0).getId()
                                        + " is not in "
                                        + NetworkOffering.State.Enabled);
                    }

                    applicableNetworks.add(defaultNetwork);
                }

                // add the new nics
                List<Pair<NetworkVO, NicProfile>> networks = new ArrayList<Pair<NetworkVO, NicProfile>>();
                int toggle = 0;
                for (NetworkVO appNet : applicableNetworks) {
                    NicProfile defaultNic = new NicProfile();
                    if (toggle == 0) {
                        defaultNic.setDefaultNic(true);
                        toggle++;
                    }
                    networks.add(new Pair<NetworkVO, NicProfile>(appNet,
                            defaultNic));
                }
                VMInstanceVO vmi = _itMgr.findByIdAndType(vm.getType(),
                        vm.getId());
                VirtualMachineProfileImpl<VMInstanceVO> vmProfile = new VirtualMachineProfileImpl<VMInstanceVO>(
                        vmi);
                _networkMgr.allocate(vmProfile, networks);
                s_logger.debug("AssignVM: Advance virtual, adding networks no "
                        + networks.size() + " to " + vm.getInstanceName());
            } // END IF NON SEC GRP ENABLED
        } // END IF ADVANCED
        s_logger.info("AssignVM: vm " + vm.getInstanceName()
                + " now belongs to account " + cmd.getAccountName());
        return vm;
    }

    @Override
    public UserVm restoreVM(RestoreVMCmd cmd) throws InsufficientCapacityException, ResourceUnavailableException {
        // Input validation
        Account caller = UserContext.current().getCaller();

        long vmId = cmd.getVmId();
        Long newTemplateId = cmd.getTemplateId();

        UserVmVO vm = _vmDao.findById(vmId);
        if (vm == null) {
            InvalidParameterValueException ex = new InvalidParameterValueException("Cannot find VM with ID " + vmId);
            ex.addProxyObject(String.valueOf(vmId), "vmId");
            throw ex;
        }

        _accountMgr.checkAccess(caller, null, true, vm);

        return restoreVMInternal(caller, vm, newTemplateId);
    }

    public UserVm restoreVMInternal(Account caller, UserVmVO vm, Long newTemplateId) throws InsufficientCapacityException, ResourceUnavailableException {

        Long userId = caller.getId();
        Account owner = _accountDao.findById(vm.getAccountId());
        UserVO user = _userDao.findById(userId);
        long vmId = vm.getId();
        boolean needRestart = false;

        // Input validation
        if (owner == null) {
            throw new InvalidParameterValueException("The owner of " + vm
                    + " does not exist: " + vm.getAccountId());
        }

        if (owner.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of " + vm
                    + " is disabled: " + vm.getAccountId());
        }

        if (vm.getState() != VirtualMachine.State.Running
                && vm.getState() != VirtualMachine.State.Stopped) {
            throw new CloudRuntimeException(
                    "Vm "
                            + vm.getUuid()
                            + " currently in "
                            + vm.getState()
                            + " state, restore vm can only execute when VM in Running or Stopped");
        }

        if (vm.getState() == VirtualMachine.State.Running) {
            needRestart = true;
        }

        List<VolumeVO> rootVols = _volsDao.findByInstanceAndType(vmId, Volume.Type.ROOT);
        if (rootVols.isEmpty()) {
            InvalidParameterValueException ex = new InvalidParameterValueException(
                    "Can not find root volume for VM " + vm.getUuid());
            ex.addProxyObject(vm.getUuid(), "vmId");
            throw ex;
        }

        VolumeVO root = rootVols.get(0);
        Long templateId = root.getTemplateId();
        boolean isISO = false;
        if(templateId == null) {
        // Assuming that for a vm deployed using ISO, template ID is set to NULL
            isISO = true;
            templateId = vm.getIsoId();
        }

        VMTemplateVO template = null;
        //newTemplateId can be either template or ISO id. In the following snippet based on the vm deployment (from template or ISO) it is handled accordingly
        if(newTemplateId != null) {
            template = _templateDao.findById(newTemplateId);
            _accountMgr.checkAccess(caller, null, true, template);
            if (isISO) {
                if (!template.getFormat().equals(ImageFormat.ISO)) {
                    throw new InvalidParameterValueException("Invalid ISO id provided to restore the VM ");
                }
            } else {
                if (template.getFormat().equals(ImageFormat.ISO)) {
                    throw new InvalidParameterValueException("Invalid template id provided to restore the VM ");
                }
            }
        } else {
            if (isISO && templateId == null) {
                throw new CloudRuntimeException("Cannot restore the VM since there is no ISO attached to VM");
            }
            template = _templateDao.findById(templateId);
            if (template == null) {
                InvalidParameterValueException ex = new InvalidParameterValueException(
                        "Cannot find template/ISO for specified volumeid and vmId");
                ex.addProxyObject(vm.getUuid(), "vmId");
                ex.addProxyObject(root.getUuid(), "volumeId");
                throw ex;
            }
        }

        if (needRestart) {
            try {
                _itMgr.stop(vm, user, caller);
            } catch (ResourceUnavailableException e) {
                s_logger.debug("Stop vm " + vm.getUuid() + " failed", e);
                CloudRuntimeException ex = new CloudRuntimeException(
                        "Stop vm failed for specified vmId");
                ex.addProxyObject(vm.getUuid(), "vmId");
                throw ex;
            }
        }

        /* If new template/ISO is provided allocate a new volume from new template/ISO otherwise allocate new volume from original template/ISO */
        VolumeVO newVol = null;
        if (newTemplateId != null) {
            if (isISO) {
                newVol = volumeMgr.allocateDuplicateVolume(root, null);
                vm.setIsoId(newTemplateId);
                vm.setGuestOSId(template.getGuestOSId());
                vm.setTemplateId(newTemplateId);
                _vmDao.update(vmId, vm);
            } else {
            newVol = volumeMgr.allocateDuplicateVolume(root, newTemplateId);
            vm.setGuestOSId(template.getGuestOSId());
            vm.setTemplateId(newTemplateId);
            _vmDao.update(vmId, vm);
            }
        } else {
            newVol = volumeMgr.allocateDuplicateVolume(root, null);
        }

        _volsDao.attachVolume(newVol.getId(), vmId, newVol.getDeviceId());

        /* Detach and destory the old root volume */

        _volsDao.detachVolume(root.getId());
        volumeMgr.destroyVolume(root);

        if (template.getEnablePassword()) {
            String password = generateRandomPassword();
            boolean result = resetVMPasswordInternal(vmId, password);
            if (result) {
                vm.setPassword(password);
                _vmDao.loadDetails(vm);
                // update the password in vm_details table too
                // Check if an SSH key pair was selected for the instance and if so
                // use it to encrypt & save the vm password
                encryptAndStorePassword(vm, password);
            } else {
                throw new CloudRuntimeException("VM reset is completed but failed to reset password for the virtual machine ");
            }
        }

        if (needRestart) {
            try {
                _itMgr.start(vm, null, user, caller);
            } catch (Exception e) {
                s_logger.debug("Unable to start VM " + vm.getUuid(), e);
                CloudRuntimeException ex = new CloudRuntimeException(
                        "Unable to start VM with specified id" + e.getMessage());
                ex.addProxyObject(vm.getUuid(), "vmId");
                throw ex;
            }
        }

        s_logger.debug("Restore VM " + vmId + " with template "
                + template.getUuid() + " done successfully");
        return vm;

    }

    @Override
    public boolean plugNic(Network network, NicTO nic, VirtualMachineTO vm,
            ReservationContext context, DeployDestination dest)
                    throws ConcurrentOperationException, ResourceUnavailableException,
                    InsufficientCapacityException {
        UserVmVO vmVO = _vmDao.findById(vm.getId());
        if (vmVO.getState() == State.Running) {
            try {
                PlugNicCommand plugNicCmd = new PlugNicCommand(nic,vm.getName(), vm.getType());
                Commands cmds = new Commands(OnError.Stop);
                cmds.addCommand("plugnic",plugNicCmd);
                _agentMgr.send(dest.getHost().getId(),cmds);
                PlugNicAnswer plugNicAnswer = cmds.getAnswer(PlugNicAnswer.class);
                if (!(plugNicAnswer != null && plugNicAnswer.getResult())) {
                    s_logger.warn("Unable to plug nic for " + vmVO + " due to: " + " due to: " + plugNicAnswer.getDetails());
                    return false;
                }
            } catch (OperationTimedoutException e) {
                throw new AgentUnavailableException("Unable to plug nic for " + vmVO + " in network " + network, dest.getHost().getId(), e);
            }
        } else if (vmVO.getState() == State.Stopped || vmVO.getState() == State.Stopping) {
            s_logger.warn(vmVO + " is Stopped, not sending PlugNicCommand.  Currently " + vmVO.getState());
        } else {
            s_logger.warn("Unable to plug nic, " + vmVO + " is not in the right state " + vmVO.getState());
            throw new ResourceUnavailableException("Unable to plug nic on the backend," +
                    vmVO + " is not in the right state", DataCenter.class, vmVO.getDataCenterId());
        }
        return true;
    }

    @Override
    public boolean unplugNic(Network network, NicTO nic, VirtualMachineTO vm,
            ReservationContext context, DeployDestination dest) throws ConcurrentOperationException, ResourceUnavailableException {
        UserVmVO vmVO = _vmDao.findById(vm.getId());
        if (vmVO.getState() == State.Running) {
            try {
                UnPlugNicCommand unplugNicCmd = new UnPlugNicCommand(nic,vm.getName());
                Commands cmds = new Commands(OnError.Stop);
                cmds.addCommand("unplugnic",unplugNicCmd);
                _agentMgr.send(dest.getHost().getId(),cmds);
                UnPlugNicAnswer unplugNicAnswer = cmds.getAnswer(UnPlugNicAnswer.class);
                if (!(unplugNicAnswer != null && unplugNicAnswer.getResult())) {
                    s_logger.warn("Unable to unplug nic for " + vmVO + " due to: " + unplugNicAnswer.getDetails());
                    return false;
                }
            } catch (OperationTimedoutException e) {
                throw new AgentUnavailableException("Unable to unplug nic for " + vmVO + " in network " + network, dest.getHost().getId(), e);
            }
        } else if (vmVO.getState() == State.Stopped || vmVO.getState() == State.Stopping) {
            s_logger.warn(vmVO + " is Stopped, not sending UnPlugNicCommand.  Currently " + vmVO.getState());
        } else {
            s_logger.warn("Unable to unplug nic, " + vmVO + " is not in the right state " + vmVO.getState());
            throw new ResourceUnavailableException("Unable to unplug nic on the backend," +
                    vmVO + " is not in the right state", DataCenter.class, vmVO.getDataCenterId());
        }
        return true;
    }

    @Override
    public void prepareStop(VirtualMachineProfile<UserVmVO> profile) {
        UserVmVO vm = _vmDao.findById(profile.getId());
        if (vm.getState() == State.Running)
            collectVmDiskStatistics(vm);
    }
    
    private void encryptAndStorePassword(UserVmVO vm, String password) {
        String sshPublicKey = vm.getDetail("SSH.PublicKey");
        if (sshPublicKey != null && !sshPublicKey.equals("")
                && password != null && !password.equals("saved_password")) {
            if (!sshPublicKey.startsWith("ssh-rsa")) {
                s_logger.warn("Only RSA public keys can be used to encrypt a vm password.");
                return;
            }
            String encryptedPasswd = RSAHelper.encryptWithSSHPublicKey(
                    sshPublicKey, password);
            if (encryptedPasswd == null) {
                throw new CloudRuntimeException("Error encrypting password");
            }

            vm.setDetail("Encrypted.Password", encryptedPasswd);
            _vmDao.saveDetails(vm);
        }
    }

}