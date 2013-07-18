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

package org.apache.network.security;

import java.io.IOException;

import org.apache.agent.AgentManager;
import org.apache.api.query.dao.SecurityGroupJoinDaoImpl;
import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.cluster.agentlb.dao.HostTransferMapDaoImpl;
import org.apache.configuration.dao.ConfigurationDaoImpl;
import org.apache.dc.dao.ClusterDaoImpl;
import org.apache.dc.dao.DataCenterDaoImpl;
import org.apache.dc.dao.DataCenterIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterLinkLocalIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterVnetDaoImpl;
import org.apache.dc.dao.DcDetailsDaoImpl;
import org.apache.dc.dao.HostPodDaoImpl;
import org.apache.dc.dao.PodVlanDaoImpl;
import org.apache.domain.dao.DomainDaoImpl;
import org.apache.event.dao.UsageEventDaoImpl;
import org.apache.host.dao.HostDaoImpl;
import org.apache.host.dao.HostDetailsDaoImpl;
import org.apache.host.dao.HostTagsDaoImpl;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.security.SecurityGroupManagerImpl2;
import org.apache.network.security.SecurityGroupManagerTestConfiguration.Library;
import org.apache.network.security.dao.SecurityGroupDaoImpl;
import org.apache.network.security.dao.SecurityGroupRuleDaoImpl;
import org.apache.network.security.dao.SecurityGroupRulesDaoImpl;
import org.apache.network.security.dao.SecurityGroupVMMapDaoImpl;
import org.apache.network.security.dao.SecurityGroupWorkDaoImpl;
import org.apache.network.security.dao.VmRulesetLogDaoImpl;
import org.apache.projects.ProjectManager;
import org.apache.tags.dao.ResourceTagsDaoImpl;
import org.apache.user.AccountManager;
import org.apache.user.DomainManager;
import org.apache.user.dao.AccountDaoImpl;
import org.apache.vm.UserVmManager;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.dao.NicDaoImpl;
import org.apache.vm.dao.UserVmDaoImpl;
import org.apache.vm.dao.UserVmDetailsDaoImpl;
import org.apache.vm.dao.VMInstanceDaoImpl;
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
@ComponentScan(basePackageClasses={
        SecurityGroupRulesDaoImpl.class,
        UserVmDaoImpl.class,
        AccountDaoImpl.class,
        ConfigurationDaoImpl.class,
        SecurityGroupWorkDaoImpl.class,
        VmRulesetLogDaoImpl.class,
        VMInstanceDaoImpl.class,
        DomainDaoImpl.class,
        UsageEventDaoImpl.class,
        ResourceTagsDaoImpl.class,
        HostDaoImpl.class,
        HostDetailsDaoImpl.class,
        HostTagsDaoImpl.class,
        ClusterDaoImpl.class,
        HostPodDaoImpl.class,
        DataCenterDaoImpl.class,
        DataCenterIpAddressDaoImpl.class,
        HostTransferMapDaoImpl.class,
        SecurityGroupManagerImpl2.class,
        SecurityGroupDaoImpl.class,
        SecurityGroupVMMapDaoImpl.class,
        UserVmDetailsDaoImpl.class,
        DataCenterIpAddressDaoImpl.class,
        DataCenterLinkLocalIpAddressDaoImpl.class,
        DataCenterVnetDaoImpl.class,
        PodVlanDaoImpl.class,
        DcDetailsDaoImpl.class,
        SecurityGroupRuleDaoImpl.class,
        NicDaoImpl.class,
        SecurityGroupJoinDaoImpl.class},
        includeFilters={@Filter(value=Library.class, type=FilterType.CUSTOM)},
        useDefaultFilters=false
        )
public class SecurityGroupManagerTestConfiguration {

    @Bean
    public NetworkModel networkModel() {
        return Mockito.mock(NetworkModel.class);
    }

    @Bean
    public AgentManager agentManager() {
        return Mockito.mock(AgentManager.class);
    }

    @Bean
    public VirtualMachineManager virtualMachineManager(){
        return Mockito.mock(VirtualMachineManager.class);
    }

    @Bean
    public UserVmManager userVmManager() {
        return Mockito.mock(UserVmManager.class);
    }

    @Bean
    public NetworkManager networkManager(){
        return Mockito.mock(NetworkManager.class);
    }

    @Bean
    public AccountManager accountManager() {
        return Mockito.mock(AccountManager.class);
    }

    @Bean
    public DomainManager domainManager() {
        return Mockito.mock(DomainManager.class);
    }

    @Bean
    public ProjectManager projectManager() {
        return Mockito.mock(ProjectManager.class);
    }

    public static class Library implements TypeFilter {

        @Override
        public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            ComponentScan cs = SecurityGroupManagerTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }

    }
}
