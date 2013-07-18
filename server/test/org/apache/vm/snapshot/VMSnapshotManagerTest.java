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
package org.apache.vm.snapshot;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.CreateVMSnapshotAnswer;
import org.apache.agent.api.CreateVMSnapshotCommand;
import org.apache.agent.api.to.VolumeTO;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.OperationTimedoutException;
import org.apache.exception.ResourceAllocationException;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor;
import org.apache.hypervisor.HypervisorGuruManager;
import org.apache.hypervisor.dao.HypervisorCapabilitiesDao;
import org.apache.storage.GuestOSVO;
import org.apache.storage.Snapshot;
import org.apache.storage.SnapshotVO;
import org.apache.storage.VolumeVO;
import org.apache.storage.dao.GuestOSDao;
import org.apache.storage.dao.SnapshotDao;
import org.apache.storage.dao.VolumeDao;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.dao.AccountDao;
import org.apache.user.dao.UserDao;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.fsm.NoTransitionException;
import org.apache.vm.UserVmVO;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.VirtualMachine.State;
import org.apache.vm.dao.UserVmDao;
import org.apache.vm.dao.VMInstanceDao;
import org.apache.vm.snapshot.VMSnapshot;
import org.apache.vm.snapshot.VMSnapshotManagerImpl;
import org.apache.vm.snapshot.VMSnapshotVO;
import org.apache.vm.snapshot.dao.VMSnapshotDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.amazonaws.services.ec2.model.HypervisorType;



public class VMSnapshotManagerTest {
    @Spy VMSnapshotManagerImpl _vmSnapshotMgr = new VMSnapshotManagerImpl();
    @Mock Account admin;
    @Mock VMSnapshotDao _vmSnapshotDao;
    @Mock VolumeDao _volumeDao;
    @Mock AccountDao _accountDao;
    @Mock VMInstanceDao _vmInstanceDao;
    @Mock UserVmDao _userVMDao;
    @Mock HostDao _hostDao;
    @Mock UserDao _userDao;
    @Mock AgentManager _agentMgr;
    @Mock HypervisorGuruManager _hvGuruMgr;
    @Mock AccountManager _accountMgr;
    @Mock GuestOSDao _guestOSDao;
    @Mock PrimaryDataStoreDao _storagePoolDao;
    @Mock SnapshotDao _snapshotDao;
    @Mock VirtualMachineManager _itMgr;
    @Mock ConfigurationDao _configDao;
    @Mock HypervisorCapabilitiesDao _hypervisorCapabilitiesDao;
    int _vmSnapshotMax = 10;

    private static long TEST_VM_ID = 3L;
    @Mock UserVmVO vmMock;
    @Mock VolumeVO volumeMock;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        doReturn(admin).when(_vmSnapshotMgr).getCaller();
        _vmSnapshotMgr._accountDao = _accountDao;
        _vmSnapshotMgr._userVMDao = _userVMDao;
        _vmSnapshotMgr._vmSnapshotDao = _vmSnapshotDao;
        _vmSnapshotMgr._volumeDao = _volumeDao;
        _vmSnapshotMgr._accountMgr = _accountMgr;
        _vmSnapshotMgr._snapshotDao = _snapshotDao;
        _vmSnapshotMgr._guestOSDao = _guestOSDao;
        _vmSnapshotMgr._hypervisorCapabilitiesDao = _hypervisorCapabilitiesDao;

        doNothing().when(_accountMgr).checkAccess(any(Account.class), any(AccessType.class),
                any(Boolean.class), any(ControlledEntity.class));

        _vmSnapshotMgr._vmSnapshotMax = _vmSnapshotMax;

        when(_userVMDao.findById(anyLong())).thenReturn(vmMock);
        when(_vmSnapshotDao.findByName(anyLong(), anyString())).thenReturn(null);
        when(_vmSnapshotDao.findByVm(anyLong())).thenReturn(new ArrayList<VMSnapshotVO>());
        when(_hypervisorCapabilitiesDao.isVmSnapshotEnabled(Hypervisor.HypervisorType.XenServer, "default")).thenReturn(true);
        
