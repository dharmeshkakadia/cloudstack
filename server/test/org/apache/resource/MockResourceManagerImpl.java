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

package org.apache.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupRoutingCommand;
import org.apache.cloudstack.api.command.admin.cluster.AddClusterCmd;
import org.apache.cloudstack.api.command.admin.cluster.DeleteClusterCmd;
import org.apache.cloudstack.api.command.admin.host.*;
import org.apache.cloudstack.api.command.admin.storage.*;
import org.apache.cloudstack.api.command.admin.swift.*;
import org.apache.dc.DataCenterVO;
import org.apache.dc.HostPodVO;
import org.apache.dc.PodCluster;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.DiscoveryException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.ResourceInUseException;
import org.apache.host.Host;
import org.apache.host.HostStats;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.Host.Type;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.org.Cluster;
import org.apache.resource.Discoverer;
import org.apache.resource.ResourceListener;
import org.apache.resource.ResourceManager;
import org.apache.resource.ResourceStateAdapter;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.resource.ResourceState.Event;
import org.apache.service.ServiceOfferingVO;
import org.apache.storage.ImageStore;
import org.apache.storage.S3;
import org.apache.storage.Swift;
import org.apache.template.VirtualMachineTemplate;
import org.apache.utils.Pair;
import org.apache.utils.component.Manager;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.fsm.NoTransitionException;

