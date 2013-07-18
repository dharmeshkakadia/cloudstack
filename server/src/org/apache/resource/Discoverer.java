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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.exception.DiscoveryException;
import org.apache.host.HostVO;
import org.apache.hypervisor.Hypervisor;
import org.apache.resource.ServerResource;
import org.apache.utils.component.Adapter;

/**
 * Discoverer encapsulates interfaces that will discover resources.
 *
 */
public interface Discoverer extends Adapter {
    /**
     * Given an accessible ip address, find out what it is.
     * 
     * @param url
     * @param username
     * @param password
     * @return ServerResource
     */
    Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI uri, String username, String password, List<String> hostTags) throws DiscoveryException;

	void postDiscovery(List<HostVO> hosts, long msId)  throws DiscoveryException;
	
	boolean matchHypervisor(String hypervisor);
	Hypervisor.HypervisorType getHypervisorType();
	public void putParam(Map<String, String> params);
	
	ServerResource reloadResource(HostVO host);
	
}
