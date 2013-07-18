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
package org.apache.network;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.cloudstack.api.command.user.firewall.ListPortForwardingRulesCmd;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.NetworkRuleConflictException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.IpAddress;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.FirewallRuleVO;
import org.apache.network.rules.PortForwardingRule;
import org.apache.network.rules.PortForwardingRuleVO;
import org.apache.network.rules.RulesManager;
import org.apache.network.rules.RulesService;
import org.apache.network.rules.StaticNatRule;
import org.apache.network.rules.FirewallRule.Purpose;
import org.apache.user.Account;
import org.apache.uservm.UserVm;
import org.apache.utils.Pair;
import org.apache.utils.component.Manager;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.net.Ip;
import org.apache.vm.Nic;
import org.apache.vm.VirtualMachine;

@Local(value = {RulesManager.class, RulesService.class})
public class MockRulesManagerImpl extends ManagerBase implements RulesManager, RulesService {

	@Override
	public Pair<List<? extends FirewallRule>, Integer> searchStaticNatRules(
			Long ipId, Long id, Long vmId, Long start, Long size,
			String accountName, Long domainId, Long projectId,
			boolean isRecursive, boolean listAll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean revokePortForwardingRule(long ruleId, boolean apply) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Pair<List<? extends PortForwardingRule>, Integer> listPortForwardingRules(
			ListPortForwardingRulesCmd cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean applyPortForwardingRules(long ipAdddressId, Account caller)
			throws ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean enableStaticNat(long ipAddressId, long vmId, long networkId,
			String ipAddr) throws NetworkRuleConflictException, ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PortForwardingRule getPortForwardigRule(long ruleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FirewallRule getFirewallRule(long ruleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StaticNatRule createStaticNatRule(StaticNatRule rule,
			boolean openFirewall) throws NetworkRuleConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean revokeStaticNatRule(long ruleId, boolean apply) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean applyStaticNatRules(long ipAdddressId, Account caller)
			throws ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StaticNatRule buildStaticNatRule(FirewallRule rule, boolean forRevoke) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSourceCidrs(long ruleId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean disableStaticNat(long ipId)
			throws ResourceUnavailableException, NetworkRuleConflictException,
			InsufficientAddressCapacityException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean applyPortForwardingRulesForNetwork(long networkId,
			boolean continueOnError, Account caller) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean applyStaticNatRulesForNetwork(long networkId,
			boolean continueOnError, Account caller) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkRuleAndUserVm(FirewallRule rule, UserVm userVm,
			Account caller) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean revokeAllPFAndStaticNatRulesForIp(long ipId, long userId,
			Account caller) throws ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean revokeAllPFStaticNatRulesForNetwork(long networkId,
			long userId, Account caller) throws ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean revokePortForwardingRulesForVm(long vmId) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public FirewallRule[] reservePorts(IpAddress ip, String protocol,
			Purpose purpose, boolean openFirewall, Account caller, int... ports)
			throws NetworkRuleConflictException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean applyStaticNatsForNetwork(long networkId,
			boolean continueOnError, Account caller) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getSystemIpAndEnableStaticNatForVm(VirtualMachine vm,
			boolean getNewIp) throws InsufficientAddressCapacityException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean disableStaticNat(long ipAddressId, Account caller,
			long callerUserId, boolean releaseIpIfElastic)
			throws ResourceUnavailableException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean applyStaticNatForNetwork(long networkId,
			boolean continueOnError, Account caller, boolean forRevoke) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		
		return true;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MockRulesManagerImpl";
	}

    @Override
    public PortForwardingRule createPortForwardingRule(PortForwardingRule rule,
            Long vmId, Ip vmIp, boolean openFirewall)
            throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FirewallRuleVO> listAssociatedRulesForGuestNic(Nic nic) {
        // TODO Auto-generated method stub
        return null;
    }

}
