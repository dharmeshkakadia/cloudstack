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
package org.apache.cloudstack.storage.test;

import java.io.IOException;

import org.apache.cloudstack.acl.APIChecker;
import org.apache.cloudstack.engine.service.api.OrchestrationService;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.framework.rpc.RpcProvider;
import org.apache.cloudstack.storage.cache.manager.StorageCacheManagerImpl;
import org.apache.cloudstack.storage.test.ChildTestConfiguration.Library;
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
import org.apache.agent.AgentManager;
import org.apache.alert.AlertManager;
import org.apache.capacity.dao.CapacityDaoImpl;
import org.apache.cluster.ClusteredAgentRebalanceService;
import org.apache.cluster.agentlb.dao.HostTransferMapDaoImpl;
import org.apache.configuration.dao.ConfigurationDaoImpl;
import org.apache.dc.ClusterDetailsDaoImpl;
import org.apache.dc.dao.ClusterDaoImpl;
import org.apache.dc.dao.DataCenterDaoImpl;
import org.apache.dc.dao.DataCenterIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterLinkLocalIpAddressDaoImpl;
import org.apache.dc.dao.DataCenterVnetDaoImpl;
import org.apache.dc.dao.DcDetailsDaoImpl;
import org.apache.dc.dao.HostPodDaoImpl;
import org.apache.dc.dao.PodVlanDaoImpl;
import org.apache.domain.dao.DomainDaoImpl;
import org.apache.host.dao.HostDao;
import org.apache.host.dao.HostDaoImpl;
import org.apache.host.dao.HostDetailsDaoImpl;
import org.apache.host.dao.HostTagsDaoImpl;
import org.apache.hypervisor.HypervisorGuruManager;
import org.apache.resource.ResourceManager;
import org.apache.server.ManagementServer;
import org.apache.server.auth.UserAuthenticator;
import oorg.apache.service.dao.ServiceOfferingDaoImpl;
import org.apache.storage.dao.DiskOfferingDaoImpl;
import org.apache.storage.dao.SnapshotDaoImpl;
import org.apache.storage.dao.StoragePoolDetailsDaoImpl;
import org.apache.storage.dao.StoragePoolHostDaoImpl;
import org.apache.storage.dao.StoragePoolWorkDaoImpl;
import org.apache.storage.dao.VMTemplateDaoImpl;
import org.apache.storage.dao.VMTemplateDetailsDaoImpl;
import org.apache.storage.dao.VMTemplateHostDaoImpl;
import org.apache.storage.dao.VMTemplatePoolDaoImpl;
import org.apache.storage.dao.VMTemplateZoneDaoImpl;
import org.apache.storage.dao.VolumeDaoImpl;
import org.apache.storage.dao.VolumeHostDaoImpl;
import org.apache.storage.download.DownloadMonitorImpl;
import org.apache.storage.s3.S3Manager;
import org.apache.storage.secondary.SecondaryStorageVmManager;
import org.apache.storage.snapshot.SnapshotManager;
import org.apache.storage.swift.SwiftManager;
import org.apache.template.TemplateManager;
import org.apache.user.AccountManager;
import org.apache.user.ResourceLimitService;
import org.apache.user.dao.AccountDaoImpl;
import org.apache.user.dao.UserDaoImpl;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.dao.ConsoleProxyDaoImpl;
import org.apache.vm.dao.DomainRouterDao;
import org.apache.vm.dao.NicDaoImpl;
import org.apache.vm.dao.SecondaryStorageVmDaoImpl;
import org.apache.vm.dao.UserVmDaoImpl;
import org.apache.vm.dao.UserVmDetailsDaoImpl;
import org.apache.vm.dao.VMInstanceDaoImpl;
import org.apache.vm.snapshot.dao.VMSnapshotDaoImpl;

