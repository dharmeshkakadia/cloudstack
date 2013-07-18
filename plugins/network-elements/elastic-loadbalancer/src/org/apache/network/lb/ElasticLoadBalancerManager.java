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
package org.apache.network.lb;

import java.util.List;

import org.apache.cloudstack.api.command.user.loadbalancer.CreateLoadBalancerRuleCmd;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.NetworkRuleConflictException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.rules.LoadBalancer;
import org.apache.user.Account;

public interface ElasticLoadBalancerManager {
    public static final int DEFAULT_ELB_VM_RAMSIZE = 128;            // 512 MB
    public static final int DEFAULT_ELB_VM_CPU_MHZ = 256;               // 500 MHz

    public boolean applyLoadBalancerRules(Network network, 
            List<LoadBalancingRule> rules) 
            throws ResourceUnavailableException;

    public LoadBalancer handleCreateLoadBalancerRule(CreateLoadBalancerRuleCmd lb, Account caller, long networkId) throws InsufficientAddressCapacityException, NetworkRuleConflictException;
    
    public void handleDeleteLoadBalancerRule(LoadBalancer lb, long callerUserId, Account caller);
}
