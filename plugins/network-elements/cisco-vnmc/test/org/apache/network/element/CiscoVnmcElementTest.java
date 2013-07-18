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
package org.apache.network.element;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.AssociateAsaWithLogicalEdgeFirewallCommand;
import org.apache.agent.api.CleanupLogicalEdgeFirewallCommand;
import org.apache.agent.api.ConfigureNexusVsmForAsaCommand;
import org.apache.agent.api.CreateLogicalEdgeFirewallCommand;
import org.apache.agent.api.routing.SetFirewallRulesCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesCommand;
import org.apache.agent.api.routing.SetSourceNatCommand;
import org.apache.agent.api.routing.SetStaticNatRulesCommand;
import org.apache.configuration.ConfigurationManager;
import org.apache.dc.ClusterVSMMapVO;
import org.apache.dc.DataCenter;
import org.apache.dc.VlanVO;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.dc.dao.ClusterVSMMapDao;
import org.apache.dc.dao.VlanDao;
import org.apache.deploy.DeployDestination;
import org.apache.domain.Domain;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.network.CiscoNexusVSMDeviceVO;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.BroadcastDomainType;
import org.apache.network.Networks.TrafficType;
import org.apache.network.addr.PublicIp;
import org.apache.network.cisco.CiscoAsa1000vDeviceVO;
import org.apache.network.cisco.CiscoVnmcControllerVO;
import org.apache.network.cisco.NetworkAsa1000vMapVO;
import org.apache.network.dao.CiscoAsa1000vDao;
import org.apache.network.dao.CiscoNexusVSMDeviceDao;
import org.apache.network.dao.CiscoVnmcDao;
import org.apache.network.dao.NetworkAsa1000vMapDao;
import org.apache.network.dao.NetworkServiceMapDao;
import org.apache.network.element.CiscoVnmcElement;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.PortForwardingRule;
import org.apache.network.rules.StaticNat;
import org.apache.network.rules.StaticNatRule;
import org.apache.offering.NetworkOffering;
import org.apache.resource.ResourceManager;
import org.apache.user.Account;
import org.apache.utils.net.Ip;
import org.apache.vm.ReservationContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Any;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CiscoVnmcElementTest {

    CiscoVnmcElement _element = new CiscoVnmcElement();
    AgentManager _agentMgr = mock(AgentManager.class);
    NetworkManager _networkMgr = mock(NetworkManager.class);
    NetworkModel _networkModel = mock(NetworkModel.class);
    HostDao _hostDao = mock(HostDao.class);
    NetworkServiceMapDao _ntwkSrvcDao = mock(NetworkServiceMapDao.class);
    ConfigurationManager _configMgr = mock(ConfigurationManager.class);
    CiscoVnmcDao _ciscoVnmcDao = mock(CiscoVnmcDao.class);
    CiscoAsa1000vDao _ciscoAsa1000vDao = mock(CiscoAsa1000vDao.class);
    NetworkAsa1000vMapDao _networkAsa1000vMapDao = mock(NetworkAsa1000vMapDao.class);
    ClusterVSMMapDao _clusterVsmMapDao = mock(ClusterVSMMapDao.class);
    CiscoNexusVSMDeviceDao _vsmDeviceDao = mock(CiscoNexusVSMDeviceDao.class);
    VlanDao _vlanDao = mock(VlanDao.class);

    @Before
    public void setUp() throws ConfigurationException {
        _element._resourceMgr = mock(ResourceManager.class);
        _element._agentMgr = _agentMgr;
        _element._networkMgr = _networkMgr;
        _element._networkModel = _networkModel;
        _element._hostDao = _hostDao;
        _element._configMgr = _configMgr;
        _element._ciscoVnmcDao = _ciscoVnmcDao;
        _element._ciscoAsa1000vDao = _ciscoAsa1000vDao;
        _element._networkAsa1000vMapDao = _networkAsa1000vMapDao;
        _element._clusterVsmMapDao = _clusterVsmMapDao;
        _element._vsmDeviceDao = _vsmDeviceDao;
        _element._vlanDao = _vlanDao;

        // Standard responses
        when(_networkModel.isProviderForNetwork(Provider.CiscoVnmc, 1L)).thenReturn(true);

        _element.configure("CiscoVnmcTestElement", Collections.<String, Object> emptyMap());
    }

    @Test
    public void canHandleTest() {
        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        assertTrue(_element.canHandle(network));

        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.UnDecided);
        assertFalse(_element.canHandle(network));
    }

    @Test
    public void implementTest() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
    	URI uri = URI.create("vlan://123");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        when(network.getDataCenterId()).thenReturn(1L);
        when(network.getGateway()).thenReturn("1.1.1.1");
        when(network.getBroadcastUri()).thenReturn(uri);
        when(network.getCidr()).thenReturn("1.1.1.0/24");

        NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(1L);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        DeployDestination dest = mock(DeployDestination.class);

        Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("d1");
        Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("a1");
        ReservationContext context = mock(ReservationContext.class);
        when(context.getDomain()).thenReturn(dom);
        when(context.getAccount()).thenReturn(acc);

        DataCenter dc = mock(DataCenter.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(_configMgr.getZone(network.getDataCenterId())).thenReturn(dc);

        List<CiscoVnmcControllerVO> devices = new ArrayList<CiscoVnmcControllerVO>();
        devices.add(mock(CiscoVnmcControllerVO.class));
        when(_ciscoVnmcDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(devices);

        CiscoAsa1000vDeviceVO asaVO = mock(CiscoAsa1000vDeviceVO.class);
        when(asaVO.getInPortProfile()).thenReturn("foo");
        when(asaVO.getManagementIp()).thenReturn("1.2.3.4");

        List<CiscoAsa1000vDeviceVO> asaList = new ArrayList<CiscoAsa1000vDeviceVO>();
        asaList.add(asaVO);
        when(_ciscoAsa1000vDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(asaList);

        when(_networkAsa1000vMapDao.findByNetworkId(network.getId())).thenReturn(mock(NetworkAsa1000vMapVO.class));
        when(_networkAsa1000vMapDao.findByAsa1000vId(anyLong())).thenReturn(null);
        when(_networkAsa1000vMapDao.persist(any(NetworkAsa1000vMapVO.class))).thenReturn(mock(NetworkAsa1000vMapVO.class));

        when(_networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.SourceNat, Provider.CiscoVnmc)).thenReturn(true);

        ClusterVSMMapVO clusterVsmMap = mock(ClusterVSMMapVO.class);
        when(_clusterVsmMapDao.findByClusterId(anyLong())).thenReturn(clusterVsmMap);

        CiscoNexusVSMDeviceVO vsmDevice = mock(CiscoNexusVSMDeviceVO.class);
        when(vsmDevice.getUserName()).thenReturn("foo");
        when(vsmDevice.getPassword()).thenReturn("bar");
        when(vsmDevice.getipaddr()).thenReturn("1.2.3.4");
        when(_vsmDeviceDao.findById(anyLong())).thenReturn(vsmDevice);

        HostVO hostVO = mock(HostVO.class);
        when(hostVO.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(hostVO);

        Ip ip = mock(Ip.class);
        when(ip.addr()).thenReturn("1.2.3.4");

        PublicIp publicIp = mock(PublicIp.class);
        when(publicIp.getAddress()).thenReturn(ip);
        when(publicIp.getState()).thenReturn(IpAddress.State.Releasing);
        when(publicIp.getAccountId()).thenReturn(1L);
        when(publicIp.isSourceNat()).thenReturn(true);
        when(publicIp.getVlanTag()).thenReturn("123");
        when(publicIp.getGateway()).thenReturn("1.1.1.1");
        when(publicIp.getNetmask()).thenReturn("1.1.1.1");
        when(publicIp.getMacAddress()).thenReturn(null);
        when(publicIp.isOneToOneNat()).thenReturn(true);
        when(_networkMgr.assignSourceNatIpAddressToGuestNetwork(acc, network)).thenReturn(publicIp);

        VlanVO vlanVO = mock(VlanVO.class);
        when(vlanVO.getVlanGateway()).thenReturn("1.1.1.1");
        List<VlanVO> vlanVOList = new ArrayList<VlanVO>();
        when(_vlanDao.listVlansByPhysicalNetworkId(network.getPhysicalNetworkId())).thenReturn(vlanVOList);

        Answer answer = mock(Answer.class);
        when(answer.getResult()).thenReturn(true);

        when(_agentMgr.easySend(anyLong(), any(CreateLogicalEdgeFirewallCommand.class))).thenReturn(answer);
        when(_agentMgr.easySend(anyLong(), any(ConfigureNexusVsmForAsaCommand.class))).thenReturn(answer);
        when(_agentMgr.easySend(anyLong(), any(SetSourceNatCommand.class))).thenReturn(answer);
        when(_agentMgr.easySend(anyLong(), any(AssociateAsaWithLogicalEdgeFirewallCommand.class))).thenReturn(answer);
        
        assertTrue(_element.implement(network, offering, dest, context));
    }

    @Test
    public void shutdownTest() throws ConcurrentOperationException, ResourceUnavailableException {
    	URI uri = URI.create("vlan://123");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        when(network.getDataCenterId()).thenReturn(1L);
        when(network.getBroadcastUri()).thenReturn(uri);

        ReservationContext context = mock(ReservationContext.class);

        when(_networkAsa1000vMapDao.findByNetworkId(network.getId())).thenReturn(mock(NetworkAsa1000vMapVO.class));

        List<CiscoVnmcControllerVO> devices = new ArrayList<CiscoVnmcControllerVO>();
        devices.add(mock(CiscoVnmcControllerVO.class));
        when(_ciscoVnmcDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(devices);

        HostVO hostVO = mock(HostVO.class);
        when(hostVO.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(hostVO);

        Answer answer = mock(Answer.class);
        when(answer.getResult()).thenReturn(true);

        when(_agentMgr.easySend(anyLong(), any(CleanupLogicalEdgeFirewallCommand.class))).thenReturn(answer);

    	assertTrue(_element.shutdown(network, context, true));
    }

    @Test
    public void applyFWRulesTest() throws ResourceUnavailableException {
    	URI uri = URI.create("vlan://123");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        when(network.getDataCenterId()).thenReturn(1L);
        when(network.getBroadcastUri()).thenReturn(uri);
        when(network.getCidr()).thenReturn("1.1.1.0/24");
        when(network.getState()).thenReturn(Network.State.Implemented);

        Ip ip = mock(Ip.class);
        when(ip.addr()).thenReturn("1.2.3.4");

        IpAddress ipAddress = mock(IpAddress.class);
        when(ipAddress.getAddress()).thenReturn(ip);

        when(_networkModel.getIp(anyLong())).thenReturn(ipAddress);
        when(_networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.Firewall, Provider.CiscoVnmc)).thenReturn(true);

        List<CiscoVnmcControllerVO> devices = new ArrayList<CiscoVnmcControllerVO>();
        devices.add(mock(CiscoVnmcControllerVO.class));
        when(_ciscoVnmcDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(devices);

        when(_networkAsa1000vMapDao.findByNetworkId(network.getId())).thenReturn(mock(NetworkAsa1000vMapVO.class));

        HostVO hostVO = mock(HostVO.class);
        when(hostVO.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(hostVO);

        FirewallRule rule = mock(FirewallRule.class);
        when(rule.getSourceIpAddressId()).thenReturn(1L);
        List<FirewallRule> rules = new ArrayList<FirewallRule>();
        rules.add(rule);

        Answer answer = mock(Answer.class);
        when(answer.getResult()).thenReturn(true);

        when(_agentMgr.easySend(anyLong(), any(SetFirewallRulesCommand.class))).thenReturn(answer);

        assertTrue(_element.applyFWRules(network, rules));
    }

    @Test
    public void applyPRulesTest() throws ResourceUnavailableException {
    	URI uri = URI.create("vlan://123");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        when(network.getDataCenterId()).thenReturn(1L);
        when(network.getBroadcastUri()).thenReturn(uri);
        when(network.getCidr()).thenReturn("1.1.1.0/24");
        when(network.getState()).thenReturn(Network.State.Implemented);

        Ip ip = mock(Ip.class);
        when(ip.addr()).thenReturn("1.2.3.4");

        IpAddress ipAddress = mock(IpAddress.class);
        when(ipAddress.getAddress()).thenReturn(ip);
        when(ipAddress.getVlanId()).thenReturn(1L);

        when(_networkModel.getIp(anyLong())).thenReturn(ipAddress);
        when(_networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.PortForwarding, Provider.CiscoVnmc)).thenReturn(true);

        List<CiscoVnmcControllerVO> devices = new ArrayList<CiscoVnmcControllerVO>();
        devices.add(mock(CiscoVnmcControllerVO.class));
        when(_ciscoVnmcDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(devices);

        when(_networkAsa1000vMapDao.findByNetworkId(network.getId())).thenReturn(mock(NetworkAsa1000vMapVO.class));

        HostVO hostVO = mock(HostVO.class);
        when(hostVO.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(hostVO);

        VlanVO vlanVO = mock(VlanVO.class);
        when(vlanVO.getVlanTag()).thenReturn(null);
        when(_vlanDao.findById(anyLong())).thenReturn(vlanVO);

        PortForwardingRule rule = mock(PortForwardingRule.class);
        when(rule.getSourceIpAddressId()).thenReturn(1L);
        when(rule.getDestinationIpAddress()).thenReturn(ip);
        List<PortForwardingRule> rules = new ArrayList<PortForwardingRule>();
        rules.add(rule);

        Answer answer = mock(Answer.class);
        when(answer.getResult()).thenReturn(true);

        when(_agentMgr.easySend(anyLong(), any(SetPortForwardingRulesCommand.class))).thenReturn(answer);

        assertTrue(_element.applyPFRules(network, rules));
    }

    @Test
    public void applyStaticNatsTest() throws ResourceUnavailableException {
    	URI uri = URI.create("vlan://123");

        Network network = mock(Network.class);
        when(network.getId()).thenReturn(1L);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        when(network.getDataCenterId()).thenReturn(1L);
        when(network.getBroadcastUri()).thenReturn(uri);
        when(network.getCidr()).thenReturn("1.1.1.0/24");
        when(network.getState()).thenReturn(Network.State.Implemented);

        Ip ip = mock(Ip.class);
        when(ip.addr()).thenReturn("1.2.3.4");

        IpAddress ipAddress = mock(IpAddress.class);
        when(ipAddress.getAddress()).thenReturn(ip);
        when(ipAddress.getVlanId()).thenReturn(1L);

        when(_networkModel.getIp(anyLong())).thenReturn(ipAddress);
        when(_networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.StaticNat, Provider.CiscoVnmc)).thenReturn(true);

        List<CiscoVnmcControllerVO> devices = new ArrayList<CiscoVnmcControllerVO>();
        devices.add(mock(CiscoVnmcControllerVO.class));
        when(_ciscoVnmcDao.listByPhysicalNetwork(network.getPhysicalNetworkId())).thenReturn(devices);

        when(_networkAsa1000vMapDao.findByNetworkId(network.getId())).thenReturn(mock(NetworkAsa1000vMapVO.class));

        HostVO hostVO = mock(HostVO.class);
        when(hostVO.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(hostVO);

        VlanVO vlanVO = mock(VlanVO.class);
        when(vlanVO.getVlanTag()).thenReturn(null);
        when(_vlanDao.findById(anyLong())).thenReturn(vlanVO);

        StaticNat rule = mock(StaticNat.class);
        when(rule.getSourceIpAddressId()).thenReturn(1L);
        when(rule.getDestIpAddress()).thenReturn("1.2.3.4");
        when(rule.isForRevoke()).thenReturn(false);
        List<StaticNat> rules = new ArrayList<StaticNat>();
        rules.add(rule);

        Answer answer = mock(Answer.class);
        when(answer.getResult()).thenReturn(true);

        when(_agentMgr.easySend(anyLong(), any(SetStaticNatRulesCommand.class))).thenReturn(answer);

        assertTrue(_element.applyStaticNats(network, rules));
    }
}
