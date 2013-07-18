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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Command;
import org.apache.agent.api.ConfigurePublicIpsOnLogicalRouterAnswer;
import org.apache.agent.api.ConfigurePublicIpsOnLogicalRouterCommand;
import org.apache.deploy.DeployDestination;
import org.apache.domain.Domain;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.NiciraNvpDeviceVO;
import org.apache.network.NiciraNvpRouterMappingVO;
import org.apache.network.PublicIpAddress;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.BroadcastDomainType;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.NetworkServiceMapDao;
import org.apache.network.dao.NiciraNvpDao;
import org.apache.network.dao.NiciraNvpRouterMappingDao;
import org.apache.network.element.NiciraNvpElement;
import org.apache.network.nicira.NatRule;
import org.apache.offering.NetworkOffering;
import org.apache.resource.ResourceManager;
import org.apache.user.Account;
import org.apache.utils.net.Ip;
import org.apache.vm.ReservationContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class NiciraNvpElementTest {

    NiciraNvpElement _element = new NiciraNvpElement();
    NetworkManager _networkManager = mock(NetworkManager.class);
    NetworkModel _networkModel = mock(NetworkModel.class);
    NetworkServiceMapDao _ntwkSrvcDao = mock(NetworkServiceMapDao.class);
    AgentManager _agentManager = mock(AgentManager.class);
    HostDao _hostDao = mock(HostDao.class);
    NiciraNvpDao _niciraNvpDao = mock(NiciraNvpDao.class);
    NiciraNvpRouterMappingDao _niciraNvpRouterMappingDao = mock(NiciraNvpRouterMappingDao.class);

    @Before
    public void setUp() throws ConfigurationException {
        _element._resourceMgr = mock(ResourceManager.class);
        _element._networkManager = _networkManager;
        _element._ntwkSrvcDao = _ntwkSrvcDao;
        _element._networkModel = _networkModel;
        _element._agentMgr = _agentManager;
        _element._hostDao = _hostDao;
        _element._niciraNvpDao = _niciraNvpDao;
        _element._niciraNvpRouterMappingDao = _niciraNvpRouterMappingDao;

        // Standard responses
        when(_networkModel.isProviderForNetwork(Provider.NiciraNvp, 42L)).thenReturn(true);

        _element.configure("NiciraNvpTestElement", Collections.<String, Object> emptyMap());
    }

    @Test
    public void canHandleTest() {
        Network net = mock(Network.class);
        when(net.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
        when(net.getId()).thenReturn(42L);

        when(_ntwkSrvcDao.canProviderSupportServiceInNetwork(42L, Service.Connectivity, Provider.NiciraNvp)).thenReturn(true);
        // Golden path
        assertTrue(_element.canHandle(net, Service.Connectivity));

        when(net.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Vlan);
        // Only broadcastdomaintype lswitch is supported
        assertFalse(_element.canHandle(net, Service.Connectivity));

        when(net.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
        when(_ntwkSrvcDao.canProviderSupportServiceInNetwork(42L, Service.Connectivity, Provider.NiciraNvp)).thenReturn(false);
        // No nvp provider in the network
        assertFalse(_element.canHandle(net, Service.Connectivity));

        when(_networkModel.isProviderForNetwork(Provider.NiciraNvp, 42L)).thenReturn(false);
        when(_ntwkSrvcDao.canProviderSupportServiceInNetwork(42L, Service.Connectivity, Provider.NiciraNvp)).thenReturn(true);
        // NVP provider does not provide Connectivity for this network
        assertFalse(_element.canHandle(net, Service.Connectivity));

        when(_networkModel.isProviderForNetwork(Provider.NiciraNvp, 42L)).thenReturn(true);
        // Only service Connectivity is supported
        assertFalse(_element.canHandle(net, Service.Dhcp));

    }

    @Test
    public void implementTest() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        Network network = mock(Network.class);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
        when(network.getId()).thenReturn(42L);

        NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(42L);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        DeployDestination dest = mock(DeployDestination.class);

        Domain dom = mock(Domain.class);
        when(dom.getName()).thenReturn("domain");
        Account acc = mock(Account.class);
        when(acc.getAccountName()).thenReturn("accountname");
        ReservationContext context = mock(ReservationContext.class);
        when(context.getDomain()).thenReturn(dom);
        when(context.getAccount()).thenReturn(acc);

        // assertTrue(_element.implement(network, offering, dest, context));
    }

    @Test
    public void applyIpTest() throws ResourceUnavailableException {
        Network network = mock(Network.class);
        when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
        when(network.getId()).thenReturn(42L);
        when(network.getPhysicalNetworkId()).thenReturn(42L);

        NetworkOffering offering = mock(NetworkOffering.class);
        when(offering.getId()).thenReturn(42L);
        when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
        when(offering.getGuestType()).thenReturn(GuestType.Isolated);

        List<PublicIpAddress> ipAddresses = new ArrayList<PublicIpAddress>();
        PublicIpAddress pipReleased = mock(PublicIpAddress.class);
        PublicIpAddress pipAllocated = mock(PublicIpAddress.class);
        Ip ipReleased = new Ip("42.10.10.10");
        Ip ipAllocated = new Ip("10.10.10.10");
        when(pipAllocated.getState()).thenReturn(IpAddress.State.Allocated);
        when(pipAllocated.getAddress()).thenReturn(ipAllocated);
        when(pipAllocated.getNetmask()).thenReturn("255.255.255.0");
        when(pipReleased.getState()).thenReturn(IpAddress.State.Releasing);
        when(pipReleased.getAddress()).thenReturn(ipReleased);
        when(pipReleased.getNetmask()).thenReturn("255.255.255.0");
        ipAddresses.add(pipAllocated);
        ipAddresses.add(pipReleased);

        Set<Service> services = new HashSet<Service>();
        services.add(Service.SourceNat);
        services.add(Service.StaticNat);
        services.add(Service.PortForwarding);

        List<NiciraNvpDeviceVO> deviceList = new ArrayList<NiciraNvpDeviceVO>();
        NiciraNvpDeviceVO nndVO = mock(NiciraNvpDeviceVO.class);
        NiciraNvpRouterMappingVO nnrmVO = mock(NiciraNvpRouterMappingVO.class);
        when(_niciraNvpRouterMappingDao.findByNetworkId(42L)).thenReturn(nnrmVO);
        when(nnrmVO.getLogicalRouterUuid()).thenReturn("abcde");
        when(nndVO.getHostId()).thenReturn(42L);
        HostVO hvo = mock(HostVO.class);
        when(hvo.getId()).thenReturn(42L);
        when(hvo.getDetail("l3gatewayserviceuuid")).thenReturn("abcde");
        when(_hostDao.findById(42L)).thenReturn(hvo);
        deviceList.add(nndVO);
        when(_niciraNvpDao.listByPhysicalNetwork(42L)).thenReturn(deviceList);

        ConfigurePublicIpsOnLogicalRouterAnswer answer = mock(ConfigurePublicIpsOnLogicalRouterAnswer.class);
        when(answer.getResult()).thenReturn(true);
        when(_agentManager.easySend(eq(42L), any(ConfigurePublicIpsOnLogicalRouterCommand.class))).thenReturn(answer);

        assertTrue(_element.applyIps(network, ipAddresses, services));

        verify(_agentManager, atLeast(1)).easySend(eq(42L),
                argThat(new ArgumentMatcher<ConfigurePublicIpsOnLogicalRouterCommand>() {
                    @Override
                    public boolean matches(Object argument) {
                        ConfigurePublicIpsOnLogicalRouterCommand command = (ConfigurePublicIpsOnLogicalRouterCommand) argument;
                        if (command.getPublicCidrs().size() == 1)
                            return true;
                        return false;
                    }
                }));
    }
}