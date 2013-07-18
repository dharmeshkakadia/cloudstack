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
package org.apache.network.vpc;

import java.util.List;
import java.util.Map;

import org.apache.cloudstack.api.command.user.vpc.ListPrivateGatewaysCmd;
import org.apache.cloudstack.api.command.user.vpc.ListStaticRoutesCmd;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.NetworkRuleConflictException;
import org.apache.exception.ResourceAllocationException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.utils.Pair;

public interface VpcService {

    /**Returns existing VPC found by id
     * 
     * @param vpcId
     * @return
     */
    public Vpc getVpc(long vpcId);

    
    /**
     * Returns all the Guest networks that are part of VPC
     * 
     * @param vpcId
     * @return
     */
    public List<? extends Network> getVpcNetworks(long vpcId);

    /**
     * Persists VPC record in the database
     * 
     * @param zoneId
     * @param vpcOffId
     * @param vpcOwnerId
     * @param vpcName
     * @param displayText
     * @param cidr
     * @param networkDomain TODO
     * @return
     * @throws ResourceAllocationException TODO
     */
    public Vpc createVpc(long zoneId, long vpcOffId, long vpcOwnerId, String vpcName, String displayText, String cidr,
            String networkDomain) throws ResourceAllocationException;

    
    /**
     * Deletes a VPC
     * 
     * @param vpcId
     * @return
     * @throws InsufficientCapacityException
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    public boolean deleteVpc(long vpcId) throws ConcurrentOperationException, ResourceUnavailableException;

    
    /**
     * Updates VPC with new name/displayText
     * 
     * @param vpcId
     * @param vpcName
     * @param displayText
     * @return
     */
    public Vpc updateVpc(long vpcId, String vpcName, String displayText);

    
    /**
     * Lists VPC(s) based on the parameters passed to the method call
     * 
     * @param id
     * @param vpcName
     * @param displayText
     * @param supportedServicesStr
     * @param cidr
     * @param state TODO
     * @param accountName
     * @param domainId
     * @param keyword
     * @param startIndex
     * @param pageSizeVal
     * @param zoneId TODO
     * @param isRecursive TODO
     * @param listAll TODO
     * @param restartRequired TODO
     * @param tags TODO
     * @param projectId TODO
     * @param vpc
     * @return
     */
    public List<? extends Vpc> listVpcs(Long id, String vpcName, String displayText,
            List<String> supportedServicesStr, String cidr, Long vpcOffId, String state, String accountName, Long domainId,
            String keyword, Long startIndex, Long pageSizeVal, Long zoneId, Boolean isRecursive, Boolean listAll,
            Boolean restartRequired, Map<String, String> tags, Long projectId);

    /**
     * Starts VPC which includes starting VPC provider and applying all the neworking rules on the backend
     * 
     * @param vpcId
     * @param destroyOnFailure TODO
     * @return
     * @throws InsufficientCapacityException
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    boolean startVpc(long vpcId, boolean destroyOnFailure) throws ConcurrentOperationException,
                                                        ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Shuts down the VPC which includes shutting down all VPC provider and rules cleanup on the backend
     * 
     * @param vpcId
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdownVpc(long vpcId) throws ConcurrentOperationException, ResourceUnavailableException;

    
    /**
     * Restarts the VPC. VPC gets shutdown and started as a part of it
     * 
     * @param id
     * @return
     * @throws InsufficientCapacityException
     */
    boolean restartVpc(long id) throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * Returns a Private gateway found in the VPC by id
     * 
     * @param id
     * @return
     */
    PrivateGateway getVpcPrivateGateway(long id);

    
    /**
     * Persists VPC private gateway in the Database.
     * 
     *
     * @param vpcId TODO
     * @param physicalNetworkId
     * @param vlan
     * @param ipAddress
     * @param gateway
     * @param netmask
     * @param gatewayOwnerId
     * @param isSourceNat
     * @param aclId
     * @return
     * @throws InsufficientCapacityException
     * @throws ConcurrentOperationException
     * @throws ResourceAllocationException
     */
    public PrivateGateway createVpcPrivateGateway(long vpcId, Long physicalNetworkId, String vlan, String ipAddress,
                          String gateway, String netmask, long gatewayOwnerId, Boolean isSoruceNat, Long aclId) throws ResourceAllocationException,
            ConcurrentOperationException, InsufficientCapacityException;

    /**
     * Applies VPC private gateway on the backend, so it becomes functional
     * 
     * @param gatewayId
     * @param destroyOnFailure TODO
     * @return
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    public PrivateGateway applyVpcPrivateGateway(long gatewayId, boolean destroyOnFailure) throws ConcurrentOperationException, ResourceUnavailableException;

    
    /**
     * Deletes VPC private gateway
     * 
     * @param id
     * @return
     * @throws ResourceUnavailableException
     * @throws ConcurrentOperationException
     */
    boolean deleteVpcPrivateGateway(long gatewayId) throws ConcurrentOperationException, ResourceUnavailableException;

    
    /**
     * Returns the list of Private gateways existing in the VPC
     * 
     * @param listPrivateGatewaysCmd
     * @return
     */
    public Pair<List<PrivateGateway>, Integer> listPrivateGateway(ListPrivateGatewaysCmd listPrivateGatewaysCmd);

    
    /**
     * Returns Static Route found by Id
     * 
     * @param routeId
     * @return
     */
    StaticRoute getStaticRoute(long routeId);

    
    /**
     * Applies existing Static Routes to the VPC elements
     * 
     * @param vpcId
     * @return
     * @throws ResourceUnavailableException
     */
    public boolean applyStaticRoutes(long vpcId) throws ResourceUnavailableException;

    
    /**
     * Deletes static route from the backend and the database
     * 
     * @param routeId
     * @return TODO
     * @throws ResourceUnavailableException
     */
    public boolean revokeStaticRoute(long routeId) throws ResourceUnavailableException;

    
    /**
     * Persists static route entry in the Database
     * 
     * @param gatewayId
     * @param cidr
     * @return
     */
    public StaticRoute createStaticRoute(long gatewayId, String cidr) throws NetworkRuleConflictException;

    
    /**
     * Lists static routes based on parameters passed to the call
     * 
     * @param listStaticRoutesCmd
     * @return
     */
    public Pair<List<? extends StaticRoute>, Integer> listStaticRoutes(ListStaticRoutesCmd cmd);

    
    /**
     * Returns gateway (VPN or Public) existign in the VPC 
     * 
     * @param id
     * @return
     */
    VpcGateway getVpcGateway(long id);

    
    /**
     * Associates IP address from the Public network, to the VPC
     * 
     * @param ipId
     * @param vpcId
     * @return
     * @throws ResourceAllocationException
     * @throws ResourceUnavailableException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     */
    IpAddress associateIPToVpc(long ipId, long vpcId) throws ResourceAllocationException, ResourceUnavailableException,
        InsufficientAddressCapacityException, ConcurrentOperationException;

}