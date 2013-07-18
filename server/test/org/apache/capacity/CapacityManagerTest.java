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

package org.apache.capacity;
import org.apache.capacity.Capacity;
import org.apache.capacity.CapacityManagerImpl;
import org.apache.capacity.CapacityVO;
import org.apache.capacity.dao.CapacityDao;
import org.apache.dc.ClusterDetailsDao;
import org.apache.dc.ClusterDetailsVO;
import org.apache.log4j.Logger;
import org.apache.service.ServiceOfferingVO;
import org.apache.service.dao.ServiceOfferingDao;
import org.apache.vm.UserVmDetailVO;
import org.apache.vm.VirtualMachine;
import org.apache.vm.dao.UserVmDetailsDao;
import org.junit.*;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;


public class CapacityManagerTest {
    CapacityDao CDao = mock(CapacityDao.class);
    ServiceOfferingDao SOfferingDao = mock(ServiceOfferingDao.class);
    ClusterDetailsDao ClusterDetailsDao= mock(org.apache.dc.ClusterDetailsDao.class);
    CapacityManagerImpl capMgr;
    private ServiceOfferingVO svo = mock(ServiceOfferingVO.class);
    private CapacityVO cvo_cpu =    mock(CapacityVO.class);
    private CapacityVO cvo_ram =    mock(CapacityVO.class);
    private VirtualMachine vm =     mock(VirtualMachine.class);
    private ClusterDetailsVO cluster_detail_cpu = mock(ClusterDetailsVO.class);
    private ClusterDetailsVO cluster_detail_ram =  mock(ClusterDetailsVO.class);

    public CapacityManagerImpl  setUp() {
        CapacityManagerImpl  capMgr = new CapacityManagerImpl();
        ((CapacityManagerImpl)capMgr)._clusterDetailsDao= ClusterDetailsDao;
        capMgr._capacityDao = CDao;
        capMgr._offeringsDao = SOfferingDao;
        return capMgr;
    }

    @Test
    public void allocateCapacityTest(){
        capMgr=setUp();
        when(vm.getHostId()).thenReturn(1l);
        when(vm.getServiceOfferingId()).thenReturn(2l);
        when(SOfferingDao.findById(anyLong())).thenReturn(svo);
        when(CDao.findByHostIdType(anyLong(), eq(Capacity.CAPACITY_TYPE_CPU))).thenReturn(cvo_cpu);
        when(CDao.findByHostIdType(anyLong(), eq(Capacity.CAPACITY_TYPE_MEMORY))).thenReturn(cvo_ram);
        when(cvo_cpu.getUsedCapacity()).thenReturn(500l);
        when(cvo_cpu.getTotalCapacity()).thenReturn(2000l);
        when(cvo_ram.getUsedCapacity()).thenReturn(3000l);
        when(cvo_ram.getTotalCapacity()).thenReturn((long) 1024*1024*1024);
        when(svo.getCpu()).thenReturn(500);
        when(svo.getRamSize()).thenReturn(512);
        when(cvo_cpu.getReservedCapacity()).thenReturn(0l);
        when(cvo_ram.getReservedCapacity()).thenReturn(0l);
        when(cluster_detail_ram.getValue()).thenReturn("1.5");
        when(cluster_detail_cpu.getValue()).thenReturn("2");
        when(CDao.update(anyLong(), isA(CapacityVO.class))).thenReturn(true) ;
        boolean hasCapacity=capMgr.checkIfHostHasCapacity(1l,500,1024*1024*1024,false,2,2,false);
        Assert.assertTrue(hasCapacity);

    }
}
