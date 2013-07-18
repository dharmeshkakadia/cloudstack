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
package org.apache.cloudstack.api;

import org.apache.async.AsyncJob;
import org.apache.capacity.Capacity;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.ApiConstants.HostDetails;
import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.api.command.user.job.QueryAsyncJobResultCmd;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ApplicationLoadBalancerResponse;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.cloudstack.api.response.AutoScalePolicyResponse;
import org.apache.cloudstack.api.response.AutoScaleVmGroupResponse;
import org.apache.cloudstack.api.response.AutoScaleVmProfileResponse;
import org.apache.cloudstack.api.response.CapacityResponse;
import org.apache.cloudstack.api.response.ClusterResponse;
import org.apache.cloudstack.api.response.ConditionResponse;
import org.apache.cloudstack.api.response.ConfigurationResponse;
import org.apache.cloudstack.api.response.CounterResponse;
import org.apache.cloudstack.api.response.CreateCmdResponse;
import org.apache.cloudstack.api.response.DiskOfferingResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.DomainRouterResponse;
import org.apache.cloudstack.api.response.EventResponse;
import org.apache.cloudstack.api.response.ExtractResponse;
import org.apache.cloudstack.api.response.FirewallResponse;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.GlobalLoadBalancerResponse;
import org.apache.cloudstack.api.response.GuestOSResponse;
import org.apache.cloudstack.api.response.GuestVlanRangeResponse;
import org.apache.cloudstack.api.response.HostForMigrationResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.HypervisorCapabilitiesResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.ImageStoreResponse;
import org.apache.cloudstack.api.response.InstanceGroupResponse;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.api.response.IpForwardingRuleResponse;
import org.apache.cloudstack.api.response.IsolationMethodResponse;
import org.apache.cloudstack.api.response.LBHealthCheckResponse;
import org.apache.cloudstack.api.response.LBStickinessResponse;
import org.apache.cloudstack.api.response.LDAPConfigResponse;
import org.apache.cloudstack.api.response.LoadBalancerResponse;
import org.apache.cloudstack.api.response.NetworkACLItemResponse;
import org.apache.cloudstack.api.response.NetworkACLResponse;
import org.apache.cloudstack.api.response.NetworkOfferingResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.NicResponse;
import org.apache.cloudstack.api.response.NicSecondaryIpResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.PortableIpRangeResponse;
import org.apache.cloudstack.api.response.PortableIpResponse;
import org.apache.cloudstack.api.response.PrivateGatewayResponse;
import org.apache.cloudstack.api.response.ProjectAccountResponse;
import org.apache.cloudstack.api.response.ProjectInvitationResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.api.response.RegionResponse;
import org.apache.cloudstack.api.response.RemoteAccessVpnResponse;
import org.apache.cloudstack.api.response.ResourceCountResponse;
import org.apache.cloudstack.api.response.ResourceLimitResponse;
import org.apache.cloudstack.api.response.ResourceTagResponse;
import org.apache.cloudstack.api.response.S3Response;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.ServiceResponse;
import org.apache.cloudstack.api.response.Site2SiteCustomerGatewayResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnConnectionResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnGatewayResponse;
import org.apache.cloudstack.api.response.SnapshotPolicyResponse;
import org.apache.cloudstack.api.response.SnapshotResponse;
import org.apache.cloudstack.api.response.SnapshotScheduleResponse;
import org.apache.cloudstack.api.response.StaticRouteResponse;
import org.apache.cloudstack.api.response.StorageNetworkIpRangeResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.SwiftResponse;
import org.apache.cloudstack.api.response.SystemVmInstanceResponse;
import org.apache.cloudstack.api.response.SystemVmResponse;
import org.apache.cloudstack.api.response.TemplatePermissionsResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.TrafficMonitorResponse;
import org.apache.cloudstack.api.response.TrafficTypeResponse;
import org.apache.cloudstack.api.response.UsageRecordResponse;
import org.apache.cloudstack.api.response.UserResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;
import org.apache.cloudstack.api.response.VirtualRouterProviderResponse;
import org.apache.cloudstack.api.response.VlanIpRangeResponse;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.api.response.VpcOfferingResponse;
import org.apache.cloudstack.api.response.VpcResponse;
import org.apache.cloudstack.api.response.VpnUsersResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.network.lb.ApplicationLoadBalancerRule;
import org.apache.cloudstack.region.PortableIp;
import org.apache.cloudstack.region.PortableIpRange;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.usage.Usage;
import org.apache.configuration.Configuration;
import org.apache.configuration.ResourceCount;
import org.apache.configuration.ResourceLimit;
import org.apache.dc.DataCenter;
import org.apache.dc.Pod;
import org.apache.dc.StorageNetworkIpRange;
import org.apache.dc.Vlan;
import org.apache.domain.Domain;
import org.apache.event.Event;
import org.apache.host.Host;
import org.apache.hypervisor.HypervisorCapabilities;
import org.apache.network.GuestVlan;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.PhysicalNetwork;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.PhysicalNetworkTrafficType;
import org.apache.network.RemoteAccessVpn;
import org.apache.network.Site2SiteCustomerGateway;
import org.apache.network.Site2SiteVpnConnection;
import org.apache.network.Site2SiteVpnGateway;
import org.apache.network.VirtualRouterProvider;
import org.apache.network.VpnUser;
import org.apache.network.Network.Service;
import org.apache.network.Networks.IsolationType;
import org.apache.network.as.AutoScalePolicy;
import org.apache.network.as.AutoScaleVmGroup;
import org.apache.network.as.AutoScaleVmProfile;
import org.apache.network.as.Condition;
import org.apache.network.as.Counter;
import org.apache.network.router.VirtualRouter;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.HealthCheckPolicy;
import org.apache.network.rules.LoadBalancer;
import org.apache.network.rules.PortForwardingRule;
import org.apache.network.rules.StaticNatRule;
import org.apache.network.rules.StickinessPolicy;
import org.apache.network.security.SecurityGroup;
import org.apache.network.security.SecurityRule;
import org.apache.network.vpc.NetworkACL;
import org.apache.network.vpc.NetworkACLItem;
import org.apache.network.vpc.PrivateGateway;
import org.apache.network.vpc.StaticRoute;
import org.apache.network.vpc.Vpc;
import org.apache.network.vpc.VpcOffering;
import org.apache.offering.DiskOffering;
import org.apache.offering.NetworkOffering;
import org.apache.offering.ServiceOffering;
import org.apache.org.Cluster;
import org.apache.projects.Project;
import org.apache.projects.ProjectAccount;
import org.apache.projects.ProjectInvitation;
import org.apache.region.ha.GlobalLoadBalancerRule;
import org.apache.server.ResourceTag;
import org.apache.storage.GuestOS;
import org.apache.storage.ImageStore;
import org.apache.storage.S3;
import org.apache.storage.Snapshot;
import org.apache.storage.StoragePool;
import org.apache.storage.Swift;
import org.apache.storage.Volume;
import org.apache.storage.snapshot.SnapshotPolicy;
import org.apache.storage.snapshot.SnapshotSchedule;
import org.apache.template.VirtualMachineTemplate;
import org.apache.user.Account;
import org.apache.user.User;
import org.apache.user.UserAccount;
import org.apache.uservm.UserVm;
import org.apache.utils.net.Ip;
import org.apache.vm.InstanceGroup;
import org.apache.vm.Nic;
import org.apache.vm.NicSecondaryIp;
import org.apache.vm.VirtualMachine;
import org.apache.vm.snapshot.VMSnapshot;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public interface ResponseGenerator {
    UserResponse createUserResponse(UserAccount user);

    AccountResponse createAccountResponse(Account account);

    DomainResponse createDomainResponse(Domain domain);

    DiskOfferingResponse createDiskOfferingResponse(DiskOffering offering);

    ResourceLimitResponse createResourceLimitResponse(ResourceLimit limit);

    ResourceCountResponse createResourceCountResponse(ResourceCount resourceCount);

    ServiceOfferingResponse createServiceOfferingResponse(ServiceOffering offering);

    ConfigurationResponse createConfigurationResponse(Configuration cfg);

    SnapshotResponse createSnapshotResponse(Snapshot snapshot);

    SnapshotPolicyResponse createSnapshotPolicyResponse(SnapshotPolicy policy);

    List<UserVmResponse> createUserVmResponse(String objectName, UserVm... userVms);

    List<UserVmResponse> createUserVmResponse(String objectName, EnumSet<VMDetails> details, UserVm... userVms);

    SystemVmResponse createSystemVmResponse(VirtualMachine systemVM);

    DomainRouterResponse createDomainRouterResponse(VirtualRouter router);

    HostResponse createHostResponse(Host host, EnumSet<HostDetails> details);

    HostResponse createHostResponse(Host host);

    HostForMigrationResponse createHostForMigrationResponse(Host host);

    HostForMigrationResponse createHostForMigrationResponse(Host host, EnumSet<HostDetails> details);

    VlanIpRangeResponse createVlanIpRangeResponse(Vlan vlan);

    IPAddressResponse createIPAddressResponse(IpAddress ipAddress);

    GuestVlanRangeResponse createDedicatedGuestVlanRangeResponse(GuestVlan result);

    GlobalLoadBalancerResponse createGlobalLoadBalancerResponse(GlobalLoadBalancerRule globalLoadBalancerRule);

    LoadBalancerResponse createLoadBalancerResponse(LoadBalancer loadBalancer);

    LBStickinessResponse createLBStickinessPolicyResponse(List<? extends StickinessPolicy> stickinessPolicies, LoadBalancer lb);

    LBStickinessResponse createLBStickinessPolicyResponse(StickinessPolicy stickinessPolicy, LoadBalancer lb);

    LBHealthCheckResponse createLBHealthCheckPolicyResponse(List<? extends HealthCheckPolicy> healthcheckPolicies,
            LoadBalancer lb);

    LBHealthCheckResponse createLBHealthCheckPolicyResponse(HealthCheckPolicy healthcheckPolicy, LoadBalancer lb);

    PodResponse createPodResponse(Pod pod, Boolean showCapacities);

    ZoneResponse createZoneResponse(DataCenter dataCenter, Boolean showCapacities);

    VolumeResponse createVolumeResponse(Volume volume);

    InstanceGroupResponse createInstanceGroupResponse(InstanceGroup group);

    StoragePoolResponse createStoragePoolResponse(StoragePool pool);

    StoragePoolResponse createStoragePoolForMigrationResponse(StoragePool pool);

    ClusterResponse createClusterResponse(Cluster cluster, Boolean showCapacities);

    FirewallRuleResponse createPortForwardingRuleResponse(PortForwardingRule fwRule);

    IpForwardingRuleResponse createIpForwardingRuleResponse(StaticNatRule fwRule);

    User findUserById(Long userId);

    UserVm findUserVmById(Long vmId);

    Volume findVolumeById(Long volumeId);

    Account findAccountByNameDomain(String accountName, Long domainId);

    VirtualMachineTemplate findTemplateById(Long templateId);

    Host findHostById(Long hostId);

    //List<TemplateResponse> createTemplateResponses(long templateId, long zoneId, boolean readyOnly);

    VpnUsersResponse createVpnUserResponse(VpnUser user);

    RemoteAccessVpnResponse createRemoteAccessVpnResponse(RemoteAccessVpn vpn);

    List<TemplateResponse> createTemplateResponses(long templateId, Long zoneId, boolean readyOnly);

    List<TemplateResponse> createTemplateResponses(long templateId, Long snapshotId, Long volumeId, boolean readyOnly);

    //ListResponse<SecurityGroupResponse> createSecurityGroupResponses(List<? extends SecurityGroupRules> networkGroups);

    SecurityGroupResponse createSecurityGroupResponseFromSecurityGroupRule(List<? extends SecurityRule> SecurityRules);

    SecurityGroupResponse createSecurityGroupResponse(SecurityGroup group);

    ExtractResponse createExtractResponse(Long uploadId, Long id, Long zoneId, Long accountId, String mode, String url);

    ExtractResponse createExtractResponse(Long id, Long zoneId, Long accountId, String mode, String url);

    String toSerializedString(CreateCmdResponse response, String responseType);

    AsyncJobResponse createAsyncJobResponse(AsyncJob job);

    EventResponse createEventResponse(Event event);

    //List<EventResponse> createEventResponse(EventJoinVO... events);

    TemplateResponse createTemplateUpdateResponse(VirtualMachineTemplate result);

    List<TemplateResponse> createTemplateResponses(VirtualMachineTemplate result, Long zoneId, boolean readyOnly);

    List<CapacityResponse> createCapacityResponse(List<? extends Capacity> result, DecimalFormat format);

    TemplatePermissionsResponse createTemplatePermissionsResponse(List<String> accountNames, Long id, boolean isAdmin);

    AsyncJobResponse queryJobResult(QueryAsyncJobResultCmd cmd);

    NetworkOfferingResponse createNetworkOfferingResponse(NetworkOffering offering);

    NetworkResponse createNetworkResponse(Network network);

    UserResponse createUserResponse(User user);

    //List<UserResponse> createUserResponse(UserAccountJoinVO... users);

    AccountResponse createUserAccountResponse(UserAccount user);

    Long getSecurityGroupId(String groupName, long accountId);

    List<TemplateResponse> createIsoResponses(VirtualMachineTemplate iso, Long zoneId, boolean readyOnly);

   // List<TemplateResponse> createIsoResponses(long isoId, Long zoneId, boolean readyOnly);
    //List<TemplateResponse> createIsoResponses(VirtualMachineTemplate iso, long zoneId, boolean readyOnly);

    ProjectResponse createProjectResponse(Project project);

    List<TemplateResponse> createTemplateResponses(long templateId, Long vmId);

    FirewallResponse createFirewallResponse(FirewallRule fwRule);

    HypervisorCapabilitiesResponse createHypervisorCapabilitiesResponse(HypervisorCapabilities hpvCapabilities);

    ProjectAccountResponse createProjectAccountResponse(ProjectAccount projectAccount);

    ProjectInvitationResponse createProjectInvitationResponse(ProjectInvitation invite);

    SystemVmInstanceResponse createSystemVmInstanceResponse(VirtualMachine systemVM);

    SwiftResponse createSwiftResponse(Swift swift);

    S3Response createS3Response(S3 result);

    PhysicalNetworkResponse createPhysicalNetworkResponse(PhysicalNetwork result);

    ServiceResponse createNetworkServiceResponse(Service service);

    ProviderResponse createNetworkServiceProviderResponse(PhysicalNetworkServiceProvider result);

    TrafficTypeResponse createTrafficTypeResponse(PhysicalNetworkTrafficType result);

    VirtualRouterProviderResponse createVirtualRouterProviderResponse(VirtualRouterProvider result);

    LDAPConfigResponse createLDAPConfigResponse(String hostname, Integer port, Boolean useSSL, String queryFilter, String baseSearch, String dn);

    StorageNetworkIpRangeResponse createStorageNetworkIpRangeResponse(StorageNetworkIpRange result);

    RegionResponse createRegionResponse(Region region);

    ImageStoreResponse createImageStoreResponse(ImageStore os);

    /**
     * @param resourceTag
     * @param keyValueOnly TODO
     * @return
     */
    ResourceTagResponse createResourceTagResponse(ResourceTag resourceTag, boolean keyValueOnly);


    Site2SiteVpnGatewayResponse createSite2SiteVpnGatewayResponse(Site2SiteVpnGateway result);


    /**
     * @param offering
     * @return
     */
    VpcOfferingResponse createVpcOfferingResponse(VpcOffering offering);

    /**
     * @param vpc
     * @return
     */
    VpcResponse createVpcResponse(Vpc vpc);

    /**
     * @param networkACLItem
     * @return
     */
    NetworkACLItemResponse createNetworkACLItemResponse(NetworkACLItem networkACLItem);

    /**
     * @param networkACL
     * @return
     */
    NetworkACLResponse createNetworkACLResponse(NetworkACL networkACL);

    /**
     * @param result
     * @return
     */
    PrivateGatewayResponse createPrivateGatewayResponse(PrivateGateway result);

    /**
     * @param result
     * @return
     */
    StaticRouteResponse createStaticRouteResponse(StaticRoute result);

    Site2SiteCustomerGatewayResponse createSite2SiteCustomerGatewayResponse(Site2SiteCustomerGateway result);

    Site2SiteVpnConnectionResponse createSite2SiteVpnConnectionResponse(Site2SiteVpnConnection result);

    CounterResponse createCounterResponse(Counter ctr);

    ConditionResponse createConditionResponse(Condition cndn);

    AutoScalePolicyResponse createAutoScalePolicyResponse(AutoScalePolicy policy);

    AutoScaleVmProfileResponse createAutoScaleVmProfileResponse(AutoScaleVmProfile profile);

    AutoScaleVmGroupResponse createAutoScaleVmGroupResponse(AutoScaleVmGroup vmGroup);

    GuestOSResponse createGuestOSResponse(GuestOS os);

    SnapshotScheduleResponse createSnapshotScheduleResponse(SnapshotSchedule sched);

    UsageRecordResponse createUsageResponse(Usage usageRecord);

    TrafficMonitorResponse createTrafficMonitorResponse(Host trafficMonitor);
    VMSnapshotResponse createVMSnapshotResponse(VMSnapshot vmSnapshot);

    NicSecondaryIpResponse createSecondaryIPToNicResponse(NicSecondaryIp result);
    public NicResponse createNicResponse(Nic result);

    ApplicationLoadBalancerResponse createLoadBalancerContainerReponse(ApplicationLoadBalancerRule lb, Map<Ip, UserVm> lbInstances);

    AffinityGroupResponse createAffinityGroupResponse(AffinityGroup group);

    Long getAffinityGroupId(String name, long entityOwnerId);

    PortableIpRangeResponse createPortableIPRangeResponse(PortableIpRange range);

    PortableIpResponse createPortableIPResponse(PortableIp portableIp);

    InternalLoadBalancerElementResponse createInternalLbElementResponse(VirtualRouterProvider result);

    IsolationMethodResponse createIsolationMethodResponse(IsolationType method);

}
