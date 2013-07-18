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
package org.apache.cloudstack.internallbelement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.apache.agent.api.to.LoadBalancerTO;
import org.apache.cloudstack.lb.ApplicationLoadBalancerRuleVO;
import org.apache.cloudstack.network.element.InternalLoadBalancerElement;
import org.apache.cloudstack.network.lb.InternalLoadBalancerVMManager;
import org.apache.configuration.ConfigurationManager;
import org.apache.dc.DataCenterVO;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.VirtualRouterProvider.VirtualRouterProviderType;
import org.apache.network.addr.PublicIp;
import org.apache.network.dao.NetworkVO;
import org.apache.network.dao.PhysicalNetworkServiceProviderDao;
import org.apache.network.dao.PhysicalNetworkServiceProviderVO;
import org.apache.network.dao.VirtualRouterProviderDao;
import org.apache.network.element.VirtualRouterProviderVO;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.LoadBalancerContainer.Scheme;
import org.apache.user.AccountManager;
import org.apache.utils.component.ComponentContext;
import org.apache.utils.net.Ip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/lb_element.xml")
public class InternalLbElementTest {
    //The class to test
    @Inject InternalLoadBalancerElement _lbEl;
    
    //Mocked interfaces
    @Inject AccountManager _accountMgr;
    @Inject VirtualRouterProviderDao _vrProviderDao;
    @Inject PhysicalNetworkServiceProviderDao _pNtwkProviderDao;
    @Inject InternalLoadBalancerVMManager _internalLbMgr;
    @Inject ConfigurationManager _configMgr;
    
    long validElId = 1L;
    long nonExistingElId = 2L;
    long invalidElId = 3L; //not of VirtualRouterProviderType
    long notEnabledElId = 4L;
    
    long validProviderId = 1L;
    long nonExistingProviderId = 2L;
    long invalidProviderId = 3L;
        

    @Before
    public void setUp() {
        
        ComponentContext.initComponentsLifeCycle();
        VirtualRouterProviderVO validElement = new VirtualRouterProviderVO(1, VirtualRouterProviderType.InternalLbVm);
        validElement.setEnabled(true);
        VirtualRouterProviderVO invalidElement = new VirtualRouterProviderVO(1, VirtualRouterProviderType.VirtualRouter);
        VirtualRouterProviderVO notEnabledElement = new VirtualRouterProviderVO(1, VirtualRouterProviderType.InternalLbVm);
 
        Mockito.when(_vrProviderDao.findByNspIdAndType(validElId, VirtualRouterProviderType.InternalLbVm)).thenReturn(validElement);
        Mockito.when(_vrProviderDao.findByNspIdAndType(invalidElId, VirtualRouterProviderType.InternalLbVm)).thenReturn(invalidElement);
        Mockito.when(_vrProviderDao.findByNspIdAndType(notEnabledElId, VirtualRouterProviderType.InternalLbVm)).thenReturn(notEnabledElement);

        Mockito.when(_vrProviderDao.persist(validElement)).thenReturn(validElement);
        
        Mockito.when(_vrProviderDao.findByNspIdAndType(validProviderId, VirtualRouterProviderType.InternalLbVm)).thenReturn(validElement);
        
        PhysicalNetworkServiceProviderVO validProvider = new PhysicalNetworkServiceProviderVO(1, "InternalLoadBalancerElement");
        PhysicalNetworkServiceProviderVO invalidProvider = new PhysicalNetworkServiceProviderVO(1, "Invalid name!");

        Mockito.when(_pNtwkProviderDao.findById(validProviderId)).thenReturn(validProvider);
        Mockito.when(_pNtwkProviderDao.findById(invalidProviderId)).thenReturn(invalidProvider);
        
        Mockito.when(_vrProviderDao.persist(Mockito.any(VirtualRouterProviderVO.class))).thenReturn(validElement);
        
        DataCenterVO dc = new DataCenterVO
                (1L, null, null, null, null, null, null, null, null, null, NetworkType.Advanced, null, null);
        Mockito.when(_configMgr.getZone(Mockito.anyLong())).thenReturn(dc);
    }
    
    //TEST FOR getProvider() method
    
