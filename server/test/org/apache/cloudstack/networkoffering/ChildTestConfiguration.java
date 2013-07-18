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

package org.apache.cloudstack.networkoffering;

import java.io.IOException;

import org.apache.agent.AgentManager;
import org.apache.alert.AlertManager;
import org.apache.api.query.dao.UserAccountJoinDaoImpl;
import org.apache.capacity.dao.CapacityDaoImpl;
import org.apache.cloudstack.acl.SecurityChecker;
import org.apache.cloudstack.region.PortableIpDaoImpl;
import org.apache.cloudstack.region.dao.RegionDaoImpl;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDaoImpl;
import org.apache.cloudstack.test.utils.SpringUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.region.PortableIpRangeDaoImpl;
import org.apache.cluster.agentlb.dao.HostTransferMapDaoImpl;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.dc.ClusterDetailsDao;
import org.apache.dc.dao.AccountVlanMapDaoImpl;
import org.apache.dc.dao.ClusterDaoImpl;
import org.apache.dc.dao.DataCenterDaoImpl;
import org.apache.dc.dao.DataCenterIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterLinkLocalIpAddressDao;
import org.apache.dc.dao.DataCenterVnetDaoImpl;
import org.apache.dc.dao.DcDetailsDaoImpl;
import org.apache.dc.dao.DedicatedResourceDao;
import org.apache.dc.dao.HostPodDaoImpl;
import org.apache.dc.dao.PodVlanDaoImpl;
import org.apache.dc.dao.PodVlanMapDaoImpl;
import org.apache.dc.dao.VlanDaoImpl;
import org.apache.domain.dao.DomainDaoImpl;
import org.apache.event.dao.UsageEventDaoImpl;
import org.apache.host.dao.HostDaoImpl;
import org.apache.host.dao.HostDetailsDaoImpl;
import org.apache.host.dao.HostTagsDaoImpl;
import org.apache.network.Ipv6AddressManager;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.NetworkService;
import org.apache.network.StorageNetworkManager;
import org.apache.network.dao.AccountGuestVlanMapDaoImpl;
import org.apache.network.dao.FirewallRulesCidrsDaoImpl;
import org.apache.network.dao.FirewallRulesDaoImpl;
import org.apache.network.dao.IPAddressDaoImpl;
import org.apache.network.dao.LoadBalancerDaoImpl;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkDomainDaoImpl;
import org.apache.network.dao.NetworkServiceMapDaoImpl;
import org.apache.network.dao.PhysicalNetworkDaoImpl;
import org.apache.network.dao.PhysicalNetworkServiceProviderDaoImpl;
import org.apache.network.dao.PhysicalNetworkTrafficTypeDaoImpl;
import org.apache.network.dao.UserIpv6AddressDaoImpl;
import org.apache.network.element.DhcpServiceProvider;
import org.apache.network.element.IpDeployer;
import org.apache.network.element.NetworkElement;
import org.apache.network.guru.NetworkGuru;
import org.apache.network.lb.LoadBalancingRulesManager;
import org.apache.network.rules.FirewallManager;
import org.apache.network.rules.RulesManager;
import org.apache.network.rules.dao.PortForwardingRulesDaoImpl;
import org.apache.network.vpc.NetworkACLManager;
import org.apache.network.vpc.VpcManager;
import org.apache.network.vpc.dao.PrivateIpDaoImpl;
import org.apache.network.vpn.RemoteAccessVpnService;
import org.apache.offerings.dao.NetworkOfferingDao;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.projects.ProjectManager;
import org.apache.server.ConfigurationServer;
import org.apache.server.ManagementService;
import org.apache.service.dao.ServiceOfferingDaoImpl;
import org.apache.service.dao.ServiceOfferingDetailsDaoImpl;
import org.apache.storage.dao.DiskOfferingDaoImpl;
import org.apache.storage.dao.S3DaoImpl;
import org.apache.storage.dao.SnapshotDaoImpl;
import org.apache.storage.dao.StoragePoolDetailsDaoImpl;
import org.apache.storage.dao.SwiftDaoImpl;
import org.apache.storage.dao.VolumeDaoImpl;
import org.apache.storage.s3.S3Manager;
import org.apache.storage.secondary.SecondaryStorageVmManager;
import org.apache.storage.swift.SwiftManager;
import org.apache.tags.dao.ResourceTagsDaoImpl;
import org.apache.user.AccountDetailsDao;
import org.apache.user.AccountManager;
import org.apache.user.ResourceLimitService;
import org.apache.user.UserContext;
import org.apache.user.UserContextInitializer;
import org.apache.user.dao.AccountDaoImpl;
import org.apache.user.dao.UserDaoImpl;
import org.apache.vm.dao.InstanceGroupDaoImpl;
import org.apache.vm.dao.NicDaoImpl;
import org.apache.vm.dao.NicSecondaryIpDaoImpl;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.VMInstanceDaoImpl;

