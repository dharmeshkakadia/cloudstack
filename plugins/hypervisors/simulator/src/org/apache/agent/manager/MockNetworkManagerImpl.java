/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.agent.manager;

import org.apache.agent.api.Answer;
import org.apache.agent.api.CheckS2SVpnConnectionsCommand;
import org.apache.agent.api.NetworkUsageAnswer;
import org.apache.agent.api.NetworkUsageCommand;
import org.apache.agent.api.PlugNicAnswer;
import org.apache.agent.api.PlugNicCommand;
import org.apache.agent.api.PvlanSetupCommand;
import org.apache.agent.api.SetupGuestNetworkAnswer;
import org.apache.agent.api.SetupGuestNetworkCommand;
import org.apache.agent.api.UnPlugNicAnswer;
import org.apache.agent.api.UnPlugNicCommand;
import org.apache.agent.api.routing.DhcpEntryCommand;
import org.apache.agent.api.routing.IpAssocAnswer;
import org.apache.agent.api.routing.IpAssocCommand;
import org.apache.agent.api.routing.IpAssocVpcCommand;
import org.apache.agent.api.routing.LoadBalancerConfigCommand;
import org.apache.agent.api.routing.NetworkElementCommand;
import org.apache.agent.api.routing.SetFirewallRulesAnswer;
import org.apache.agent.api.routing.SetFirewallRulesCommand;
import org.apache.agent.api.routing.SetNetworkACLAnswer;
import org.apache.agent.api.routing.SetNetworkACLCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesAnswer;
import org.apache.agent.api.routing.SetPortForwardingRulesCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesVpcCommand;
import org.apache.agent.api.routing.SetSourceNatAnswer;
import org.apache.agent.api.routing.SetSourceNatCommand;
import org.apache.agent.api.routing.SetStaticNatRulesAnswer;
import org.apache.agent.api.routing.SetStaticNatRulesCommand;
import org.apache.agent.api.routing.SetStaticRouteAnswer;
import org.apache.agent.api.routing.SetStaticRouteCommand;
import org.apache.agent.api.routing.Site2SiteVpnCfgCommand;
import org.apache.agent.api.to.IpAddressTO;
import org.apache.agent.api.to.PortForwardingRuleTO;
import org.apache.log4j.Logger;
import org.apache.simulator.MockVMVO;
import org.apache.simulator.dao.MockVMDao;
import org.apache.utils.component.ManagerBase;

import javax.inject.Inject;

public class MockNetworkManagerImpl extends ManagerBase implements MockNetworkManager {
    private static final Logger s_logger = Logger.getLogger(MockVmManagerImpl.class);

    @Inject
    MockVMDao _mockVmDao;

