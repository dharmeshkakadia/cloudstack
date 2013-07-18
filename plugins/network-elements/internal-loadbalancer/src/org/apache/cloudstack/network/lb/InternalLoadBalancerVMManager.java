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
package org.apache.cloudstack.network.lb;

import java.util.List;
import java.util.Map;

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.router.VirtualRouter;
import org.apache.user.Account;
import org.apache.utils.component.Manager;
import org.apache.utils.net.Ip;
import org.apache.vm.VirtualMachineProfile.Param;

public interface InternalLoadBalancerVMManager extends Manager, InternalLoadBalancerVMService{
    //RAM/CPU for the system offering used by Internal LB VMs
    public static final int DEFAULT_INTERNALLB_VM_RAMSIZE = 128;            // 128 MB
    public static final int DEFAULT_INTERNALLB_VM_CPU_MHZ = 256;            // 256 MHz
    
    /**
     * Destroys Internal LB vm instance
     * @param vmId
     * @param caller
     * @param callerUserId
     * @return 
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    boolean destroyInternalLbVm(long vmId, Account caller, Long callerUserId) 
            throws ResourceUnavailableException, ConcurrentOperationException;


    /**
     * Deploys internal lb vm
     * @param guestNetwork
     * @param requestedGuestIp
     * @param dest
     * @param owner
     * @param params
     * @return
     * @throws InsufficientCapacityException
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    List<? extends VirtualRouter> deployInternalLbVm(Network guestNetwork, Ip requestedGuestIp, DeployDestination dest, Account owner,
            Map<Param, Object> params) throws InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException;



    /**
     * 
     * @param network
     * @param rules
     * @param internalLbVms
     * @return
     * @throws ResourceUnavailableException
     */
    boolean applyLoadBalancingRules(Network network, List<LoadBalancingRule> rules, List<? extends VirtualRouter> internalLbVms)
            throws ResourceUnavailableException;


    /**
     * Returns existing Internal Load Balancer elements based on guestNetworkId (required) and requestedIp (optional)
     * @param guestNetworkId
     * @param requestedGuestIp
     * @return
     */
    List<? extends VirtualRouter> findInternalLbVms(long guestNetworkId, Ip requestedGuestIp);

}
