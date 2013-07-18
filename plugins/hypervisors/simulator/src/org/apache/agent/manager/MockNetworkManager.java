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
import org.apache.utils.component.Manager;

public interface MockNetworkManager extends Manager {

    Answer SetStaticNatRules(SetStaticNatRulesCommand cmd);

    Answer SetPortForwardingRules(SetPortForwardingRulesCommand cmd);

    Answer SetFirewallRules(SetFirewallRulesCommand cmd);

    Answer getNetworkUsage(NetworkUsageCommand cmd);

    Answer IpAssoc(IpAssocCommand cmd);

    Answer LoadBalancerConfig(LoadBalancerConfigCommand cmd);

    Answer AddDhcpEntry(DhcpEntryCommand cmd);

    Answer setupPVLAN(PvlanSetupCommand cmd);

    PlugNicAnswer plugNic(PlugNicCommand cmd);

    UnPlugNicAnswer unplugNic(UnPlugNicCommand cmd);

    IpAssocAnswer ipAssoc(IpAssocVpcCommand cmd);

    SetSourceNatAnswer setSourceNat(SetSourceNatCommand cmd);

    SetNetworkACLAnswer setNetworkAcl(SetNetworkACLCommand cmd);

    SetPortForwardingRulesAnswer setVpcPortForwards(SetPortForwardingRulesVpcCommand cmd);

    SetupGuestNetworkAnswer setUpGuestNetwork(SetupGuestNetworkCommand cmd);

    SetStaticNatRulesAnswer setVPCStaticNatRules(SetStaticNatRulesCommand cmd);

    SetStaticRouteAnswer setStaticRoute(SetStaticRouteCommand cmd);

    Answer siteToSiteVpn(Site2SiteVpnCfgCommand cmd);

    Answer checkSiteToSiteVpnConnection(CheckS2SVpnConnectionsCommand cmd);
}
