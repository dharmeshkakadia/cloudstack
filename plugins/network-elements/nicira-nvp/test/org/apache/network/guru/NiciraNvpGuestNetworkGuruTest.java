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
package org.apache.network.guru;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Command;
import org.apache.agent.api.CreateLogicalSwitchAnswer;
import org.apache.agent.api.DeleteLogicalSwitchAnswer;
import org.apache.dc.DataCenter;
import org.apache.dc.DataCenterVO;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.dc.dao.DataCenterDao;
import org.apache.deploy.DeployDestination;
import org.apache.deploy.DeploymentPlan;
import org.apache.domain.Domain;
import org.apache.exception.InsufficientVirtualNetworkCapcityException;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.network.Network;
import org.apache.network.NetworkManager;
import org.apache.network.NetworkModel;
import org.apache.network.NetworkProfile;
import org.apache.network.NiciraNvpDeviceVO;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Service;
import org.apache.network.Network.State;
import org.apache.network.Networks.BroadcastDomainType;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.NetworkDao;
import org.apache.network.dao.NetworkVO;
import org.apache.network.dao.NiciraNvpDao;
import org.apache.network.dao.PhysicalNetworkDao;
import org.apache.network.dao.PhysicalNetworkVO;
import org.apache.network.guru.GuestNetworkGuru;
import org.apache.network.guru.NiciraNvpGuestNetworkGuru;
import org.apache.offering.NetworkOffering;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.user.Account;
import org.apache.vm.ReservationContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import edu.emory.mathcs.backport.java.util.Arrays;

public class NiciraNvpGuestNetworkGuruTest {
	PhysicalNetworkDao physnetdao = mock (PhysicalNetworkDao.class);
	NiciraNvpDao nvpdao = mock(NiciraNvpDao.class);
	DataCenterDao dcdao = mock(DataCenterDao.class);
	NetworkOfferingServiceMapDao nosd = mock(NetworkOfferingServiceMapDao.class);
	AgentManager agentmgr = mock (AgentManager.class);
	NetworkManager netmgr = mock (NetworkManager.class);
	NetworkModel netmodel = mock (NetworkModel.class);

	HostDao hostdao = mock (HostDao.class);
	NetworkDao netdao = mock(NetworkDao.class);
	NiciraNvpGuestNetworkGuru guru;
	
	
	@Before
	public void setUp() {
		guru = new NiciraNvpGuestNetworkGuru();
		((GuestNetworkGuru) guru)._physicalNetworkDao = physnetdao;
		guru._physicalNetworkDao = physnetdao;
		guru._niciraNvpDao = nvpdao;
		guru._dcDao = dcdao;
		guru._ntwkOfferingSrvcDao = nosd;
		guru._networkModel = netmodel;
		guru._hostDao = hostdao;
		guru._agentMgr = agentmgr;
		guru._networkDao = netdao;
		
		DataCenterVO dc = mock(DataCenterVO.class);
		when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
		when(dc.getGuestNetworkCidr()).thenReturn("10.1.1.1/24");
		
		when(dcdao.findById((Long) any())).thenReturn((DataCenterVO) dc);
	}
	
	@Test
	public void testCanHandle() {
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
	
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(true);
		
		assertTrue(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);
		
		// Not supported TrafficType != Guest
		when(offering.getTrafficType()).thenReturn(TrafficType.Management);
		assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);
		