    @Test 
    public void verifyProviderName() {  
       Provider pr = _lbEl.getProvider();
       assertEquals("Wrong provider is returned", pr.getName(), Provider.InternalLbVm.getName());
    }
    
    //TEST FOR isReady() METHOD
    
    @Test 
    public void verifyValidProviderState() {
       PhysicalNetworkServiceProviderVO provider = new PhysicalNetworkServiceProviderVO();
       provider = setId(provider, validElId);
       boolean isReady = _lbEl.isReady(provider);
       assertTrue("Valid provider is returned as not ready", isReady);
    }
    
    
    @Test 
    public void verifyNonExistingProviderState() {
       PhysicalNetworkServiceProviderVO provider = new PhysicalNetworkServiceProviderVO();
       provider = setId(provider, nonExistingElId);
       boolean isReady = _lbEl.isReady(provider);
       assertFalse("Non existing provider is returned as ready", isReady);
    }
    
    
    @Test 
    public void verifyInvalidProviderState() {
       PhysicalNetworkServiceProviderVO provider = new PhysicalNetworkServiceProviderVO();
       provider = setId(provider, invalidElId);
       boolean isReady = _lbEl.isReady(provider);
       assertFalse("Not valid provider is returned as ready", isReady);
    }
    
    @Test 
    public void verifyNotEnabledProviderState() {
       PhysicalNetworkServiceProviderVO provider = new PhysicalNetworkServiceProviderVO();
       provider = setId(provider, notEnabledElId);
       boolean isReady = _lbEl.isReady(provider);
       assertFalse("Not enabled provider is returned as ready", isReady);
    }
    
    //TEST FOR canEnableIndividualServices METHOD
    @Test 
    public void verifyCanEnableIndividualSvc() {  
       boolean result = _lbEl.canEnableIndividualServices();
       assertTrue("Wrong value is returned by canEnableIndividualSvc", result);
    }
    
    //TEST FOR verifyServicesCombination METHOD
    @Test 
    public void verifyServicesCombination() {  
       boolean result = _lbEl.verifyServicesCombination(new HashSet<Service>());
       assertTrue("Wrong value is returned by verifyServicesCombination", result);
    }
    
    
    //TEST FOR applyIps METHOD
    @Test 
    public void verifyApplyIps() throws ResourceUnavailableException {
       List<PublicIp> ips = new ArrayList<PublicIp>();
       boolean result = _lbEl.applyIps(new NetworkVO(), ips, new HashSet<Service>());
       assertTrue("Wrong value is returned by applyIps method", result);
    }
    
    
    //TEST FOR updateHealthChecks METHOD
    @Test 
    public void verifyUpdateHealthChecks() throws ResourceUnavailableException {
       List<LoadBalancerTO> check = _lbEl.updateHealthChecks(new NetworkVO(), new ArrayList<LoadBalancingRule>());
       assertNull("Wrong value is returned by updateHealthChecks method", check);
    }
    
    //TEST FOR validateLBRule METHOD
    @Test 
    public void verifyValidateLBRule() throws ResourceUnavailableException {
        ApplicationLoadBalancerRuleVO lb = new ApplicationLoadBalancerRuleVO(null, null, 22, 22, "roundrobin",
                1L, 1L, 1L, new Ip("10.10.10.1"), 1L, Scheme.Internal);
        lb.setState(FirewallRule.State.Add);
        
        LoadBalancingRule rule = new LoadBalancingRule(lb, null,
                null, null, new Ip("10.10.10.1"));
        
        
        boolean result = _lbEl.validateLBRule(new NetworkVO(), rule);
        assertTrue("Wrong value is returned by validateLBRule method", result);
    }
    
    
    private static PhysicalNetworkServiceProviderVO setId(PhysicalNetworkServiceProviderVO vo, long id) {
        PhysicalNetworkServiceProviderVO voToReturn = vo;
        Class<?> c = voToReturn.getClass();
        try {
            Field f = c.getDeclaredField("id");
            f.setAccessible(true);
            f.setLong(voToReturn, id);
        } catch (NoSuchFieldException ex) {
           return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
        
        return voToReturn;
    }
    
    
    
}


