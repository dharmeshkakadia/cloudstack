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
package org.apache.agent.manager.allocator;

import java.util.List;

import org.apache.deploy.DeploymentPlan;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.Host.Type;
import org.apache.offering.ServiceOffering;
import org.apache.utils.component.Adapter;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

public interface HostAllocator extends Adapter {

	/**
	 * @param UserVm vm
	 * @param ServiceOffering offering
	 **/
	boolean isVirtualMachineUpgradable(final VirtualMachine vm, final ServiceOffering offering);

	/** 
	* Determines which physical hosts are suitable to 
	* allocate the guest virtual machines on 
	* 
	* @param VirtualMachineProfile vmProfile
	* @param DeploymentPlan plan
	* @param GuestType type
	* @param ExcludeList avoid
	* @param int returnUpTo (use -1 to return all possible hosts)
	* @return List<Host> List of hosts that are suitable for VM allocation
	**/ 
	
	public List<Host> allocateTo(VirtualMachineProfile<?extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, int returnUpTo);
	
    /** 
    * Determines which physical hosts are suitable to 
    * allocate the guest virtual machines on 
    * 
    * @param VirtualMachineProfile vmProfile
    * @param DeploymentPlan plan
    * @param GuestType type
    * @param ExcludeList avoid
    * @param int returnUpTo (use -1 to return all possible hosts)
    * @param boolean considerReservedCapacity (default should be true, set to false if host capacity calculation should not look at reserved capacity)
    * @return List<Host> List of hosts that are suitable for VM allocation
    **/ 
    
    public List<Host> allocateTo(VirtualMachineProfile<?extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, int returnUpTo, boolean considerReservedCapacity);

    /**
     * Determines which physical hosts are suitable to
     * allocate the guest virtual machines on
     *
     * @param VirtualMachineProfile vmProfile
     * @param DeploymentPlan plan
     * @param GuestType type
     * @param ExcludeList avoid
     * @param List<HostVO> hosts
     * @param int returnUpTo (use -1 to return all possible hosts)
     * @param boolean considerReservedCapacity (default should be true, set to false if host capacity calculation should not look at reserved capacity)
     * @return List<Host> List of hosts that are suitable for VM allocation
     **/
     public List<Host> allocateTo(VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, Type type, ExcludeList avoid, List<HostVO> hosts, int returnUpTo, boolean considerReservedCapacity);

     public static int RETURN_UPTO_ALL = -1;

}
