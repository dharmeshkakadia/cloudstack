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

package org.apache.vpc;

import java.io.IOException;

import org.apache.alert.AlertManager;
import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.cluster.agentlb.dao.HostTransferMapDaoImpl;
import org.apache.configuration.dao.ConfigurationDaoImpl;
import org.apache.configuration.dao.ResourceCountDaoImpl;
import org.apache.configuration.dao.ResourceLimitDaoImpl;
import org.apache.dao.EntityManagerImpl;
import org.apache.dc.dao.AccountVlanMapDaoImpl;
import org.apache.dc.dao.ClusterDaoImpl;
import org.apache.dc.dao.DataCenterDaoImpl;
import org.apache.dc.dao.DataCenterIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterLinkLocalIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterVnetDaoImpl;
import org.apache.dc.dao.DcDetailsDaoImpl;
import org.apache.dc.dao.HostPodDaoImpl;
import org.apache.dc.dao.PodVlanDaoImpl;
import org.apache.dc.dao.PodVlanMapDaoImpl;
import org.apache.dc.dao.VlanDaoImpl;
import org.apache.domain.dao.DomainDaoImpl;
import org.apache.event.dao.UsageEventDao;
import org.apache.host.dao.HostDaoImpl;
import org.apache.host.dao.HostDetailsDaoImpl;
import org.apache.host.dao.HostTagsDaoImpl;
import org.apache.network.Ipv6AddressManagerImpl;
import org.apache.network.StorageNetworkManager;
import org.apache.network.dao.FirewallRulesCidrsDaoImpl;
import org.apache.network.dao.FirewallRulesDaoImpl;
import org.apache.network.dao.IPAddressDaoImpl;
import org.apache.network.dao.LoadBalancerDaoImpl;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.PhysicalNetworkDaoImpl;
import org.apache.network.dao.PhysicalNetworkTrafficTypeDaoImpl;
import org.apache.network.dao.RouterNetworkDaoImpl;
import org.apache.network.dao.Site2SiteVpnGatewayDaoImpl;
import org.apache.network.dao.UserIpv6AddressDaoImpl;
import org.apache.network.dao.VirtualRouterProviderDaoImpl;
import org.apache.network.element.NetworkElement;
import org.apache.network.element.Site2SiteVpnServiceProvider;
import org.apache.network.lb.LoadBalancingRulesManager;
import org.apache.network.rules.RulesManager;
import org.apache.network.vpc.VpcManagerImpl;
import org.apache.network.vpc.dao.PrivateIpDaoImpl;
import org.apache.network.vpc.dao.StaticRouteDaoImpl;
import org.apache.network.vpc.dao.VpcDao;
import org.apache.network.vpc.dao.VpcGatewayDaoImpl;
import org.apache.network.vpc.dao.VpcOfferingDao;
import org.apache.network.vpc.dao.VpcServiceMapDaoImpl;
import org.apache.network.vpn.RemoteAccessVpnService;
import org.apache.network.vpn.Site2SiteVpnManager;
import org.apache.projects.dao.ProjectAccountDaoImpl;
import org.apache.projects.dao.ProjectDaoImpl;
import org.apache.resourcelimit.ResourceLimitManagerImpl;
import org.apache.service.dao.ServiceOfferingDaoImpl;
import org.apache.storage.dao.SnapshotDaoImpl;
import org.apache.storage.dao.VMTemplateDaoImpl;
import org.apache.storage.dao.VMTemplateDetailsDaoImpl;
import org.apache.storage.dao.VMTemplateHostDaoImpl;
import org.apache.storage.dao.VMTemplateZoneDaoImpl;
import org.apache.storage.dao.VolumeDaoImpl;
import org.apache.tags.dao.ResourceTagsDaoImpl;
import org.apache.user.AccountManager;
import org.apache.user.dao.AccountDaoImpl;
import org.apache.user.dao.UserStatisticsDaoImpl;
import org.apache.vm.UserVmManager;
import org.apache.vm.dao.DomainRouterDaoImpl;
import org.apache.vm.dao.NicDaoImpl;
import org.apache.vm.dao.NicSecondaryIpDaoImpl;
import org.apache.vm.dao.UserVmDaoImpl;
import org.apache.vm.dao.UserVmDetailsDaoImpl;
import org.apache.vm.dao.VMInstanceDaoImpl;
import org.apache.vpc.dao.MockNetworkOfferingDaoImpl;
import org.apache.vpc.dao.MockNetworkOfferingServiceMapDaoImpl;
import org.apache.vpc.dao.MockNetworkServiceMapDaoImpl;
import org.apache.vpc.dao.MockVpcDaoImpl;
import org.apache.vpc.dao.MockVpcOfferingDaoImpl;
import org.apache.vpc.dao.MockVpcOfferingServiceMapDaoImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;


