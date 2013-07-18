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
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.api.command.user.vpc.ListPrivateGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpc.ListStaticRoutesCmd;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.NetworkRuleConflictException;
import org.apache.exception.ResourceAllocationException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.PhysicalNetwork;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.addr.PublicIp;
import org.apache.network.vpc.PrivateGateway;
import org.apache.network.vpc.StaticRoute;
import org.apache.network.vpc.Vpc;
import org.apache.network.vpc.VpcGateway;
import org.apache.network.vpc.VpcManager;
import org.apache.network.vpc.VpcService;
import org.apache.offering.NetworkOffering;
import org.apache.user.Account;
import org.apache.utils.Pair;
import org.apache.utils.component.ManagerBase;
import org.apache.vpc.dao.MockVpcDaoImpl;
import org.springframework.stereotype.Component;

@Component
@Local(value = { VpcManager.class, VpcService.class })
public class MockVpcManagerImpl extends ManagerBase implements VpcManager {
    @Inject MockVpcDaoImpl _vpcDao;


    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getVpc(long)
     */
    @Override
    public Vpc getVpc(long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getActiveVpc(long)
     */
    @Override
    public Vpc getActiveVpc(long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getVpcNetworks(long)
     */
    @Override
    public List<? extends Network> getVpcNetworks(long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#createVpc(long, long, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Vpc createVpc(long zoneId, long vpcOffId, long vpcOwnerId, String vpcName, String displayText, String cidr, String networkDomain) throws ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#deleteVpc(long)
     */
    @Override
    public boolean deleteVpc(long vpcId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#updateVpc(long, java.lang.String, java.lang.String)
     */
    @Override
    public Vpc updateVpc(long vpcId, String vpcName, String displayText) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#listVpcs(java.lang.Long, java.lang.String, java.lang.String, java.util.List, java.lang.String, java.lang.Long, java.lang.String, java.lang.String, java.lang.Long, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.util.Map, java.lang.Long)
     */
    @Override
    public List<? extends Vpc> listVpcs(Long id, String vpcName, String displayText, List<String> supportedServicesStr, String cidr, Long vpcOffId, String state, String accountName, Long domainId, String keyword,
            Long startIndex, Long pageSizeVal, Long zoneId, Boolean isRecursive, Boolean listAll, Boolean restartRequired, Map<String, String> tags, Long projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#startVpc(long, boolean)
     */
    @Override
    public boolean startVpc(long vpcId, boolean destroyOnFailure) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#shutdownVpc(long)
     */
    @Override
    public boolean shutdownVpc(long vpcId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#restartVpc(long)
     */
    @Override
    public boolean restartVpc(long id) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getVpcPrivateGateway(long)
     */
    @Override
    public PrivateGateway getVpcPrivateGateway(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrivateGateway createVpcPrivateGateway(long vpcId, Long physicalNetworkId, String vlan, String ipAddress, String gateway, String netmask, long gatewayOwnerId, Boolean isSoruceNat, Long aclId) throws ResourceAllocationException, ConcurrentOperationException, InsufficientCapacityException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#createVpcPrivateGateway(long, java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long)
     */

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#applyVpcPrivateGateway(long, boolean)
     */
    @Override
    public PrivateGateway applyVpcPrivateGateway(long gatewayId, boolean destroyOnFailure) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#deleteVpcPrivateGateway(long)
     */
    @Override
    public boolean deleteVpcPrivateGateway(long gatewayId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#listPrivateGateway(org.apache.cloudstack.api.commands.ListPrivateGatewaysCmd)
     */
    @Override
    public Pair<List<PrivateGateway>, Integer> listPrivateGateway(ListPrivateGatewaysCmd listPrivateGatewaysCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getStaticRoute(long)
     */
    @Override
    public StaticRoute getStaticRoute(long routeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#applyStaticRoutes(long)
     */
    @Override
    public boolean applyStaticRoutes(long vpcId) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#revokeStaticRoute(long)
     */
    @Override
    public boolean revokeStaticRoute(long routeId) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#createStaticRoute(long, java.lang.String)
     */
    @Override
    public StaticRoute createStaticRoute(long gatewayId, String cidr) throws NetworkRuleConflictException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#listStaticRoutes(org.apache.cloudstack.api.commands.ListStaticRoutesCmd)
     */
    @Override
    public Pair<List<? extends StaticRoute>, Integer> listStaticRoutes(ListStaticRoutesCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#getVpcGateway(long)
     */
    @Override
    public VpcGateway getVpcGateway(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcService#associateIPToVpc(long, long)
     */
    @Override
    public IpAddress associateIPToVpc(long ipId, long vpcId) throws ResourceAllocationException, ResourceUnavailableException, InsufficientAddressCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#getVpcsForAccount(long)
     */
    @Override
    public List<? extends Vpc> getVpcsForAccount(long accountId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#destroyVpc(org.apache.network.vpc.Vpc)
     */
    @Override
    public boolean destroyVpc(Vpc vpc, Account caller, Long callerUserId) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }



    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#ipUsedInVpc(org.apache.network.IpAddress)
     */
    @Override
    public boolean isIpAllocatedToVpc(IpAddress ip) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#unassignIPFromVpcNetwork(long, long)
     */
    @Override
    public void unassignIPFromVpcNetwork(long ipId, long networkId) {
        // TODO Auto-generated method stub

    }

    @Override
    public Network createVpcGuestNetwork(long ntwkOffId, String name, String displayText, String gateway, String cidr, String vlanId, String networkDomain, Account owner, Long domainId, PhysicalNetwork pNtwk, long zoneId, ACLType aclType, Boolean subdomainAccess, long vpcId, Long aclId, Account caller, Boolean displayNetworkEnabled) throws ConcurrentOperationException, InsufficientCapacityException, ResourceAllocationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#assignSourceNatIpAddressToVpc(org.apache.user.Account, org.apache.network.vpc.Vpc)
     */
    @Override
    public PublicIp assignSourceNatIpAddressToVpc(Account owner, Vpc vpc) throws InsufficientAddressCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
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
     * @see org.apache.network.vpc.VpcManager#validateNtwkOffForVpc(org.apache.offering.NetworkOffering, java.util.List)
     */
    @Override
    public void validateNtwkOffForVpc(NetworkOffering guestNtwkOff, List<Service> supportedSvcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.network.vpc.VpcManager#getSupportedVpcHypervisors()
     */
    @Override
    public List<HypervisorType> getSupportedVpcHypervisors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Service, Set<Provider>> getVpcOffSvcProvidersMap(long vpcOffId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validateNtwkOffForNtwkInVpc(Long networkId, long newNtwkOffId, String newCidr, String newNetworkDomain, Vpc vpc, String gateway, Account networkOwner, Long aclId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<PrivateGateway> getVpcPrivateGateways(long vpcId) {
        return null;
    }

}