@Configuration
@ComponentScan(basePackageClasses={
        AccountVlanMapDaoImpl.class,
        VolumeDaoImpl.class,
        HostPodDaoImpl.class,
        DomainDaoImpl.class,
        SwiftDaoImpl.class,
        ServiceOfferingDaoImpl.class,
        ServiceOfferingDetailsDaoImpl.class,
        VlanDaoImpl.class,
        IPAddressDaoImpl.class,
        ResourceTagsDaoImpl.class,
        AccountDaoImpl.class,
        InstanceGroupDaoImpl.class,
        UserAccountJoinDaoImpl.class,
        CapacityDaoImpl.class,
        SnapshotDaoImpl.class,
        HostDaoImpl.class,
        VMInstanceDaoImpl.class,
        HostTransferMapDaoImpl.class,
        PortForwardingRulesDaoImpl.class,
        PrivateIpDaoImpl.class,
        UsageEventDaoImpl.class,
        PodVlanMapDaoImpl.class,
        DiskOfferingDaoImpl.class,
        DataCenterDaoImpl.class,
        DataCenterIpAddressDaoImpl.class,
        DataCenterVnetDaoImpl.class,
        PodVlanDaoImpl.class,
        DcDetailsDaoImpl.class,
        NicSecondaryIpDaoImpl.class,
        UserIpv6AddressDaoImpl.class,
        S3DaoImpl.class,
        UserDaoImpl.class,
        NicDaoImpl.class,
        NetworkDomainDaoImpl.class,
        HostDetailsDaoImpl.class,
        HostTagsDaoImpl.class,
        ClusterDaoImpl.class,
        FirewallRulesDaoImpl.class,
        FirewallRulesCidrsDaoImpl.class,
        PhysicalNetworkDaoImpl.class,
        PhysicalNetworkTrafficTypeDaoImpl.class,
        PhysicalNetworkServiceProviderDaoImpl.class,
        LoadBalancerDaoImpl.class,
        NetworkServiceMapDaoImpl.class,
        PrimaryDataStoreDaoImpl.class,
        StoragePoolDetailsDaoImpl.class,
        PortableIpRangeDaoImpl.class,
        RegionDaoImpl.class,
        PortableIpDaoImpl.class,
        AccountGuestVlanMapDaoImpl.class
    },
includeFilters={@Filter(value=ChildTestConfiguration.Library.class, type=FilterType.CUSTOM)},
useDefaultFilters=false
        )

public class ChildTestConfiguration {

    @Bean
    public ManagementService managementService() {
        return Mockito.mock(ManagementService.class);
    }

    @Bean
    public AccountManager acctMgr() {
        return Mockito.mock(AccountManager.class);
    }

    @Bean
    public NetworkService ntwkSvc() {
        return Mockito.mock(NetworkService.class);
    }

    @Bean
    public NetworkModel ntwkMdl() {
        return Mockito.mock(NetworkModel.class);
    }

    @Bean
    public AlertManager alertMgr() {
        return Mockito.mock(AlertManager.class);
    }

    @Bean
    public SecurityChecker securityChkr() {
        return Mockito.mock(SecurityChecker.class);
    }

    @Bean
    public ResourceLimitService resourceSvc() {
        return Mockito.mock(ResourceLimitService.class);
    }

