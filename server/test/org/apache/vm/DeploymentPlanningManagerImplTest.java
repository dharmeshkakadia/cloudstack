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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.capacity.CapacityManager;
import org.apache.capacity.dao.CapacityDao;
import org.apache.cloudstack.affinity.AffinityGroupProcessor;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.dc.ClusterDetailsDao;
import org.apache.dc.ClusterVO;
import org.apache.dc.DataCenterVO;
import org.apache.dc.dao.ClusterDao;
import org.apache.dc.dao.DataCenterDao;
import org.apache.dc.dao.DedicatedResourceDao;
import org.apache.dc.dao.HostPodDao;
import org.apache.deploy.DataCenterDeployment;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentClusterPlanner;
import org.apache.deploy.DeploymentPlanner;
import org.apache.deploy.DeploymentPlanningManagerImpl;
import org.apache.deploy.FirstFitPlanner;
import org.apache.deploy.PlannerHostReservationVO;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.deploy.DeploymentPlanner.PlannerResourceUsage;
import org.apache.deploy.dao.PlannerHostReservationDao;
import org.apache.exception.AffinityConflictException;
import org.apache.exception.InsufficientServerCapacityException;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.service.ServiceOfferingVO;
import org.apache.storage.StorageManager;
import org.apache.storage.dao.DiskOfferingDao;
import org.apache.storage.dao.GuestOSCategoryDao;
import org.apache.storage.dao.GuestOSDao;
import org.apache.storage.dao.StoragePoolHostDao;
import org.apache.storage.dao.VolumeDao;
import org.apache.user.AccountManager;
import org.apache.utils.component.ComponentContext;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfileImpl;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.VMInstanceDao;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DeploymentPlanningManagerImplTest {

    @Inject
    DeploymentPlanningManagerImpl _dpm;

    @Inject
    PlannerHostReservationDao _plannerHostReserveDao;

    @Inject VirtualMachineProfileImpl vmProfile;

    @Inject
    AffinityGroupVMMapDao _affinityGroupVMMapDao;

    @Inject
    ExcludeList avoids;

    @Inject
    DataCenterVO dc;

    @Inject
    DataCenterDao _dcDao;

    @Inject
    FirstFitPlanner _planner;

    @Inject
    ClusterDao _clusterDao;

    @Inject
    DedicatedResourceDao _dedicatedDao;

    private static long domainId = 5L;

    private static long dataCenterId = 1L;


    @BeforeClass
    public static void setUp() throws ConfigurationException {
    }

    @Before
    public void testSetUp() {
        ComponentContext.initComponentsLifeCycle();

        PlannerHostReservationVO reservationVO = new PlannerHostReservationVO(200L, 1L, 2L, 3L, PlannerResourceUsage.Shared);
        Mockito.when(_plannerHostReserveDao.persist(Mockito.any(PlannerHostReservationVO.class))).thenReturn(reservationVO);
        Mockito.when(_plannerHostReserveDao.findById(Mockito.anyLong())).thenReturn(reservationVO);
        Mockito.when(_affinityGroupVMMapDao.countAffinityGroupsForVm(Mockito.anyLong())).thenReturn(0L);

        VMInstanceVO vm = new VMInstanceVO();
        Mockito.when(vmProfile.getVirtualMachine()).thenReturn(vm);

        Mockito.when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);
        Mockito.when(dc.getId()).thenReturn(dataCenterId);

        ClusterVO clusterVO = new ClusterVO();
        clusterVO.setHypervisorType(HypervisorType.XenServer.toString());
        Mockito.when(_clusterDao.findById(Mockito.anyLong())).thenReturn(clusterVO);

        Mockito.when(_planner.getName()).thenReturn("FirstFitPlanner");
        List<DeploymentPlanner> planners = new ArrayList<DeploymentPlanner>();
        planners.add(_planner);
        _dpm.setPlanners(planners);

    }

    @Test
    public void dataCenterAvoidTest() throws InsufficientServerCapacityException, AffinityConflictException {
        ServiceOfferingVO svcOffering = new ServiceOfferingVO("testOffering", 1, 512, 500, 1, 1, false, false, false,
                "test dpm", false, false, null, false, VirtualMachine.Type.User, domainId, null, "FirstFitPlanner");
        Mockito.when(vmProfile.getServiceOffering()).thenReturn(svcOffering);

        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId);

        Mockito.when(avoids.shouldAvoid((DataCenterVO) Mockito.anyObject())).thenReturn(true);
        DeployDestination dest = _dpm.planDeployment(vmProfile, plan, avoids);
        assertNull("DataCenter is in avoid set, destination should be null! ", dest);
    }

    @Test
    public void plannerCannotHandleTest() throws InsufficientServerCapacityException, AffinityConflictException {
        ServiceOfferingVO svcOffering = new ServiceOfferingVO("testOffering", 1, 512, 500, 1, 1, false, false, false,
                "test dpm", false, false, null, false, VirtualMachine.Type.User, domainId, null,
                "UserDispersingPlanner");
        Mockito.when(vmProfile.getServiceOffering()).thenReturn(svcOffering);

        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId);
        Mockito.when(avoids.shouldAvoid((DataCenterVO) Mockito.anyObject())).thenReturn(false);

        Mockito.when(_planner.canHandle(vmProfile, plan, avoids)).thenReturn(false);
        DeployDestination dest = _dpm.planDeployment(vmProfile, plan, avoids);
        assertNull("Planner cannot handle, destination should be null! ", dest);
    }

    @Test
    public void emptyClusterListTest() throws InsufficientServerCapacityException, AffinityConflictException {
        ServiceOfferingVO svcOffering = new ServiceOfferingVO("testOffering", 1, 512, 500, 1, 1, false, false, false,
                "test dpm", false, false, null, false, VirtualMachine.Type.User, domainId, null, "FirstFitPlanner");
        Mockito.when(vmProfile.getServiceOffering()).thenReturn(svcOffering);

        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId);
        Mockito.when(avoids.shouldAvoid((DataCenterVO) Mockito.anyObject())).thenReturn(false);
        Mockito.when(_planner.canHandle(vmProfile, plan, avoids)).thenReturn(true);

        Mockito.when(((DeploymentClusterPlanner) _planner).orderClusters(vmProfile, plan, avoids)).thenReturn(null);
        DeployDestination dest = _dpm.planDeployment(vmProfile, plan, avoids);
        assertNull("Planner cannot handle, destination should be null! ", dest);
    }


    @Configuration
    @ComponentScan(basePackageClasses = { DeploymentPlanningManagerImpl.class }, includeFilters = { @Filter(value = TestConfiguration.Library.class, type = FilterType.CUSTOM) }, useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {

        @Bean
        public FirstFitPlanner firstFitPlanner() {
            return Mockito.mock(FirstFitPlanner.class);
        }

        @Bean
        public DeploymentPlanner deploymentPlanner() {
            return Mockito.mock(DeploymentPlanner.class);
        }

        @Bean
        public DataCenterVO dataCenter() {
            return Mockito.mock(DataCenterVO.class);
        }

        @Bean
        public ExcludeList excludeList() {
            return Mockito.mock(ExcludeList.class);
        }

        @Bean
        public VirtualMachineProfileImpl virtualMachineProfileImpl() {
            return Mockito.mock(VirtualMachineProfileImpl.class);
        }

        @Bean
        public ClusterDetailsDao clusterDetailsDao() {
            return Mockito.mock(ClusterDetailsDao.class);
        }

        @Bean
        public DataStoreManager cataStoreManager() {
            return Mockito.mock(DataStoreManager.class);
        }

        @Bean
        public StorageManager storageManager() {
            return Mockito.mock(StorageManager.class);
        }

        @Bean
        public HostDao hostDao() {
            return Mockito.mock(HostDao.class);
        }

        @Bean
        public HostPodDao hostPodDao() {
            return Mockito.mock(HostPodDao.class);
        }

        @Bean
        public ClusterDao clusterDao() {
            return Mockito.mock(ClusterDao.class);
        }

        @Bean
        public DedicatedResourceDao dedicatedResourceDao() {
            return Mockito.mock(DedicatedResourceDao.class);
        }

        @Bean
        public GuestOSDao guestOSDao() {
            return Mockito.mock(GuestOSDao.class);
        }

        @Bean
        public GuestOSCategoryDao guestOSCategoryDao() {
            return Mockito.mock(GuestOSCategoryDao.class);
        }

        @Bean
        public CapacityManager capacityManager() {
            return Mockito.mock(CapacityManager.class);
        }

        @Bean
        public StoragePoolHostDao storagePoolHostDao() {
            return Mockito.mock(StoragePoolHostDao.class);
        }

        @Bean
        public VolumeDao volumeDao() {
            return Mockito.mock(VolumeDao.class);
        }

        @Bean
        public ConfigurationDao configurationDao() {
            return Mockito.mock(ConfigurationDao.class);
        }

        @Bean
        public DiskOfferingDao diskOfferingDao() {
            return Mockito.mock(DiskOfferingDao.class);
        }

        @Bean
        public PrimaryDataStoreDao primaryDataStoreDao() {
            return Mockito.mock(PrimaryDataStoreDao.class);
        }

        @Bean
        public CapacityDao capacityDao() {
            return Mockito.mock(CapacityDao.class);
        }

        @Bean
        public PlannerHostReservationDao plannerHostReservationDao() {
            return Mockito.mock(PlannerHostReservationDao.class);
        }

        @Bean
        public AffinityGroupProcessor affinityGroupProcessor() {
            return Mockito.mock(AffinityGroupProcessor.class);
        }

        @Bean
        public AffinityGroupDao affinityGroupDao() {
            return Mockito.mock(AffinityGroupDao.class);
        }

        @Bean
        public AffinityGroupVMMapDao affinityGroupVMMapDao() {
            return Mockito.mock(AffinityGroupVMMapDao.class);
        }

        @Bean
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public AgentManager agentManager() {
            return Mockito.mock(AgentManager.class);
        }

        @Bean
        public MessageBus messageBus() {
            return Mockito.mock(MessageBus.class);
        }


        @Bean
        public UserVmDao userVMDao() {
            return Mockito.mock(UserVmDao.class);
        }

        @Bean
        public VMInstanceDao vmInstanceDao() {
            return Mockito.mock(VMInstanceDao.class);
        }

        @Bean
        public DataCenterDao dataCenterDao() {
            return Mockito.mock(DataCenterDao.class);
        }

        public static class Library implements TypeFilter {

            @Override
            public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
                ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }
}