        List<VolumeVO> mockVolumeList = new ArrayList<VolumeVO>();
        mockVolumeList.add(volumeMock);
        when(volumeMock.getInstanceId()).thenReturn(TEST_VM_ID);
        when(_volumeDao.findByInstance(anyLong())).thenReturn(mockVolumeList);

        when(vmMock.getInstanceName()).thenReturn("i-3-VM-TEST");
        when(vmMock.getState()).thenReturn(State.Running);
        when(vmMock.getHypervisorType()).thenReturn(Hypervisor.HypervisorType.XenServer);
        when(_guestOSDao.findById(anyLong())).thenReturn(mock(GuestOSVO.class));
    }

    // vmId null case
    @Test(expected=InvalidParameterValueException.class)
    public void testAllocVMSnapshotF1() throws ResourceAllocationException{
        when(_userVMDao.findById(TEST_VM_ID)).thenReturn(null);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }

    // hypervisorCapabilities not expected case
    @Test(expected=InvalidParameterValueException.class)
    public void testAllocVMSnapshotF6() throws ResourceAllocationException{
        when(vmMock.getHypervisorType()).thenReturn(Hypervisor.HypervisorType.Ovm);
        when(_hypervisorCapabilitiesDao.isVmSnapshotEnabled(Hypervisor.HypervisorType.Ovm, "default")).thenReturn(false);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }
    
    // vm state not in [running, stopped] case
    @Test(expected=InvalidParameterValueException.class)
    public void testAllocVMSnapshotF2() throws ResourceAllocationException{
        when(vmMock.getState()).thenReturn(State.Starting);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }

    // VM in stopped state & snapshotmemory case
    @Test(expected=InvalidParameterValueException.class)
    public void testCreateVMSnapshotF3() throws AgentUnavailableException, OperationTimedoutException, ResourceAllocationException{
        when(vmMock.getState()).thenReturn(State.Stopped);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }

    // max snapshot limit case
    @SuppressWarnings("unchecked")
    @Test(expected=CloudRuntimeException.class)
    public void testAllocVMSnapshotF4() throws ResourceAllocationException{
        List<VMSnapshotVO> mockList = mock(List.class);
        when(mockList.size()).thenReturn(10);
        when(_vmSnapshotDao.findByVm(TEST_VM_ID)).thenReturn(mockList);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }

    // active volume snapshots case
    @SuppressWarnings("unchecked")
    @Test(expected=CloudRuntimeException.class)
    public void testAllocVMSnapshotF5() throws ResourceAllocationException{
        List<SnapshotVO> mockList = mock(List.class);
        when(mockList.size()).thenReturn(1);
        when(_snapshotDao.listByInstanceId(TEST_VM_ID,Snapshot.State.Creating,
                Snapshot.State.CreatedOnPrimary, Snapshot.State.BackingUp)).thenReturn(mockList);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);
    }

    // successful creation case
    @Test
    public void testCreateVMSnapshot() throws AgentUnavailableException, OperationTimedoutException, ResourceAllocationException, NoTransitionException{
        when(vmMock.getState()).thenReturn(State.Running);
        _vmSnapshotMgr.allocVMSnapshot(TEST_VM_ID,"","",true);

        when(_vmSnapshotDao.findCurrentSnapshotByVmId(anyLong())).thenReturn(null);
        doReturn(new ArrayList<VolumeTO>()).when(_vmSnapshotMgr).getVolumeTOList(anyLong());
        doReturn(new CreateVMSnapshotAnswer(null,true,"")).when(_vmSnapshotMgr).sendToPool(anyLong(), any(CreateVMSnapshotCommand.class));
        doNothing().when(_vmSnapshotMgr).processAnswer(any(VMSnapshotVO.class),
                any(UserVmVO.class), any(Answer.class), anyLong());
        doReturn(true).when(_vmSnapshotMgr).vmSnapshotStateTransitTo(any(VMSnapshotVO.class),any(VMSnapshot.Event.class));
        _vmSnapshotMgr.createVmSnapshotInternal(vmMock, mock(VMSnapshotVO.class), 5L);
    }

}
