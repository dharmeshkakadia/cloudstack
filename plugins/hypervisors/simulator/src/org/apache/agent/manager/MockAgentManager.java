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
package org.apache.agent.manager;

import javax.naming.ConfigurationException;

import org.apache.agent.api.Answer;
import org.apache.agent.api.CheckHealthCommand;
import org.apache.agent.api.CheckNetworkCommand;
import org.apache.agent.api.GetHostStatsAnswer;
import org.apache.agent.api.GetHostStatsCommand;
import org.apache.agent.api.MaintainCommand;
import org.apache.agent.api.PingTestCommand;
import org.apache.resource.AgentResourceBase;
import org.apache.simulator.MockHost;
import org.apache.utils.component.Manager;

import java.util.Map;

public interface MockAgentManager extends Manager {
	public static final long DEFAULT_HOST_MEM_SIZE = 8 * 1024 * 1024 * 1024L; // 8G, unit of Mbytes
    public static final int DEFAULT_HOST_CPU_CORES = 4; // 2 dual core CPUs (2 x 2)
	public static final int DEFAULT_HOST_SPEED_MHZ = 8000; // 1 GHz CPUs

	boolean configure(String name, Map<String, Object> params) throws ConfigurationException;

	Map<AgentResourceBase, Map<String, String>> createServerResources(Map<String, Object> params);

	boolean handleSystemVMStart(long vmId, String privateIpAddress, String privateMacAddress, String privateNetMask,
			long dcId, long podId, String name, String vmType, String url);

	boolean handleSystemVMStop(long vmId);

	GetHostStatsAnswer getHostStatistic(GetHostStatsCommand cmd);

	Answer checkHealth(CheckHealthCommand cmd);

	Answer pingTest(PingTestCommand cmd);

	MockHost getHost(String guid);

	Answer maintain(MaintainCommand cmd);

	Answer checkNetworkCommand(CheckNetworkCommand cmd);
}