@Local(value = {ResourceManager.class})
public class MockResourceManagerImpl extends ManagerBase implements ResourceManager {

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#updateHost(org.apache.api.commands.UpdateHostCmd)
     */
    @Override
    public Host updateHost(UpdateHostCmd cmd) throws NoTransitionException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#cancelMaintenance(org.apache.api.commands.CancelMaintenanceCmd)
     */
    @Override
    public Host cancelMaintenance(CancelMaintenanceCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#reconnectHost(org.apache.api.commands.ReconnectHostCmd)
     */
    @Override
    public Host reconnectHost(ReconnectHostCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#discoverCluster(org.apache.api.commands.AddClusterCmd)
     */
    @Override
    public List<? extends Cluster> discoverCluster(AddClusterCmd cmd) throws IllegalArgumentException,
            DiscoveryException, ResourceInUseException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#deleteCluster(org.apache.api.commands.DeleteClusterCmd)
     */
    @Override
    public boolean deleteCluster(DeleteClusterCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#updateCluster(org.apache.org.Cluster, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Cluster updateCluster(Cluster cluster, String clusterType, String hypervisor, String allocationState,
                                 String managedstate, Float memoryOvercommitRaito, Float cpuOvercommitRatio) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#discoverHosts(org.apache.api.commands.AddHostCmd)
     */
    @Override
    public List<? extends Host> discoverHosts(AddHostCmd cmd) throws IllegalArgumentException, DiscoveryException,
            InvalidParameterValueException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#discoverHosts(org.apache.api.commands.AddSecondaryStorageCmd)
     */
    @Override
    public List<? extends Host> discoverHosts(AddSecondaryStorageCmd cmd) throws IllegalArgumentException,
            DiscoveryException, InvalidParameterValueException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#maintain(org.apache.api.commands.PrepareForMaintenanceCmd)
     */
    @Override
    public Host maintain(PrepareForMaintenanceCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#updateHostPassword(org.apache.api.commands.UpdateHostPasswordCmd)
     */
    @Override
    public boolean updateHostPassword(UpdateHostPasswordCmd upasscmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#getHost(long)
     */
    @Override
    public Host getHost(long hostId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#getCluster(java.lang.Long)
     */
    @Override
    public Cluster getCluster(Long clusterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#discoverSwift(org.apache.api.commands.AddSwiftCmd)
     */
    @Override
    public Swift discoverSwift(AddSwiftCmd addSwiftCmd) throws DiscoveryException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#discoverS3(org.apache.api.commands.AddS3Cmd)
     */
    @Override
    public S3 discoverS3(AddS3Cmd cmd) throws DiscoveryException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#getSupportedHypervisorTypes(long, boolean, java.lang.Long)
     */
    @Override
    public List<HypervisorType> getSupportedHypervisorTypes(long zoneId, boolean forVirtualRouter, Long podId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#listSwifts(org.apache.api.commands.ListSwiftsCmd)
     */
    @Override
    public Pair<List<? extends Swift>, Integer> listSwifts(ListSwiftsCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceService#listS3s(org.apache.api.commands.ListS3sCmd)
     */
    @Override
    public List<? extends S3> listS3s(ListS3sCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#registerResourceEvent(java.lang.Integer, org.apache.resource.ResourceListener)
     */
    @Override
    public void registerResourceEvent(Integer event, ResourceListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#unregisterResourceEvent(org.apache.resource.ResourceListener)
     */
    @Override
    public void unregisterResourceEvent(ResourceListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#registerResourceStateAdapter(java.lang.String, org.apache.resource.ResourceStateAdapter)
     */
    @Override
    public void registerResourceStateAdapter(String name, ResourceStateAdapter adapter) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#unregisterResourceStateAdapter(java.lang.String)
     */
    @Override
    public void unregisterResourceStateAdapter(String name) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#createHostAndAgent(java.lang.Long, org.apache.resource.ServerResource, java.util.Map, boolean, java.util.List, boolean)
     */
    @Override
    public Host createHostAndAgent(Long hostId, ServerResource resource, Map<String, String> details, boolean old,
            List<String> hostTags, boolean forRebalance) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#addHost(long, org.apache.resource.ServerResource, org.apache.host.Host.Type, java.util.Map)
     */
    @Override
    public Host addHost(long zoneId, ServerResource resource, Type hostType, Map<String, String> hostDetails) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#createHostVOForConnectedAgent(org.apache.agent.api.StartupCommand[])
     */
    @Override
    public HostVO createHostVOForConnectedAgent(StartupCommand[] cmds) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#checkCIDR(org.apache.dc.HostPodVO, org.apache.dc.DataCenterVO, java.lang.String, java.lang.String)
     */
    @Override
    public void checkCIDR(HostPodVO pod, DataCenterVO dc, String serverPrivateIP, String serverPrivateNetmask) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#fillRoutingHostVO(org.apache.host.HostVO, org.apache.agent.api.StartupRoutingCommand, org.apache.hypervisor.Hypervisor.HypervisorType, java.util.Map, java.util.List)
     */
    @Override
    public HostVO fillRoutingHostVO(HostVO host, StartupRoutingCommand ssCmd, HypervisorType hyType,
            Map<String, String> details, List<String> hostTags) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#deleteRoutingHost(org.apache.host.HostVO, boolean, boolean)
     */
    @Override
    public void deleteRoutingHost(HostVO host, boolean isForced, boolean forceDestroyStorage)
            throws UnableDeleteHostException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#executeUserRequest(long, org.apache.resource.ResourceState.Event)
     */
    @Override
    public boolean executeUserRequest(long hostId, Event event) throws AgentUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#resourceStateTransitTo(org.apache.host.Host, org.apache.resource.ResourceState.Event, long)
     */
    @Override
    public boolean resourceStateTransitTo(Host host, Event event, long msId) throws NoTransitionException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#umanageHost(long)
     */
    @Override
    public boolean umanageHost(long hostId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#maintenanceFailed(long)
     */
    @Override
    public boolean maintenanceFailed(long hostId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#maintain(long)
     */
    @Override
    public boolean maintain(long hostId) throws AgentUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#deleteHost(long, boolean, boolean)
     */
    @Override
    public boolean deleteHost(long hostId, boolean isForced, boolean isForceDeleteStorage) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#findDirectlyConnectedHosts()
     */
    @Override
    public List<HostVO> findDirectlyConnectedHosts() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllUpAndEnabledHosts(org.apache.host.Host.Type, java.lang.Long, java.lang.Long, long)
     */
    @Override
    public List<HostVO> listAllUpAndEnabledHosts(Type type, Long clusterId, Long podId, long dcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllHostsInCluster(long)
     */
    @Override
    public List<HostVO> listAllHostsInCluster(long clusterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listHostsInClusterByStatus(long, org.apache.host.Status)
     */
    @Override
    public List<HostVO> listHostsInClusterByStatus(long clusterId, Status status) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllUpAndEnabledHostsInOneZoneByType(org.apache.host.Host.Type, long)
     */
    @Override
    public List<HostVO> listAllUpAndEnabledHostsInOneZoneByType(Type type, long dcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllHostsInOneZoneByType(org.apache.host.Host.Type, long)
     */
    @Override
    public List<HostVO> listAllHostsInOneZoneByType(Type type, long dcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllHostsInAllZonesByType(org.apache.host.Host.Type)
     */
    @Override
    public List<HostVO> listAllHostsInAllZonesByType(Type type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAvailHypervisorInZone(java.lang.Long, java.lang.Long)
     */
    @Override
    public List<HypervisorType> listAvailHypervisorInZone(Long hostId, Long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#findHostByGuid(java.lang.String)
     */
    @Override
    public HostVO findHostByGuid(String guid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#findHostByName(java.lang.String)
     */
    @Override
    public HostVO findHostByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listHostsByNameLike(java.lang.String)
     */
    @Override
    public List<HostVO> listHostsByNameLike(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#findPod(org.apache.template.VirtualMachineTemplate, org.apache.service.ServiceOfferingVO, org.apache.dc.DataCenterVO, long, java.util.Set)
     */
    @Override
    public Pair<HostPodVO, Long> findPod(VirtualMachineTemplate template, ServiceOfferingVO offering, DataCenterVO dc,
            long accountId, Set<Long> avoids) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getHostStatistics(long)
     */
    @Override
    public HostStats getHostStatistics(long hostId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getGuestOSCategoryId(long)
     */
    @Override
    public Long getGuestOSCategoryId(long hostId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getHostTags(long)
     */
    @Override
    public String getHostTags(long hostId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listByDataCenter(long)
     */
    @Override
    public List<PodCluster> listByDataCenter(long dcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllNotInMaintenanceHostsInOneZone(org.apache.host.Host.Type, java.lang.Long)
     */
    @Override
    public List<HostVO> listAllNotInMaintenanceHostsInOneZone(Type type, Long dcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getDefaultHypervisor(long)
     */
    @Override
    public HypervisorType getDefaultHypervisor(long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getAvailableHypervisor(long)
     */
    @Override
    public HypervisorType getAvailableHypervisor(long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#getMatchingDiscover(org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public Discoverer getMatchingDiscover(HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#findHostByGuid(long, java.lang.String)
     */
    @Override
    public List<HostVO> findHostByGuid(long dcId, String guid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.resource.ResourceManager#listAllUpAndEnabledNonHAHosts(org.apache.host.Host.Type, java.lang.Long, java.lang.Long, long)
     */
    @Override
    public List<HostVO> listAllUpAndEnabledNonHAHosts(Type type, Long clusterId, Long podId, long dcId) {
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
        return "MockResourceManagerImpl";
    }

	@Override
	public List<HostVO> listAllUpAndEnabledHostsInOneZoneByHypervisor(
			HypervisorType type, long dcId) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean releaseHostReservation(Long hostId) {
        // TODO Auto-generated method stub
        return false;
    }

}
