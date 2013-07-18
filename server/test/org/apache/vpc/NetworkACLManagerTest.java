// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.vpc;

import junit.framework.TestCase;

import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.configuration.ConfigurationManager;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkVO;
import org.apache.network.element.NetworkACLServiceProvider;
import org.apache.network.vpc.NetworkACLItem;
import org.apache.network.vpc.NetworkACLItemDao;
import org.apache.network.vpc.NetworkACLItemVO;
import org.apache.network.vpc.NetworkACLManager;
import org.apache.network.vpc.NetworkACLManagerImpl;
import org.apache.network.vpc.NetworkACLVO;
import org.apache.network.vpc.VpcManager;
import org.apache.network.vpc.dao.NetworkACLDao;
import org.apache.network.vpc.dao.VpcGatewayDao;
import org.apache.tags.dao.ResourceTagDao;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.AccountVO;
import org.apache.user.UserContext;
import org.apache.utils.component.ComponentContext;
import org.apache.utils.exception.CloudRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class NetworkACLManagerTest extends TestCase{
    @Inject
    NetworkACLManager _aclMgr;

    @Inject
    AccountManager _accountMgr;
    @Inject
    VpcManager _vpcMgr;
    @Inject
    NetworkACLDao _networkACLDao;
    @Inject
    NetworkACLItemDao _networkACLItemDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    NetworkModel _networkModel;
    @Inject
    List<NetworkACLServiceProvider> _networkAclElements;

    private NetworkACLVO acl;
    private NetworkACLItemVO aclItem;

    private static final Logger s_logger = Logger.getLogger( NetworkACLManagerTest.class);

    @Before
    public void setUp() {
        ComponentContext.initComponentsLifeCycle();
        Account account = new AccountVO("testaccount", 1, "testdomain", (short) 0, UUID.randomUUID().toString());
        UserContext.registerContext(1, account, null, true);
        acl = Mockito.mock(NetworkACLVO.class);
        aclItem = Mockito.mock(NetworkACLItemVO.class);
    }

    @Test
    public void testCreateACL() throws Exception {
        Mockito.when(_networkACLDao.persist(Mockito.any(NetworkACLVO.class))).thenReturn(acl);
        assertNotNull(_aclMgr.createNetworkACL("acl_new", "acl desc", 1L));
    }

    @Test
    public void testApplyACL() throws Exception {
        NetworkVO network = Mockito.mock(NetworkVO.class);
        Mockito.when(_networkDao.findById(Mockito.anyLong())).thenReturn(network);
        Mockito.when(_networkModel.isProviderSupportServiceInNetwork(Mockito.anyLong(), Mockito.any(Network.Service.class), Mockito.any(Network.Provider.class))).thenReturn(true);
        Mockito.when(_networkAclElements.get(0).applyNetworkACLs(Mockito.any(Network.class), Mockito.anyList())).thenReturn(true);
        assertTrue(_aclMgr.applyACLToNetwork(1L));
    }

    @Test
    public void testRevokeACLItem() throws Exception {
        Mockito.when(_networkACLItemDao.findById(Mockito.anyLong())).thenReturn(aclItem);
        assertTrue(_aclMgr.revokeNetworkACLItem(1L));
    }

    @Test
    public void testUpdateACLItem() throws Exception {
        Mockito.when(_networkACLItemDao.findById(Mockito.anyLong())).thenReturn(aclItem);
        Mockito.when(_networkACLItemDao.update(Mockito.anyLong(), Mockito.any(NetworkACLItemVO.class))).thenReturn(true);
        assertNotNull(_aclMgr.updateNetworkACLItem(1L, "UDP", null, NetworkACLItem.TrafficType.Ingress, "Deny", 10, 22, 32, null, null));
    }

    @Test(expected = CloudRuntimeException.class)
    public void deleteNonEmptyACL() throws Exception {
        List<NetworkACLItemVO> aclItems = new ArrayList<NetworkACLItemVO>();
        aclItems.add(aclItem);
        Mockito.when(_networkACLItemDao.listByACL(Mockito.anyLong())).thenReturn(aclItems);
        _aclMgr.deleteNetworkACL(acl);
    }

    @Configuration
    @ComponentScan(basePackageClasses={NetworkACLManagerImpl.class},
            includeFilters={@ComponentScan.Filter(value=NetworkACLTestConfiguration.Library.class, type= FilterType.CUSTOM)},
            useDefaultFilters=false)
    public static class NetworkACLTestConfiguration extends SpringUtils.CloudStackTestConfiguration{

        @Bean
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public NetworkManager networkManager() {
            return Mockito.mock(NetworkManager.class);
        }

        @Bean
        public NetworkModel networkModel() {
            return Mockito.mock(NetworkModel.class);
        }

        @Bean
        public VpcManager vpcManager() {
            return Mockito.mock(VpcManager.class);
        }

        @Bean
        public ResourceTagDao resourceTagDao() {
            return Mockito.mock(ResourceTagDao.class);
        }

        @Bean
        public NetworkACLDao networkACLDao() {
            return Mockito.mock(NetworkACLDao.class);
        }

        @Bean
        public NetworkACLItemDao networkACLItemDao() {
            return Mockito.mock(NetworkACLItemDao.class);
        }

        @Bean
        public NetworkDao networkDao() {
            return Mockito.mock(NetworkDao.class);
        }

        @Bean
        public ConfigurationManager configMgr() {
            return Mockito.mock(ConfigurationManager.class);
        }

        @Bean
        public NetworkACLServiceProvider networkElements() {
            return Mockito.mock(NetworkACLServiceProvider.class);
        }

        @Bean
        public VpcGatewayDao vpcGatewayDao () {
            return Mockito.mock(VpcGatewayDao.class);
        }

        public static class Library implements TypeFilter {
            @Override
            public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
                mdr.getClassMetadata().getClassName();
                ComponentScan cs = NetworkACLTestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }

}