@Configuration
@ComponentScan(basePackageClasses = { NicDaoImpl.class, VMInstanceDaoImpl.class, VMTemplateHostDaoImpl.class,
        VolumeHostDaoImpl.class, VolumeDaoImpl.class, VMTemplatePoolDaoImpl.class, ResourceTagsDaoImpl.class,
        VMTemplateDaoImpl.class, MockStorageMotionStrategy.class, ConfigurationDaoImpl.class, ClusterDaoImpl.class,
        HostPodDaoImpl.class, VMTemplateZoneDaoImpl.class, VMTemplateDetailsDaoImpl.class, HostDetailsDaoImpl.class,
        HostTagsDaoImpl.class, HostTransferMapDaoImpl.class, DataCenterIpAddressDaoImpl.class,
        DataCenterLinkLocalIpAddressDaoImpl.class, DataCenterVnetDaoImpl.class, PodVlanDaoImpl.class,
        DcDetailsDaoImpl.class, DiskOfferingDaoImpl.class, StoragePoolHostDaoImpl.class, UserVmDaoImpl.class,
        UserVmDetailsDaoImpl.class, ServiceOfferingDaoImpl.class, CapacityDaoImpl.class, SnapshotDaoImpl.class,
        VMSnapshotDaoImpl.class, OCFS2ManagerImpl.class, ClusterDetailsDaoImpl.class, SecondaryStorageVmDaoImpl.class,
        ConsoleProxyDaoImpl.class, StoragePoolWorkDaoImpl.class, StorageCacheManagerImpl.class, UserDaoImpl.class,
        DataCenterDaoImpl.class, StoragePoolDetailsDaoImpl.class, DomainDaoImpl.class, DownloadMonitorImpl.class,
        AccountDaoImpl.class }, includeFilters = { @Filter(value = Library.class, type = FilterType.CUSTOM) },
        useDefaultFilters = false)
public class ChildTestConfiguration extends TestConfiguration {

    @Bean
    public SecondaryStorageVmManager secondaryStoreageMgr() {
        return Mockito.mock(SecondaryStorageVmManager.class);
    }

    @Bean
    public HostDao hostDao() {
        return Mockito.spy(new HostDaoImpl());
    }

    @Bean
    public EndPointSelector selector() {
        return Mockito.mock(EndPointSelector.class);
    }

    @Bean
    public AgentManager agentMgr() {
        return new DirectAgentManagerSimpleImpl();
    }

    @Bean
    public ResourceLimitService limtServe() {
        return Mockito.mock(ResourceLimitService.class);
    }

    @Bean
    public AccountManager acctMgt() {
        return Mockito.mock(AccountManager.class);
    }

    @Bean
    public RpcProvider rpcProvider() {
        return Mockito.mock(RpcProvider.class);
    }

    @Bean
    public ClusteredAgentRebalanceService _rebalanceService() {
        return Mockito.mock(ClusteredAgentRebalanceService.class);
    }

    @Bean
    public UserAuthenticator authenticator() {
        return Mockito.mock(UserAuthenticator.class);
    }

    @Bean
    public OrchestrationService orchSrvc() {
        return Mockito.mock(OrchestrationService.class);
    }

    @Bean
    public APIChecker apiChecker() {
        return Mockito.mock(APIChecker.class);
    }

    @Bean
    public TemplateManager templateMgr() {
        return Mockito.mock(TemplateManager.class);
    }

    @Bean
    public VolumeManager volumeMgr() {
        return Mockito.mock(VolumeManager.class);
    }

    @Bean
    public SwiftManager switfMgr() {
        return Mockito.mock(SwiftManager.class);
    }

    @Bean
    public ManagementServer server() {
        return Mockito.mock(ManagementServer.class);
    }

    @Bean
    public VirtualMachineManager vmMgr() {
        return Mockito.mock(VirtualMachineManager.class);
    }

    @Bean
    public S3Manager s3Mgr() {
        return Mockito.mock(S3Manager.class);
    }

    @Bean
    public SnapshotManager snapshotMgr() {
        return Mockito.mock(SnapshotManager.class);
    }

    @Bean
    public ResourceManager resourceMgr() {
        return Mockito.mock(ResourceManager.class);
    }

    @Bean
    public DomainRouterDao domainRouterDao() {
        return Mockito.mock(DomainRouterDao.class);
    }

    @Bean
    public StorageManager storageMgr() {
        return Mockito.mock(StorageManager.class);
    }

    @Bean
    public AlertManager alertMgr() {
        return Mockito.mock(AlertManager.class);
    }

    @Bean
    public HypervisorGuruManager hypervisorGuruMgr() {
        return Mockito.mock(HypervisorGuruManager.class);
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
