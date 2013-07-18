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
package org.apache.agent.manager.allocator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.manager.allocator.HostAllocator;
import org.apache.deploy.DeploymentPlan;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.Host.Type;
import org.apache.host.dao.HostDao;
import org.apache.offering.ServiceOffering;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.springframework.stereotype.Component;

@Component
@Local(value={HostAllocator.class})
public class TestingAllocator extends AdapterBase implements HostAllocator {
    @Inject HostDao _hostDao;
    Long _computingHost;
    Long _storageHost;
    Long _routingHost;

    @Override
    public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type,
            ExcludeList avoid, int returnUpTo) {
        return allocateTo(vmProfile, plan, type, avoid, returnUpTo, true);
    }

    @Override
    public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type,
            ExcludeList avoid, List<HostVO> hosts, int returnUpTo, boolean considerReservedCapacity) {
        return allocateTo(vmProfile, plan, type, avoid, returnUpTo, considerReservedCapacity);
    }

    @Override
    public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type,
            ExcludeList avoid, int returnUpTo, boolean considerReservedCapacity) {
        List<Host> availableHosts = new ArrayList<Host>();
        Host host = null;    	
        if (type == Host.Type.Routing && _routingHost != null) {
            host = _hostDao.findById(_routingHost);
        } else if (type == Host.Type.Storage && _storageHost != null) {
            host = _hostDao.findById(_storageHost);
        }
        if(host != null){
            availableHosts.add(host);
        }
        return availableHosts;
    }

    @Override
    public boolean isVirtualMachineUpgradable(VirtualMachine vm, ServiceOffering offering) {
        // currently we do no special checks to rule out a VM being upgradable to an offering, so
        // return true
        return true;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) {
        String value = (String)params.get(Host.Type.Routing.toString());
        _routingHost = (value != null) ? Long.parseLong(value) : null;

        value = (String)params.get(Host.Type.Storage.toString());
        _storageHost = (value != null) ? Long.parseLong(value) : null;

        return true;
    }
}