		// Not supported: GuestType Shared
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Shared);
		assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);
		
		// Not supported: Basic networking
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		assertFalse(guru.canHandle(offering, NetworkType.Basic, physnet) == true);
		
		// Not supported: IsolationMethod != STT
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "VLAN" }));
		assertFalse(guru.canHandle(offering, NetworkType.Advanced, physnet) == true);
		
	}
	
	
	@Test
	public void testDesign() {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(true);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		Network network = mock(Network.class);
		Account account = mock(Account.class);
		
		Network designednetwork = guru.design(offering, plan, network, account);
		assertTrue(designednetwork != null);
		assertTrue(designednetwork.getBroadcastDomainType() == BroadcastDomainType.Lswitch);		
	}
	
	@Test
	public void testDesignNoElementOnPhysicalNetwork() {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Collections.<NiciraNvpDeviceVO> emptyList());
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		Network network = mock(Network.class);
		Account account = mock(Account.class);
		
		Network designednetwork = guru.design(offering, plan, network, account);
		assertTrue(designednetwork == null);	
	}
	
	@Test
	public void testDesignNoIsolationMethodSTT() {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "VLAN" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Collections.<NiciraNvpDeviceVO> emptyList());
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		Network network = mock(Network.class);
		Account account = mock(Account.class);
		
		Network designednetwork = guru.design(offering, plan, network, account);
		assertTrue(designednetwork == null);			
	}
	
	@Test
	public void testDesignNoConnectivityInOffering() {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(false);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		Network network = mock(Network.class);
		Account account = mock(Account.class);
		
		Network designednetwork = guru.design(offering, plan, network, account);
		assertTrue(designednetwork == null);		
	}
	
	@Test
	public void testImplement() throws InsufficientVirtualNetworkCapcityException {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(false);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		
		NetworkVO network = mock(NetworkVO.class);
		when(network.getName()).thenReturn("testnetwork");
		when(network.getState()).thenReturn(State.Implementing);
		when(network.getPhysicalNetworkId()).thenReturn(42L);
		
		DeployDestination dest = mock(DeployDestination.class);
		
		DataCenter dc = mock(DataCenter.class);
		when(dest.getDataCenter()).thenReturn(dc);
		
		HostVO niciraHost = mock(HostVO.class);
		when(hostdao.findById(anyLong())).thenReturn(niciraHost);
		when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
		when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
		when(niciraHost.getId()).thenReturn(42L);
		
		when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(42L);
		Domain dom = mock(Domain.class);
		when(dom.getName()).thenReturn("domain");
		Account acc = mock(Account.class);
		when(acc.getAccountName()).thenReturn("accountname");
		ReservationContext res = mock(ReservationContext.class);
		when(res.getDomain()).thenReturn(dom);
		when(res.getAccount()).thenReturn(acc);
		
		CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
		when(answer.getResult()).thenReturn(true);
		when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
 		when(agentmgr.easySend(eq(42L), (Command)any())).thenReturn(answer);		

 		Network implementednetwork = guru.implement(network, offering, dest, res);
		assertTrue(implementednetwork != null);		
		verify(agentmgr, times(1)).easySend(eq(42L), (Command)any());
	}

	@Test
	public void testImplementWithCidr() throws InsufficientVirtualNetworkCapcityException {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(false);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		
		NetworkVO network = mock(NetworkVO.class);
		when(network.getName()).thenReturn("testnetwork");
		when(network.getState()).thenReturn(State.Implementing);
		when(network.getGateway()).thenReturn("10.1.1.1");
		when(network.getCidr()).thenReturn("10.1.1.0/24");
		when(network.getPhysicalNetworkId()).thenReturn(42L);
		
		DeployDestination dest = mock(DeployDestination.class);
		
		DataCenter dc = mock(DataCenter.class);
		when(dest.getDataCenter()).thenReturn(dc);
		
		HostVO niciraHost = mock(HostVO.class);
		when(hostdao.findById(anyLong())).thenReturn(niciraHost);
		when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
		when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
		when(niciraHost.getId()).thenReturn(42L);
		
		when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(42L);
		Domain dom = mock(Domain.class);
		when(dom.getName()).thenReturn("domain");
		Account acc = mock(Account.class);
		when(acc.getAccountName()).thenReturn("accountname");
		ReservationContext res = mock(ReservationContext.class);
		when(res.getDomain()).thenReturn(dom);
		when(res.getAccount()).thenReturn(acc);
		
		CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
		when(answer.getResult()).thenReturn(true);
		when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
 		when(agentmgr.easySend(eq(42L), (Command)any())).thenReturn(answer);		

 		Network implementednetwork = guru.implement(network, offering, dest, res);
		assertTrue(implementednetwork != null);		
		assertTrue(implementednetwork.getCidr().equals("10.1.1.0/24"));
		assertTrue(implementednetwork.getGateway().equals("10.1.1.1"));
		verify(agentmgr, times(1)).easySend(eq(42L), (Command)any());
	}
	
	@Test
	public void testImplementURIException() throws InsufficientVirtualNetworkCapcityException {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(false);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		
		NetworkVO network = mock(NetworkVO.class);
		when(network.getName()).thenReturn("testnetwork");
		when(network.getState()).thenReturn(State.Implementing);
		when(network.getPhysicalNetworkId()).thenReturn(42L);
		
		DeployDestination dest = mock(DeployDestination.class);
		
		DataCenter dc = mock(DataCenter.class);
		when(dest.getDataCenter()).thenReturn(dc);
		
		HostVO niciraHost = mock(HostVO.class);
		when(hostdao.findById(anyLong())).thenReturn(niciraHost);
		when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
		when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
		when(niciraHost.getId()).thenReturn(42L);
		
		when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(42L);
		Domain dom = mock(Domain.class);
		when(dom.getName()).thenReturn("domain");
		Account acc = mock(Account.class);
		when(acc.getAccountName()).thenReturn("accountname");
		ReservationContext res = mock(ReservationContext.class);
		when(res.getDomain()).thenReturn(dom);
		when(res.getAccount()).thenReturn(acc);
		
		CreateLogicalSwitchAnswer answer = mock(CreateLogicalSwitchAnswer.class);
		when(answer.getResult()).thenReturn(true);
		//when(answer.getLogicalSwitchUuid()).thenReturn("aaaaa");
 		when(agentmgr.easySend(eq(42L), (Command)any())).thenReturn(answer);		

 		Network implementednetwork = guru.implement(network, offering, dest, res);
		assertTrue(implementednetwork == null);		
		verify(agentmgr, times(1)).easySend(eq(42L), (Command)any());
	}
	
	@Test
	public void testShutdown() throws InsufficientVirtualNetworkCapcityException, URISyntaxException {
		PhysicalNetworkVO physnet = mock(PhysicalNetworkVO.class);
		when(physnetdao.findById((Long) any())).thenReturn(physnet);
		when(physnet.getIsolationMethods()).thenReturn(Arrays.asList(new String[] { "STT" }));
		when(physnet.getId()).thenReturn(42L);
		
		NiciraNvpDeviceVO device = mock(NiciraNvpDeviceVO.class);
		when(nvpdao.listByPhysicalNetwork(42L)).thenReturn(Arrays.asList(new NiciraNvpDeviceVO[] { device }));
		when(device.getId()).thenReturn(1L);
		
		NetworkOffering offering = mock(NetworkOffering.class);
		when(offering.getId()).thenReturn(42L);
		when(offering.getTrafficType()).thenReturn(TrafficType.Guest);
		when(offering.getGuestType()).thenReturn(GuestType.Isolated);
		
		when(nosd.areServicesSupportedByNetworkOffering(42L, Service.Connectivity)).thenReturn(false);
		
		DeploymentPlan plan = mock(DeploymentPlan.class);
		
		NetworkVO network = mock(NetworkVO.class);
		when(network.getName()).thenReturn("testnetwork");
		when(network.getState()).thenReturn(State.Implementing);
		when(network.getBroadcastDomainType()).thenReturn(BroadcastDomainType.Lswitch);
		when(network.getBroadcastUri()).thenReturn(new URI("lswitch:aaaaa"));
		when(network.getPhysicalNetworkId()).thenReturn(42L);
		when(netdao.findById(42L)).thenReturn(network);
		
		DeployDestination dest = mock(DeployDestination.class);
		
		DataCenter dc = mock(DataCenter.class);
		when(dest.getDataCenter()).thenReturn(dc);
		
		HostVO niciraHost = mock(HostVO.class);
		when(hostdao.findById(anyLong())).thenReturn(niciraHost);
		when(niciraHost.getDetail("transportzoneuuid")).thenReturn("aaaa");
		when(niciraHost.getDetail("transportzoneisotype")).thenReturn("stt");
		when(niciraHost.getId()).thenReturn(42L);
		
		when(netmodel.findPhysicalNetworkId(anyLong(), (String) any(), (TrafficType) any())).thenReturn(42L);
		Domain dom = mock(Domain.class);
		when(dom.getName()).thenReturn("domain");
		Account acc = mock(Account.class);
		when(acc.getAccountName()).thenReturn("accountname");
		ReservationContext res = mock(ReservationContext.class);
		when(res.getDomain()).thenReturn(dom);
		when(res.getAccount()).thenReturn(acc);
		
		DeleteLogicalSwitchAnswer answer = mock(DeleteLogicalSwitchAnswer.class);
		when(answer.getResult()).thenReturn(true);
 		when(agentmgr.easySend(eq(42L), (Command)any())).thenReturn(answer);		

 		NetworkProfile implementednetwork = mock(NetworkProfile.class);
 		when(implementednetwork.getId()).thenReturn(42L);
 		when(implementednetwork.getBroadcastUri()).thenReturn(new URI("lswitch:aaaa"));
 		when(offering.getSpecifyVlan()).thenReturn(false);
 		
 		guru.shutdown(implementednetwork, offering);
		verify(agentmgr, times(1)).easySend(eq(42L), (Command)any());
		verify(implementednetwork, times(1)).setBroadcastUri(null);
	}
}
