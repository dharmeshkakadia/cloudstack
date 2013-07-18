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

package org.apache.vpc;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.cloudstack.api.command.admin.router.UpgradeRouterCmd;
import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.Network;
import org.apache.network.PublicIpAddress;
import org.apache.network.RemoteAccessVpn;
import org.apache.network.Site2SiteVpnConnection;
import org.apache.network.VpcVirtualNetworkApplianceService;
import org.apache.network.VpnUser;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.router.VirtualRouter;
import org.apache.network.router.VpcVirtualNetworkApplianceManager;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.StaticNat;
import org.apache.network.vpc.NetworkACLItem;
import org.apache.network.vpc.PrivateGateway;
import org.apache.network.vpc.StaticRouteProfile;
import org.apache.network.vpc.Vpc;
import org.apache.user.Account;
import org.apache.user.User;
import org.apache.uservm.UserVm;
import org.apache.utils.component.ManagerBase;
import org.apache.vm.DomainRouterVO;
import org.apache.vm.NicProfile;
import org.apache.vm.VirtualMachineProfile;
import org.apache.vm.VirtualMachineProfile.Param;
import org.springframework.stereotype.Component;

@Component
@Local(value = {VpcVirtualNetworkApplianceManager.class, VpcVirtualNetworkApplianceService.class})
public class MockVpcVirtualNetworkApplianceManager extends ManagerBase implements VpcVirtualNetworkApplianceManager,
VpcVirtualNetworkApplianceService {

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#sendSshKeysToHost(java.lang.Long, java.lang.String, java.lang.String)
     */
    @Override
    public boolean sendSshKeysToHost(Long hostId, String pubKey, String prvKey) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#savePasswordToRouter(org.apache.network.Network, org.apache.vm.NicProfile, org.apache.vm.VirtualMachineProfile, java.util.List)
     */
    @Override
    public boolean savePasswordToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean saveSSHPublicKeyToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile, List<? extends VirtualRouter> routers, String SSHPublicKey) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#saveUserDataToRouter(org.apache.network.Network, org.apache.vm.NicProfile, org.apache.vm.VirtualMachineProfile, java.util.List)
     */
    @Override
    public boolean saveUserDataToRouter(Network network, NicProfile nic, VirtualMachineProfile<UserVm> profile,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#deployVirtualRouterInGuestNetwork(org.apache.network.Network, org.apache.deploy.DeployDestination, org.apache.user.Account, java.util.Map, boolean)
     */
    @Override
    public List<DomainRouterVO> deployVirtualRouterInGuestNetwork(Network guestNetwork, DeployDestination dest,
            Account owner, Map<Param, Object> params, boolean isRedundant) throws InsufficientCapacityException,
            ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#startRemoteAccessVpn(org.apache.network.Network, org.apache.network.RemoteAccessVpn, java.util.List)
     */
    @Override
    public boolean startRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#deleteRemoteAccessVpn(org.apache.network.Network, org.apache.network.RemoteAccessVpn, java.util.List)
     */
    @Override
    public boolean deleteRemoteAccessVpn(Network network, RemoteAccessVpn vpn, List<? extends VirtualRouter> routers)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#associatePublicIP(org.apache.network.Network, java.util.List, java.util.List)
     */
    @Override
    public boolean associatePublicIP(Network network, List<? extends PublicIpAddress> ipAddress,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#applyFirewallRules(org.apache.network.Network, java.util.List, java.util.List)
     */
    @Override
    public boolean applyFirewallRules(Network network, List<? extends FirewallRule> rules,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#getRoutersForNetwork(long)
     */
    @Override
    public List<VirtualRouter> getRoutersForNetwork(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#applyVpnUsers(org.apache.network.Network, java.util.List, java.util.List)
     */
    @Override
    public String[] applyVpnUsers(Network network, List<? extends VpnUser> users, List<DomainRouterVO> routers)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#stop(org.apache.network.router.VirtualRouter, boolean, org.apache.user.User, org.apache.user.Account)
     */
    @Override
    public VirtualRouter stop(VirtualRouter router, boolean forced, User callingUser, Account callingAccount)
            throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#getDnsBasicZoneUpdate()
     */
    @Override
    public String getDnsBasicZoneUpdate() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#applyStaticNats(org.apache.network.Network, java.util.List, java.util.List)
     */
    @Override
    public boolean applyStaticNats(Network network, List<? extends StaticNat> rules,
            List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#applyDhcpEntry(org.apache.network.Network, org.apache.vm.NicProfile, org.apache.vm.VirtualMachineProfile, org.apache.deploy.DeployDestination, java.util.List)
     */
    @Override
    public boolean applyDhcpEntry(Network config, NicProfile nic, VirtualMachineProfile<UserVm> vm,
            DeployDestination dest, List<DomainRouterVO> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VirtualNetworkApplianceManager#applyUserData(org.apache.network.Network, org.apache.vm.NicProfile, org.apache.vm.VirtualMachineProfile, org.apache.deploy.DeployDestination, java.util.List)
     */
    @Override
    public boolean applyUserData(Network config, NicProfile nic, VirtualMachineProfile<UserVm> vm,
            DeployDestination dest, List<DomainRouterVO> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean configDhcpForSubnet(Network network, NicProfile nic, VirtualMachineProfile<UserVm> uservm, DeployDestination dest, List<DomainRouterVO> routers) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeDhcpSupportForSubnet(Network network, List<DomainRouterVO> routers) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#startRouter(long, boolean)
     */
    @Override
    public VirtualRouter startRouter(long routerId, boolean reprogramNetwork) throws ConcurrentOperationException,
    ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#rebootRouter(long, boolean)
     */
    @Override
    public VirtualRouter rebootRouter(long routerId, boolean reprogramNetwork) throws ConcurrentOperationException,
    ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#upgradeRouter(org.apache.api.commands.UpgradeRouterCmd)
     */
    @Override
    public VirtualRouter upgradeRouter(UpgradeRouterCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#stopRouter(long, boolean)
     */
    @Override
    public VirtualRouter stopRouter(long routerId, boolean forced) throws ResourceUnavailableException,
    ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#startRouter(long)
     */
    @Override
    public VirtualRouter startRouter(long id) throws ResourceUnavailableException, InsufficientCapacityException,
    ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VirtualNetworkApplianceService#destroyRouter(long, org.apache.user.Account, java.lang.Long)
     */
    @Override
    public VirtualRouter destroyRouter(long routerId, Account caller, Long callerUserId)
            throws ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        return true;

    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        return true;

    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VpcVirtualNetworkApplianceService#addVpcRouterToGuestNetwork(org.apache.network.router.VirtualRouter, org.apache.network.Network, boolean)
     */
    @Override
    public boolean addVpcRouterToGuestNetwork(VirtualRouter router, Network network, boolean isRedundant)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.VpcVirtualNetworkApplianceService#removeVpcRouterFromGuestNetwork(org.apache.network.router.VirtualRouter, org.apache.network.Network, boolean)
     */
    @Override
    public boolean removeVpcRouterFromGuestNetwork(VirtualRouter router, Network network, boolean isRedundant)
            throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#deployVirtualRouterInVpc(org.apache.network.vpc.Vpc, org.apache.deploy.DeployDestination, org.apache.user.Account, java.util.Map)
     */
    @Override
    public List<DomainRouterVO> deployVirtualRouterInVpc(Vpc vpc, DeployDestination dest, Account owner,
            Map<Param, Object> params) throws InsufficientCapacityException, ConcurrentOperationException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean applyNetworkACLs(Network network, List<? extends NetworkACLItem> rules, List<? extends VirtualRouter> routers,  boolean privateGateway) throws ResourceUnavailableException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#setupPrivateGateway(org.apache.network.vpc.PrivateGateway, org.apache.network.router.VirtualRouter)
     */
    @Override
    public boolean setupPrivateGateway(PrivateGateway gateway, VirtualRouter router)
            throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#destroyPrivateGateway(org.apache.network.vpc.PrivateGateway, org.apache.network.router.VirtualRouter)
     */
    @Override
    public boolean destroyPrivateGateway(PrivateGateway gateway, VirtualRouter router)
            throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#applyStaticRoutes(java.util.List, java.util.List)
     */
    @Override
    public boolean applyStaticRoutes(List<StaticRouteProfile> routes, List<DomainRouterVO> routers)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#startSite2SiteVpn(org.apache.network.Site2SiteVpnConnection, org.apache.network.router.VirtualRouter)
     */
    @Override
    public boolean startSite2SiteVpn(Site2SiteVpnConnection conn, VirtualRouter router)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.router.VpcVirtualNetworkApplianceManager#stopSite2SiteVpn(org.apache.network.Site2SiteVpnConnection, org.apache.network.router.VirtualRouter)
     */
    @Override
    public boolean stopSite2SiteVpn(Site2SiteVpnConnection conn, VirtualRouter router)
            throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<DomainRouterVO> getVpcRouters(long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean applyLoadBalancingRules(Network network, List<? extends LoadBalancingRule> rules, List<? extends VirtualRouter> routers) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public VirtualRouter findRouter(long routerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setupDhcpForPvlan(boolean add, DomainRouterVO router, Long hostId,
            NicProfile nic) {
        // TODO Auto-generated method stub
        return false;
    }
}
