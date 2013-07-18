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
package org.apache.cloudstack.internallbvmmgr;

import java.lang.reflect.Field;
import java.util.Map;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.apache.cloudstack.network.lb.InternalLoadBalancerVMService;
import org.apache.deploy.DeploymentPlan;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.OperationTimedoutException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.exception.StorageUnavailableException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.router.VirtualRouter;
import org.apache.network.router.VirtualRouter.Role;
import org.apache.service.ServiceOfferingVO;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.AccountVO;
import org.apache.user.User;
import org.apache.user.UserContext;
import org.apache.user.UserVO;
import org.apache.user.dao.AccountDao;
import org.apache.utils.component.ComponentContext;
import org.apache.vm.DomainRouterVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.dao.DomainRouterDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Set of unittests for InternalLoadBalancerVMService
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/lb_svc.xml")
@SuppressWarnings("unchecked")
public class InternalLBVMServiceTest extends TestCase {
    //The interface to test
    @Inject InternalLoadBalancerVMService _lbVmSvc;
    
    //Mocked interfaces
    @Inject AccountManager _accountMgr;
    @Inject ServiceOfferingDao _svcOffDao;
    @Inject DomainRouterDao _domainRouterDao;
    @Inject VirtualMachineManager _itMgr;
    @Inject AccountDao _accountDao;
    
    long validVmId = 1L;
    long nonExistingVmId = 2L;
    long nonInternalLbVmId = 3L;
    
    @Before
    public void setUp() {
        //mock system offering creation as it's used by configure() method called by initComponentsLifeCycle
        Mockito.when(_accountMgr.getAccount(1L)).thenReturn(new AccountVO());
        ServiceOfferingVO off = new ServiceOfferingVO("alena", 1, 1,
                1, 1, 1, false, "alena", false, false, null, false, VirtualMachine.Type.InternalLoadBalancerVm, false);
        off = setId(off, 1);
        Mockito.when(_svcOffDao.persistSystemServiceOffering(Mockito.any(ServiceOfferingVO.class))).thenReturn(off);
        
        ComponentContext.initComponentsLifeCycle();
        
        Mockito.when(_accountMgr.getSystemUser()).thenReturn(new UserVO(1));
        Mockito.when(_accountMgr.getSystemAccount()).thenReturn(new AccountVO(2));
        Mockito.when(_accountDao.findByIdIncludingRemoved(Mockito.anyLong())).thenReturn(new AccountVO(2));
        UserContext.registerContext(_accountMgr.getSystemUser().getId(), _accountMgr.getSystemAccount(), null, false);
        
        
        DomainRouterVO validVm = new DomainRouterVO(validVmId,off.getId(),1,"alena",1,HypervisorType.XenServer,1,1,1,
                false, 0,false,null,false,false,
                VirtualMachine.Type.InternalLoadBalancerVm, null);
        validVm.setRole(Role.INTERNAL_LB_VM);
        DomainRouterVO nonInternalLbVm = new DomainRouterVO(validVmId,off.getId(),1,"alena",1,HypervisorType.XenServer,1,1,1,
                false, 0,false,null,false,false,
                VirtualMachine.Type.DomainRouter, null);
        nonInternalLbVm.setRole(Role.VIRTUAL_ROUTER);
        
        Mockito.when(_domainRouterDao.findById(validVmId)).thenReturn(validVm);
        Mockito.when(_domainRouterDao.findById(nonExistingVmId)).thenReturn(null);
        Mockito.when(_domainRouterDao.findById(nonInternalLbVmId)).thenReturn(nonInternalLbVm);
        
        try {
            Mockito.when(_itMgr.start(Mockito.any(DomainRouterVO.class),
                    Mockito.any(Map.class), Mockito.any(User.class), Mockito.any(Account.class), Mockito.any(DeploymentPlan.class))).thenReturn(validVm);
        } catch (InsufficientCapacityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            Mockito.when(_itMgr.advanceStop(Mockito.any(DomainRouterVO.class), Mockito.any(Boolean.class), Mockito.any(User.class), Mockito.any(Account.class))).thenReturn(true);
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationTimedoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    //TESTS FOR START COMMAND
    
    
    @Test (expected = InvalidParameterValueException.class)
    public void startNonExistingVm() {
        String expectedExcText = null;
        try {
            _lbVmSvc.startInternalLbVm(nonExistingVmId, _accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InsufficientCapacityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test (expected = InvalidParameterValueException.class)
    public void startNonInternalLbVmVm() {
        String expectedExcText = null;
        try {
            _lbVmSvc.startInternalLbVm(nonInternalLbVmId, _accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InsufficientCapacityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void startValidLbVmVm() {
        VirtualRouter vr = null;
        try {
            vr = _lbVmSvc.startInternalLbVm(validVmId, _accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InsufficientCapacityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            assertNotNull("Internal LB vm is null which means it failed to start " + vr, vr);
        }
    }
    
    
    //TEST FOR STOP COMMAND
    @Test (expected = InvalidParameterValueException.class)
    public void stopNonExistingVm() {
        String expectedExcText = null;
        try {
            _lbVmSvc.stopInternalLbVm(nonExistingVmId, false,_accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    @Test (expected = InvalidParameterValueException.class)
    public void stopNonInternalLbVmVm() {
        String expectedExcText = null;
        try {
            _lbVmSvc.stopInternalLbVm(nonInternalLbVmId, false, _accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    @Test
    public void stopValidLbVmVm() {
        VirtualRouter vr = null;
        try {
            vr = _lbVmSvc.stopInternalLbVm(validVmId, false, _accountMgr.getAccount(1L), 1L);
        } catch (StorageUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConcurrentOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            assertNotNull("Internal LB vm is null which means it failed to stop " + vr, vr);
        }
    }
    
    
    
    private static ServiceOfferingVO setId(ServiceOfferingVO vo, long id) {
        ServiceOfferingVO voToReturn = vo;
        Class<?> c = voToReturn.getClass();
        try {
            Field f = c.getSuperclass().getDeclaredField("id");
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
