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

import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupRoutingCommand;
import org.apache.dc.DataCenterVO;
import org.apache.dc.HostPodVO;
import org.apache.dc.PodCluster;
import org.apache.exception.AgentUnavailableException;
import org.apache.host.Host;
import org.apache.host.HostStats;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.Host.Type;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.resource.ResourceListener;
import org.apache.resource.ResourceService;
import org.apache.resource.ResourceState;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.resource.ResourceState.Event;
import org.apache.service.ServiceOfferingVO;
import org.apache.template.VirtualMachineTemplate;
import org.apache.utils.Pair;
import org.apache.utils.fsm.NoTransitionException;

/**
 * ResourceManager manages how physical resources are organized within the
 * CloudStack. It also manages the life cycle of the physical resources.
 */
public interface ResourceManager extends ResourceService{
    /**
     * Register a listener for different types of resource life cycle events.
     * There can only be one type of listener per type of host.
     * 
     * @param Event type see ResourceListener.java, allow combination of multiple events.
     * @param listener the listener to notify.
     */
    public void registerResourceEvent(Integer event, ResourceListener listener);
    
    public void unregisterResourceEvent(ResourceListener listener);
    
    /**
     * 
     * @param name of adapter
     * @param adapter
     * @param hates, a list of names which will be eliminated by this adapter. Especially for the case where 
     * can be only one adapter responds to an event, e.g. startupCommand
     */
    public void registerResourceStateAdapter(String name, ResourceStateAdapter adapter);
    
    public void unregisterResourceStateAdapter(String name);
    
	public Host createHostAndAgent(Long hostId, ServerResource resource, Map<String, String> details, boolean old, List<String> hostTags,
	        boolean forRebalance);
	
	public Host addHost(long zoneId, ServerResource resource, Type hostType, Map<String, String> hostDetails);
	
	public HostVO createHostVOForConnectedAgent(StartupCommand[] cmds);
	
	public void checkCIDR(HostPodVO pod, DataCenterVO dc, String serverPrivateIP, String serverPrivateNetmask);
	
	public HostVO fillRoutingHostVO(HostVO host, StartupRoutingCommand ssCmd, HypervisorType hyType, Map<String, String> details, List<String> hostTags);
	
	public void deleteRoutingHost(HostVO host, boolean isForced, boolean forceDestroyStorage) throws UnableDeleteHostException;
	
    public boolean executeUserRequest(long hostId, ResourceState.Event event) throws AgentUnavailableException;

	boolean resourceStateTransitTo(Host host, Event event, long msId) throws NoTransitionException;

	boolean umanageHost(long hostId);

	boolean maintenanceFailed(long hostId);
	
	public boolean maintain(final long hostId) throws AgentUnavailableException;
	
    @Override
    public boolean deleteHost(long hostId, boolean isForced, boolean isForceDeleteStorage);
    
    public List<HostVO> findDirectlyConnectedHosts();
    
    public List<HostVO> listAllUpAndEnabledHosts(Host.Type type, Long clusterId, Long podId, long dcId);
    
    public List<HostVO> listAllHostsInCluster(long clusterId);
    
    public List<HostVO> listHostsInClusterByStatus(long clusterId, Status status);
    
    public List<HostVO> listAllUpAndEnabledHostsInOneZoneByType(Host.Type type, long dcId);
    public List<HostVO> listAllUpAndEnabledHostsInOneZoneByHypervisor(HypervisorType type, long dcId);
    
    public List<HostVO> listAllHostsInOneZoneByType(Host.Type type, long dcId);
    
    public List<HostVO> listAllHostsInAllZonesByType(Type type);
    
    public List<HypervisorType> listAvailHypervisorInZone(Long hostId, Long zoneId);
    
    public HostVO findHostByGuid(String guid);
    
    public HostVO findHostByName(String name);
    
    public List<HostVO> listHostsByNameLike(String name);
    
    /**
     * Find a pod based on the user id, template, and data center.
     * 
     * @param template
     * @param dc
     * @param userId
     * @return
     */
    Pair<HostPodVO, Long> findPod(VirtualMachineTemplate template, ServiceOfferingVO offering, DataCenterVO dc, long accountId, Set<Long> avoids);
    
    HostStats getHostStatistics(long hostId);
    
    Long getGuestOSCategoryId(long hostId);
    
    String getHostTags(long hostId);
    
    List<PodCluster> listByDataCenter(long dcId);

	List<HostVO> listAllNotInMaintenanceHostsInOneZone(Type type, Long dcId);

	HypervisorType getDefaultHypervisor(long zoneId);

	HypervisorType getAvailableHypervisor(long zoneId);

    Discoverer getMatchingDiscover(HypervisorType hypervisorType);

	List<HostVO> findHostByGuid(long dcId, String guid);

    /**
     * @param type
     * @param clusterId
     * @param podId
     * @param dcId
     * @return
     */
    List<HostVO> listAllUpAndEnabledNonHAHosts(Type type, Long clusterId, Long podId, long dcId);
}