    @Bean
    public ProjectManager projectMgr() {
        return Mockito.mock(ProjectManager.class);
    }

    @Bean
    public SecondaryStorageVmManager ssvmMgr() {
        return Mockito.mock(SecondaryStorageVmManager.class);
    }

    @Bean
    public SwiftManager swiftMgr() {
        return Mockito.mock(SwiftManager.class);
    }

    @Bean
    public S3Manager s3Mgr() {
        return Mockito.mock(S3Manager.class);
    }

    @Bean
    public VpcManager vpcMgr() {
        return Mockito.mock(VpcManager.class);
    }

    @Bean
    public UserVmDao userVMDao() {
        return Mockito.mock(UserVmDao.class);
    }

    @Bean
    public RulesManager rulesMgr() {
        return Mockito.mock(RulesManager.class);
    }

    @Bean
    public LoadBalancingRulesManager lbRulesMgr() {
        return Mockito.mock(LoadBalancingRulesManager.class);
    }

    @Bean
    public RemoteAccessVpnService vpnMgr() {
        return Mockito.mock(RemoteAccessVpnService.class);
    }

    @Bean
    public NetworkGuru ntwkGuru() {
        return Mockito.mock(NetworkGuru.class);
    }

    @Bean
    public NetworkElement ntwkElement() {
        return Mockito.mock(NetworkElement.class);
    }

    @Bean
    public IpDeployer ipDeployer() {
        return Mockito.mock(IpDeployer.class);
    }

    @Bean
    public DhcpServiceProvider dhcpProvider() {
        return Mockito.mock(DhcpServiceProvider.class);
    }

    @Bean
    public FirewallManager firewallMgr() {
        return Mockito.mock(FirewallManager.class);
    }

    @Bean
    public AgentManager agentMgr() {
        return Mockito.mock(AgentManager.class);
    }

    @Bean
    public StorageNetworkManager storageNtwkMgr() {
        return Mockito.mock(StorageNetworkManager.class);
    }

    @Bean
    public NetworkACLManager ntwkAclMgr() {
        return Mockito.mock(NetworkACLManager.class);
    }

    @Bean
    public Ipv6AddressManager ipv6Mgr() {
        return Mockito.mock(Ipv6AddressManager.class);
    }

    @Bean
    public ConfigurationDao configDao() {
        return Mockito.mock(ConfigurationDao.class);
    }

    @Bean
    public UserContext userContext() {
        return Mockito.mock(UserContext.class);
    }

    @Bean
    public UserContextInitializer userContextInitializer() {
        return Mockito.mock(UserContextInitializer.class);
    }

    @Bean
    public NetworkManager networkManager() {
        return Mockito.mock(NetworkManager.class);
    }

    @Bean
    public NetworkOfferingDao networkOfferingDao() {
        return Mockito.mock(NetworkOfferingDao.class);
    }

    @Bean
    public NetworkDao networkDao() {
        return Mockito.mock(NetworkDao.class);
    }

    @Bean
    public DedicatedResourceDao DedicatedResourceDao() {
        return Mockito.mock(DedicatedResourceDao.class);
    }

    @Bean
    public NetworkOfferingServiceMapDao networkOfferingServiceMapDao() {
        return Mockito.mock(NetworkOfferingServiceMapDao.class);
    }

    @Bean
    public DataCenterLinkLocalIpAddressDao datacenterLinkLocalIpAddressDao() {
    	return Mockito.mock(DataCenterLinkLocalIpAddressDao.class);
    }

    @Bean
    public ConfigurationServer configurationServer() {
        return Mockito.mock(ConfigurationServer.class);
    }

    @Bean
    public ClusterDetailsDao clusterDetailsDao() {
        return Mockito.mock(ClusterDetailsDao.class);
    }

    @Bean
    public AccountDetailsDao accountDetailsDao() {
        return Mockito.mock(AccountDetailsDao.class);
    }

    @Bean
    public DataStoreManager dataStoreManager() {
        return Mockito.mock(DataStoreManager.class);
    }

    public static class Library implements TypeFilter {

        @Override
        public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            ComponentScan cs = ChildTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }

    }

}