    @Override
    public Answer SetStaticNatRules(SetStaticNatRulesCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer SetPortForwardingRules(SetPortForwardingRulesCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public SetFirewallRulesAnswer SetFirewallRules(SetFirewallRulesCommand cmd) {
        String[] results = new String[cmd.getRules().length];
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        if (routerIp == null) {
            return new SetFirewallRulesAnswer(cmd, false, results);
        }

        String[][] rules = cmd.generateFwRules();
        StringBuilder sb = new StringBuilder();
        String[] fwRules = rules[0];
        if (fwRules.length > 0) {
            for (int i = 0; i < fwRules.length; i++) {
                sb.append(fwRules[i]).append(',');
            }
        }
        return new SetFirewallRulesAnswer(cmd, true, results);
    }


    @Override
    public NetworkUsageAnswer getNetworkUsage(NetworkUsageCommand cmd) {
        return new NetworkUsageAnswer(cmd, null, 100L, 100L);
    }

    @Override
    public Answer IpAssoc(IpAssocCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer LoadBalancerConfig(LoadBalancerConfigCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer AddDhcpEntry(DhcpEntryCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer setupPVLAN(PvlanSetupCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public PlugNicAnswer plugNic(PlugNicCommand cmd) {
        String vmname = cmd.getVmName();
        if (_mockVmDao.findByVmName(vmname) != null) {
            s_logger.debug("Plugged NIC (dev=" + cmd.getNic().getDeviceId() + ", " + cmd.getNic().getIp() + ") into " + cmd.getVmName());
            return new PlugNicAnswer(cmd,  true, "success");
        }
        s_logger.error("Plug NIC failed for (dev=" + cmd.getNic().getDeviceId() + ", " + cmd.getNic().getIp() + ") into " + cmd.getVmName());
        return new PlugNicAnswer(cmd, false, "failure");
    }

    @Override
    public UnPlugNicAnswer unplugNic(UnPlugNicCommand cmd) {
        String vmname = cmd.getVmName();
        if (_mockVmDao.findByVmName(vmname) != null) {
            s_logger.debug("Plugged NIC (dev=" + cmd.getNic().getDeviceId() + ", " + cmd.getNic().getIp() + ") into " + cmd.getVmName());
            return new UnPlugNicAnswer(cmd,  true, "success");
        }
        s_logger.error("Plug NIC failed for (dev=" + cmd.getNic().getDeviceId() + ", " + cmd.getNic().getIp() + ") into " + cmd.getVmName());
        return new UnPlugNicAnswer(cmd, false, "failure");
    }

    @Override
    public IpAssocAnswer ipAssoc(IpAssocVpcCommand cmd) {
        String[] results = new String[cmd.getIpAddresses().length];
        int i = 0;
        IpAddressTO[] ips = cmd.getIpAddresses();
        for (IpAddressTO ip : ips) {
            results[i++] = ip.getPublicIp() + " - success";
        }
        return new IpAssocAnswer(cmd, results);
    }

    @Override
    public SetSourceNatAnswer setSourceNat(SetSourceNatCommand cmd) {
        return new SetSourceNatAnswer(cmd, true, "success");
    }

    @Override
    public SetNetworkACLAnswer setNetworkAcl(SetNetworkACLCommand cmd) {
        String[] results = new String[cmd.getRules().length];
        String routerName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        String routerIp = cmd.getAccessDetail(NetworkElementCommand.ROUTER_IP);

        StringBuilder sb = new StringBuilder();
        sb.append(routerIp);
        sb.append(routerName);

        String [][] rules = cmd.generateFwRules();
        String[] aclRules = rules[0];

        for (int i = 0; i < aclRules.length; i++) {
            sb.append(aclRules[i]).append(',');
        }
        return new SetNetworkACLAnswer(cmd, true, results);
    }

    @Override
    public SetPortForwardingRulesAnswer setVpcPortForwards(SetPortForwardingRulesVpcCommand cmd) {
        String[] results = new String[cmd.getRules().length];
        StringBuilder sb = new StringBuilder();
        for (PortForwardingRuleTO rule : cmd.getRules()) {
            sb.append("src:");
            sb.append(rule.getStringSrcPortRange());
            sb.append("dst:");
            sb.append(rule.getStringDstPortRange());
        }
        return new SetPortForwardingRulesAnswer(cmd, results, true);
    }

    @Override
    public SetStaticRouteAnswer setStaticRoute(SetStaticRouteCommand cmd) {
        String[] results = new String[cmd.getStaticRoutes().length];
        String [][] rules = cmd.generateSRouteRules();
        StringBuilder sb = new StringBuilder();
        String[] srRules = rules[0];
        for (int i = 0; i < srRules.length; i++) {
            sb.append(srRules[i]).append(',');
        }
        return new SetStaticRouteAnswer(cmd, true, results);
    }

    @Override
    public SetupGuestNetworkAnswer setUpGuestNetwork(SetupGuestNetworkCommand cmd) {
        String domrName = cmd.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        try {
            MockVMVO vms = _mockVmDao.findByVmName(domrName);
            if (vms == null) {
                return new SetupGuestNetworkAnswer(cmd, false, "Can not find VM " + domrName);
            }
            return new SetupGuestNetworkAnswer(cmd, true, "success");
        } catch (Exception e) {
            String msg = "Creating guest network failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new SetupGuestNetworkAnswer(cmd, false, msg);
        }
    }

    @Override
    public SetStaticNatRulesAnswer setVPCStaticNatRules(SetStaticNatRulesCommand cmd) {
        String[] results = new String[cmd.getRules().length];
        return new SetStaticNatRulesAnswer(cmd, results, true);
    }

    @Override
    public Answer siteToSiteVpn(Site2SiteVpnCfgCommand cmd) {
        return new Answer(cmd);
    }

    @Override
    public Answer checkSiteToSiteVpnConnection(CheckS2SVpnConnectionsCommand cmd) {
        return new Answer(cmd);
    }
}