@Configuration
@ComponentScan(basePackageClasses={VpcManagerImpl.class, NetworkElement.class,
        VpcOfferingDao.class,
        ConfigurationDaoImpl.class,
        IPAddressDaoImpl.class,
        DomainRouterDaoImpl.class,
        VpcGatewayDaoImpl.class,
        PrivateIpDaoImpl.class,
        StaticRouteDaoImpl.class,
        PhysicalNetworkDaoImpl.class,
        ResourceTagsDaoImpl.class,
        FirewallRulesDaoImpl.class,
        VlanDaoImpl.class,
        AccountDaoImpl.class,
        ResourceCountDaoImpl.class,
        Site2SiteVpnGatewayDaoImpl.class,
        PodVlanMapDaoImpl.class,
        AccountVlanMapDaoImpl.class,
        HostDaoImpl.class,
        HostDetailsDaoImpl.class,
        HostTagsDaoImpl.class,
        HostTransferMapDaoImpl.class,
        ClusterDaoImpl.class,
        HostPodDaoImpl.class,
        RouterNetworkDaoImpl.class,
        UserStatisticsDaoImpl.class,
        PhysicalNetworkTrafficTypeDaoImpl.class,
        FirewallRulesCidrsDaoImpl.class,
        ResourceLimitManagerImpl.class,
        ResourceLimitDaoImpl.class,
        ResourceCountDaoImpl.class,
        DomainDaoImpl.class,
        UserVmDaoImpl.class,
        UserVmDetailsDaoImpl.class,
        NicDaoImpl.class,
        SnapshotDaoImpl.class,
        VMInstanceDaoImpl.class,
        VolumeDaoImpl.class,
        UserIpv6AddressDaoImpl.class,
        NicSecondaryIpDaoImpl.class,
        VpcServiceMapDaoImpl.class,
        ServiceOfferingDaoImpl.class,
        VMTemplateHostDaoImpl.class,
        MockVpcDaoImpl.class,
        VMTemplateDaoImpl.class,VMTemplateZoneDaoImpl.class,VMTemplateDetailsDaoImpl.class,DataCenterDaoImpl.class,DataCenterIpAddressDaoImpl.class,DataCenterLinkLocalIpAddressDaoImpl.class,DataCenterVnetDaoImpl.class,PodVlanDaoImpl.class,
        DcDetailsDaoImpl.class,MockNetworkManagerImpl.class,MockVpcVirtualNetworkApplianceManager.class,
        EntityManagerImpl.class,LoadBalancerDaoImpl.class,FirewallRulesCidrsDaoImpl.class,VirtualRouterProviderDaoImpl.class,
        ProjectDaoImpl.class,ProjectAccountDaoImpl.class,MockVpcOfferingDaoImpl.class,
        MockConfigurationManagerImpl.class, MockNetworkOfferingServiceMapDaoImpl.class,
        MockNetworkServiceMapDaoImpl.class,MockVpcOfferingServiceMapDaoImpl.class,MockNetworkOfferingDaoImpl.class, MockNetworkModelImpl.class, Ipv6AddressManagerImpl.class},
        includeFilters={@Filter(value=VpcTestConfiguration.VpcLibrary.class, type=FilterType.CUSTOM)},
        useDefaultFilters=false
        )
public class VpcTestConfiguration {



    @Bean
    public RulesManager rulesManager() {
        return Mockito.mock(RulesManager.class);
    }


    @Bean
    public StorageNetworkManager storageNetworkManager() {
        return Mockito.mock(StorageNetworkManager.class);
    }

    @Bean
    public LoadBalancingRulesManager loadBalancingRulesManager() {
        return Mockito.mock(LoadBalancingRulesManager.class);
    }


    @Bean
    public AlertManager alertManager() {
        return Mockito.mock(AlertManager.class);
    }



    @Bean
    public UserVmManager userVmManager() {
        return Mockito.mock(UserVmManager.class);
    }


    @Bean
    public AccountManager accountManager() {
        return Mockito.mock(AccountManager.class);
    }

    @Bean
    public Site2SiteVpnServiceProvider site2SiteVpnServiceProvider() {
        return Mockito.mock(Site2SiteVpnServiceProvider.class);
    }

    @Bean
    public Site2SiteVpnManager site2SiteVpnManager() {
        return Mockito.mock(Site2SiteVpnManager.class);
    }


    @Bean
    public UsageEventDao usageEventDao() {
        return Mockito.mock(UsageEventDao.class);
    }

    @Bean
    public RemoteAccessVpnService remoteAccessVpnService() {
        return Mockito.mock(RemoteAccessVpnService.class);
    }

//    @Bean
//    public VpcDao vpcDao() {
//        return Mockito.mock(VpcDao.class);
//    }

    @Bean
    public NetworkDao networkDao() {
        return Mockito.mock(NetworkDao.class);
    }
    

    public static class VpcLibrary implements TypeFilter {
        @Override
        public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            ComponentScan cs = VpcTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }
    }
}
