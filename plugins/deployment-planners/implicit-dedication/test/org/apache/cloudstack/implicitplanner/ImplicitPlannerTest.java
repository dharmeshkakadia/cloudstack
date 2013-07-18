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
package org.apache.cloudstack.implicitplanner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.capacity.CapacityManager;
import org.apache.capacity.CapacityVO;
import org.apache.capacity.dao.CapacityDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.dc.ClusterDetailsDao;
import org.apache.dc.DataCenterVO;
import org.apache.dc.dao.ClusterDao;
import org.apache.dc.dao.DataCenterDao;
import org.apache.dc.dao.HostPodDao;
import org.apache.deploy.DataCenterDeployment;
import org.apache.deploy.ImplicitDedicationPlanner;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.exception.InsufficientServerCapacityException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.resource.ResourceManager;
import org.apache.service.ServiceOfferingVO;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.service.dao.ServiceOfferingDetailsDao;
import org.apache.storage.StorageManager;
import org.apache.storage.dao.DiskOfferingDao;
import org.apache.storage.dao.GuestOSCategoryDao;
import org.apache.storage.dao.GuestOSDao;
import org.apache.storage.dao.StoragePoolHostDao;
import org.apache.storage.dao.VolumeDao;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.AccountVO;
import org.apache.user.UserContext;
import org.apache.utils.Pair;
import org.apache.utils.component.ComponentContext;
import org.apache.vm.UserVmVO;
import org.apache.vm.VMInstanceVO;
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
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class ImplicitPlannerTest {

    @Inject
    ImplicitDedicationPlanner planner = new ImplicitDedicationPlanner();
    @Inject
    HostDao hostDao;
    @Inject
    DataCenterDao dcDao;
    @Inject
    HostPodDao podDao;
    @Inject
    ClusterDao clusterDao;
    @Inject
    GuestOSDao guestOSDao;
    @Inject
    GuestOSCategoryDao guestOSCategoryDao;
    @Inject
    DiskOfferingDao diskOfferingDao;
    @Inject
    StoragePoolHostDao poolHostDao;
    @Inject
    UserVmDao vmDao;
    @Inject
    VMInstanceDao vmInstanceDao;
    @Inject
    VolumeDao volsDao;
    @Inject
    CapacityManager capacityMgr;
    @Inject
    ConfigurationDao configDao;
    @Inject
    PrimaryDataStoreDao storagePoolDao;
    @Inject
    CapacityDao capacityDao;
    @Inject
    AccountManager accountMgr;
    @Inject
    StorageManager storageMgr;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    ClusterDetailsDao clusterDetailsDao;
    @Inject
    ServiceOfferingDao serviceOfferingDao;
    @Inject
    ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    ResourceManager resourceMgr;

    private static long domainId = 5L;
    long dataCenterId = 1L;
    long accountId = 200L;
    long offeringId = 12L;
    int noOfCpusInOffering = 1;
    int cpuSpeedInOffering = 500;
    int ramInOffering = 512;
    AccountVO acct = new AccountVO(accountId);

    @BeforeClass
    public static void setUp() throws ConfigurationException {
    }

    @Before
    public void testSetUp() {
        ComponentContext.initComponentsLifeCycle();

        acct.setType(Account.ACCOUNT_TYPE_NORMAL);
        acct.setAccountName("user1");
        acct.setDomainId(domainId);
        acct.setId(accountId);

        UserContext.registerContext(1, acct, null, true);
    }

    @Test
    public void checkWhenDcInAvoidList() throws InsufficientServerCapacityException {
        DataCenterVO mockDc = mock(DataCenterVO.class);
        ExcludeList avoids = mock(ExcludeList.class);
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        VMInstanceVO vm = mock(VMInstanceVO.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);

        when(avoids.shouldAvoid(mockDc)).thenReturn(true);
        when(vmProfile.getVirtualMachine()).thenReturn(vm);
        when(vm.getDataCenterId()).thenReturn(1L);
        when(dcDao.findById(1L)).thenReturn(mockDc);

        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);
        assertTrue("Cluster list should be null/empty if the dc is in avoid list",
                (clusterList == null || clusterList.isEmpty()));
    }

    @Test
    public void checkStrictModeWithCurrentAccountVmsPresent() throws InsufficientServerCapacityException {
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);
        ExcludeList avoids = new ExcludeList();

        initializeForTest(vmProfile, plan);

        initializeForImplicitPlannerTest(false);

        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);

        // Validations.
        // Check cluster 2 and 3 are not in the cluster list.
        // Host 6 and 7 should also be in avoid list.
        //System.out.println("checkStrictModeWithCurrentAccountVmsPresent:: Cluster list should not be empty but ::" + clusterList.toString());
        assertFalse("Cluster list should not be null/empty", (clusterList == null || clusterList.isEmpty()));
        boolean foundNeededCluster = false;
        for (Long cluster : clusterList) {
            if (cluster == 4) {
                fail("Found a cluster that shouldn't have been present, cluster id : " + cluster);
            }else {
                foundNeededCluster = true;
            }
        }
        assertTrue("Didn't find cluster 1 in the list. It should have been present", foundNeededCluster);

        Set<Long> hostsInAvoidList = avoids.getHostsToAvoid();
        assertFalse("Host 5 shouldn't have be in the avoid list, but it is present", hostsInAvoidList.contains(5L));
        Set<Long> hostsThatShouldBeInAvoidList = new HashSet<Long>();
        hostsThatShouldBeInAvoidList.add(6L);
        hostsThatShouldBeInAvoidList.add(7L);
        //System.out.println("checkStrictModeWithCurrentAccountVmsPresent:: Host in avoidlist :: " +  hostsThatShouldBeInAvoidList.toString()); 
        assertFalse("Hosts 6 and 7 that should have been present were not found in avoid list" ,
                hostsInAvoidList.containsAll(hostsThatShouldBeInAvoidList));
    }

    @Test
    public void checkStrictModeHostWithCurrentAccountVmsFull() throws InsufficientServerCapacityException {
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);
        ExcludeList avoids = new ExcludeList();

        initializeForTest(vmProfile, plan);

        initializeForImplicitPlannerTest(false);

        // Mark the host 5 with current account vms to be in avoid list.
        avoids.addHost(5L);
        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);

        // Validations.
        // Check cluster 1 and 3 are not in the cluster list.
        // Host 5 and 7 should also be in avoid list.
        assertFalse("Cluster list should not be null/empty", (clusterList == null || clusterList.isEmpty()));
        boolean foundNeededCluster = false;
        //System.out.println("Cluster list 2 should not be present ::" + clusterList.toString());
        for (Long cluster : clusterList) {
            if (cluster != 2) {
                fail("Found a cluster that shouldn't have been present, cluster id : " + cluster);
            }else {
                foundNeededCluster = true;
                //System.out.println("Cluster list 2 should not be present breaking now" + cluster);
                break;
            }
        }
        assertTrue("Didn't find cluster 2 in the list. It should have been present", foundNeededCluster);

        Set<Long> hostsInAvoidList = avoids.getHostsToAvoid();
        assertFalse("Host 6 shouldn't have be in the avoid list, but it is present", hostsInAvoidList.contains(6L));
        Set<Long> hostsThatShouldBeInAvoidList = new HashSet<Long>();
        hostsThatShouldBeInAvoidList.add(5L);
        hostsThatShouldBeInAvoidList.add(7L);
        assertFalse("Hosts 5 and 7 that should have been present were not found in avoid list" ,
                hostsInAvoidList.containsAll(hostsThatShouldBeInAvoidList));
    }

    @Test
    public void checkStrictModeNoHostsAvailable() throws InsufficientServerCapacityException {
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);
        ExcludeList avoids = new ExcludeList();

        initializeForTest(vmProfile, plan);

        initializeForImplicitPlannerTest(false);

        // Mark the host 5 and 6 to be in avoid list.
        avoids.addHost(5L);
        avoids.addHost(6L);
        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);

        // Validations.
        // Check cluster list is empty.
        //System.out.println("Cluster list should not be empty but  ::" + clusterList.toString());
        assertFalse("Cluster list should not be null/empty", (clusterList == null || clusterList.isEmpty()));
    }

    @Test
    public void checkPreferredModePreferredHostAvailable() throws InsufficientServerCapacityException {
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);
        ExcludeList avoids = new ExcludeList();

        initializeForTest(vmProfile, plan);

        initializeForImplicitPlannerTest(true);

        // Mark the host 5 and 6 to be in avoid list.
        avoids.addHost(5L);
        avoids.addHost(6L);
        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);

        // Validations.
        // Check cluster 1 and 2 are not in the cluster list.
        // Host 5 and 6 should also be in avoid list.
        assertFalse("Cluster list should not be null/empty", (clusterList == null || clusterList.isEmpty()));
        boolean foundNeededCluster = false;
        for (Long cluster : clusterList) {
            if (cluster != 3) {
                fail("Found a cluster that shouldn't have been present, cluster id : " + cluster);
            } else {
                foundNeededCluster = true;
            }
        }
        assertTrue("Didn't find cluster 3 in the list. It should have been present", foundNeededCluster);

        Set<Long> hostsInAvoidList = avoids.getHostsToAvoid();
        assertFalse("Host 7 shouldn't have be in the avoid list, but it is present", hostsInAvoidList.contains(7L));
        Set<Long> hostsThatShouldBeInAvoidList = new HashSet<Long>();
        hostsThatShouldBeInAvoidList.add(5L);
        hostsThatShouldBeInAvoidList.add(6L);
        assertTrue("Hosts 5 and 6 that should have been present were not found in avoid list" ,
                hostsInAvoidList.containsAll(hostsThatShouldBeInAvoidList));
    }

    @Test
    public void checkPreferredModeNoHostsAvailable() throws InsufficientServerCapacityException {
        @SuppressWarnings("unchecked")
        VirtualMachineProfileImpl<VMInstanceVO> vmProfile = mock(VirtualMachineProfileImpl.class);
        DataCenterDeployment plan = mock(DataCenterDeployment.class);
        ExcludeList avoids = new ExcludeList();

        initializeForTest(vmProfile, plan);

        initializeForImplicitPlannerTest(false);

        // Mark the host 5, 6 and 7 to be in avoid list.
        avoids.addHost(5L);
        avoids.addHost(6L);
        avoids.addHost(7L);
        List<Long> clusterList = planner.orderClusters(vmProfile, plan, avoids);

        // Validations.
        // Check cluster list is empty.
        assertTrue("Cluster list should not be null/empty", (clusterList == null || clusterList.isEmpty()));
    }

    private void initializeForTest(VirtualMachineProfileImpl<VMInstanceVO> vmProfile, DataCenterDeployment plan) {
        DataCenterVO mockDc = mock(DataCenterVO.class);
        VMInstanceVO vm = mock(VMInstanceVO.class);
        UserVmVO userVm = mock(UserVmVO.class);
        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);

        AccountVO account = mock(AccountVO.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getAccountId()).thenReturn(accountId);
        when(vmProfile.getOwner()).thenReturn(account);
        when(vmProfile.getVirtualMachine()).thenReturn(vm);
        when(vmProfile.getId()).thenReturn(12L);
        when( vmDao.findById(12L)).thenReturn(userVm);
        when(userVm.getAccountId()).thenReturn(accountId);

        when(vm.getDataCenterId()).thenReturn(dataCenterId);
        when(dcDao.findById(1L)).thenReturn(mockDc);
        when(plan.getDataCenterId()).thenReturn(dataCenterId);
        when(plan.getClusterId()).thenReturn(null);
        when(plan.getPodId()).thenReturn(null);
        when(configDao.getValue(anyString())).thenReturn("false").thenReturn("CPU");

        // Mock offering details.
        when(vmProfile.getServiceOffering()).thenReturn(offering);
        when(offering.getId()).thenReturn(offeringId);
        when(vmProfile.getServiceOfferingId()).thenReturn(offeringId);
        when(offering.getCpu()).thenReturn(noOfCpusInOffering);
        when(offering.getSpeed()).thenReturn(cpuSpeedInOffering);
        when(offering.getRamSize()).thenReturn(ramInOffering);

        List<Long> clustersWithEnoughCapacity = new ArrayList<Long>();
        clustersWithEnoughCapacity.add(1L);
        clustersWithEnoughCapacity.add(2L);
        clustersWithEnoughCapacity.add(3L);
        when(capacityDao.listClustersInZoneOrPodByHostCapacities(dataCenterId, noOfCpusInOffering * cpuSpeedInOffering,
                ramInOffering * 1024L * 1024L, CapacityVO.CAPACITY_TYPE_CPU, true)).thenReturn(clustersWithEnoughCapacity);

        Map<Long, Double> clusterCapacityMap = new HashMap<Long, Double>();
        clusterCapacityMap.put(1L, 2048D);
        clusterCapacityMap.put(2L, 2048D);
        clusterCapacityMap.put(3L, 2048D);
        Pair<List<Long>, Map<Long, Double>> clustersOrderedByCapacity =
                new Pair<List<Long>, Map<Long, Double>>(clustersWithEnoughCapacity, clusterCapacityMap);
        when(capacityDao.orderClustersByAggregateCapacity(dataCenterId, CapacityVO.CAPACITY_TYPE_CPU,
                true)).thenReturn(clustersOrderedByCapacity);

        List<Long> disabledClusters = new ArrayList<Long>();
        List<Long> clustersWithDisabledPods = new ArrayList<Long>();
        when(clusterDao.listDisabledClusters(dataCenterId, null)).thenReturn(disabledClusters);
        when(clusterDao.listClustersWithDisabledPods(dataCenterId)).thenReturn(clustersWithDisabledPods);
    }

    private void initializeForImplicitPlannerTest(boolean preferred) {
        String plannerMode = new String("Strict");
        if (preferred) {
            plannerMode = new String("Preferred");
        }

        Map<String, String> details = new HashMap<String, String>();
        details.put("ImplicitDedicationMode", plannerMode);
        when(serviceOfferingDetailsDao.findDetails(offeringId)).thenReturn(details);

        // Initialize hosts in clusters
        HostVO host1 = mock(HostVO.class);
        when(host1.getId()).thenReturn(5L);
        HostVO host2 = mock(HostVO.class);
        when(host2.getId()).thenReturn(6L);
        HostVO host3 = mock(HostVO.class);
        when(host3.getId()).thenReturn(7L);
        List<HostVO> hostsInCluster1 = new ArrayList<HostVO>();
        List<HostVO> hostsInCluster2 = new ArrayList<HostVO>();
        List<HostVO> hostsInCluster3 = new ArrayList<HostVO>();
        hostsInCluster1.add(host1);
        hostsInCluster2.add(host2);
        hostsInCluster3.add(host3);
        when(resourceMgr.listAllHostsInCluster(1)).thenReturn(hostsInCluster1);
        when(resourceMgr.listAllHostsInCluster(2)).thenReturn(hostsInCluster2);
        when(resourceMgr.listAllHostsInCluster(3)).thenReturn(hostsInCluster3);

        // Mock vms on each host.
        long offeringIdForVmsOfThisAccount = 15L;
        long offeringIdForVmsOfOtherAccount = 16L;
        UserVmVO vm1 = mock(UserVmVO.class);
        when(vm1.getAccountId()).thenReturn(accountId);
        when(vm1.getServiceOfferingId()).thenReturn(offeringIdForVmsOfThisAccount);
        UserVmVO vm2 = mock(UserVmVO.class);
        when(vm2.getAccountId()).thenReturn(accountId);
        when(vm2.getServiceOfferingId()).thenReturn(offeringIdForVmsOfThisAccount);
        // Vm from different account
        UserVmVO vm3 = mock(UserVmVO.class);
        when(vm3.getAccountId()).thenReturn(201L);
        when(vm3.getServiceOfferingId()).thenReturn(offeringIdForVmsOfOtherAccount);
        List<UserVmVO> userVmsForHost1 = new ArrayList<UserVmVO>();
        List<UserVmVO> userVmsForHost2 = new ArrayList<UserVmVO>();
        List<UserVmVO> userVmsForHost3 = new ArrayList<UserVmVO>();
        List<UserVmVO> stoppedVmsForHost = new ArrayList<UserVmVO>();
        // Host 2 is empty.
        userVmsForHost1.add(vm1);
        userVmsForHost1.add(vm2);
        userVmsForHost3.add(vm3);
        when(vmDao.listUpByHostId(5L)).thenReturn(userVmsForHost1);
        when(vmDao.listUpByHostId(6L)).thenReturn(userVmsForHost2);
        when(vmDao.listUpByHostId(7L)).thenReturn(userVmsForHost3);
        when(vmDao.listByLastHostId(5L)).thenReturn(stoppedVmsForHost);
        when(vmDao.listByLastHostId(6L)).thenReturn(stoppedVmsForHost);
        when(vmDao.listByLastHostId(7L)).thenReturn(stoppedVmsForHost);

        // Mock the offering with which the vm was created.
        ServiceOfferingVO offeringForVmOfThisAccount = mock(ServiceOfferingVO.class);
        when(serviceOfferingDao.findByIdIncludingRemoved(offeringIdForVmsOfThisAccount)).thenReturn(offeringForVmOfThisAccount);
        when(offeringForVmOfThisAccount.getDeploymentPlanner()).thenReturn(planner.getName());

        ServiceOfferingVO offeringForVMOfOtherAccount = mock(ServiceOfferingVO.class);
        when(serviceOfferingDao.findByIdIncludingRemoved(offeringIdForVmsOfOtherAccount)).thenReturn(offeringForVMOfOtherAccount);
        when(offeringForVMOfOtherAccount.getDeploymentPlanner()).thenReturn("FirstFitPlanner");
    }

    @Configuration
    @ComponentScan(basePackageClasses = { ImplicitDedicationPlanner.class },
        includeFilters = {@Filter(value = TestConfiguration.Library.class, type = FilterType.CUSTOM)},
        useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {

        @Bean
        public HostDao hostDao() {
            return Mockito.mock(HostDao.class);
        }

        @Bean
        public DataCenterDao dcDao() {
            return Mockito.mock(DataCenterDao.class);
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
        public GuestOSDao guestOsDao() {
            return Mockito.mock(GuestOSDao.class);
        }

        @Bean
        public GuestOSCategoryDao guestOsCategoryDao() {
            return Mockito.mock(GuestOSCategoryDao.class);
        }

        @Bean
        public DiskOfferingDao diskOfferingDao() {
            return Mockito.mock(DiskOfferingDao.class);
        }

        @Bean
        public StoragePoolHostDao storagePoolHostDao() {
            return Mockito.mock(StoragePoolHostDao.class);
        }

        @Bean
        public UserVmDao userVmDao() {
            return Mockito.mock(UserVmDao.class);
        }

        @Bean
        public VMInstanceDao vmInstanceDao() {
            return Mockito.mock(VMInstanceDao.class);
        }

        @Bean
        public VolumeDao volumeDao() {
            return Mockito.mock(VolumeDao.class);
        }

        @Bean
        public CapacityManager capacityManager() {
            return Mockito.mock(CapacityManager.class);
        }

        @Bean
        public ConfigurationDao configurationDao() {
            return Mockito.mock(ConfigurationDao.class);
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
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public StorageManager storageManager() {
            return Mockito.mock(StorageManager.class);
        }

        @Bean
        public DataStoreManager dataStoreManager() {
            return Mockito.mock(DataStoreManager.class);
        }

        @Bean
        public ClusterDetailsDao clusterDetailsDao() {
            return Mockito.mock(ClusterDetailsDao.class);
        }

        @Bean
        public ServiceOfferingDao serviceOfferingDao() {
            return Mockito.mock(ServiceOfferingDao.class);
        }

        @Bean
        public ServiceOfferingDetailsDao serviceOfferingDetailsDao() {
            return Mockito.mock(ServiceOfferingDetailsDao.class);
        }

        @Bean
        public ResourceManager resourceManager() {
            return Mockito.mock(ResourceManager.class);
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
