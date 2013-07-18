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
package org.apache.network.router;

import java.util.List;
import java.util.Map;

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.PublicIpAddress;
import org.apache.network.RemoteAccessVpn;
import org.apache.network.VirtualNetworkApplianceService;
import org.apache.network.VpnUser;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.router.VirtualRouter;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.StaticNat;
import org.apache.user.Account;
import org.apache.user.User;
import org.apache.uservm.UserVm;
import org.apache.utils.component.Manager;
import org.apache.vm.DomainRouterVO;
import org.apache.vm.NicProfile;
import org.apache.vm.VirtualMachineProfile;

/**
 * NetworkManager manages the network for the different end users.
 *
 */
public interface VirtualNetworkApplianceManager extends Manager, VirtualNetworkApplianceService{
    public static final int DEFAULT_ROUTER_VM_RAMSIZE = 128;            // 128M
    public static final int DEFAULT_ROUTER_CPU_MHZ = 500;            	// 500 MHz
    public static final boolean USE_POD_VLAN = false;
    /**
    /*
     * Send ssh public/private key pair to specified host
     * @param hostId
     * @param pubKey
     * @param prvKey
     */
    boolean sendSshKeysToHost(Long hostId, String pubKey, String prvKey);

    /**
     * save a vm password on the router.
     * @param routers TODO
     * 
     */
    boolean savePasswordToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile, 
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

    boolean saveSSHPublicKeyToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile,
            List<? extends VirtualRouter> routers, String SSHPublicKey) throws ResourceUnavailableException;

    boolean saveUserDataToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException;

	List<DomainRouterVO> deployVirtualRouterInGuestNetwork(Network guestNetwork, DeployDestination dest, Account owner, 
	        Map<VirtualMachineProfile.Param, Object> params, boolean isRedundant) throws InsufficientCapacityException,
	        ResourceUnavailableException, ConcurrentOperationException;
	
    boolean startRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) 
            throws ResourceUnavailableException;
	
	boolean deleteRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers) 
	        throws ResourceUnavailableException;
    
    boolean associatePublicIP (Network network, final List<? extends PublicIpAddress> ipAddress, 
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException;
    
    boolean applyFirewallRules(Network network, final List<? extends FirewallRule> rules, 
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException;
    
    List<VirtualRouter> getRoutersForNetwork(long networkId);

    String[] applyVpnUsers(Network network, List<? extends VpnUser> users, List<DomainRouterVO> routers) 
            throws ResourceUnavailableException;
    
    VirtualRouter stop(VirtualRouter router, boolean forced, User callingUser, Account callingAccount) 
            throws ConcurrentOperationException, ResourceUnavailableException;

    String getDnsBasicZoneUpdate();
    
    boolean applyStaticNats(Network network, final List<? extends StaticNat> rules, List<? extends VirtualRouter> routers) 
            throws ResourceUnavailableException;
    
	boolean applyDhcpEntry(Network config, NicProfile nic, VirtualMachineProfile<UserVm> vm, DeployDestination dest, 
	        List<DomainRouterVO> routers) throws ResourceUnavailableException;
	
	boolean applyUserData(Network config, NicProfile nic, VirtualMachineProfile<UserVm> vm, DeployDestination dest, 
	        List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean applyLoadBalancingRules(Network network, List<? extends LoadBalancingRule> rules, List<? extends VirtualRouter> routers) throws ResourceUnavailableException;


    boolean configDhcpForSubnet(Network network, NicProfile nic, VirtualMachineProfile<UserVm> uservm, DeployDestination dest, List<DomainRouterVO> routers) throws ResourceUnavailableException ;

    boolean removeDhcpSupportForSubnet(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException;

    boolean setupDhcpForPvlan(boolean add, DomainRouterVO router, Long hostId, NicProfile nic);
}