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
package org.apache.ovm.hypervisor;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.FenceAnswer;
import org.apache.agent.api.FenceCommand;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.OperationTimedoutException;
import org.apache.ha.FenceBuilder;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.log4j.Logger;
import org.apache.resource.ResourceManager;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.VMInstanceVO;

@Local(value=FenceBuilder.class)
public class OvmFencer extends AdapterBase implements FenceBuilder {
	private static final Logger s_logger = Logger.getLogger(OvmFencer.class);
	@Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		return true;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public OvmFencer() {
		super();
	}

	@Override
	public Boolean fenceOff(VMInstanceVO vm, HostVO host) {
		if (host.getHypervisorType() != HypervisorType.Ovm) {
			s_logger.debug("Don't know how to fence non Ovm hosts " + host.getHypervisorType());
			return null;
		}
		
		List<HostVO> hosts = _resourceMgr.listAllHostsInCluster(host.getClusterId());
		FenceCommand fence = new FenceCommand(vm, host);
		
		for (HostVO h : hosts) {
			if (h.getHypervisorType() != HypervisorType.Ovm) {
				continue;
			}
			
			if( h.getStatus() != Status.Up ) {
				continue;
			}
			
			if( h.getId() == host.getId() ) {
				continue;
			}
			
			FenceAnswer answer;
			try {
				answer = (FenceAnswer)_agentMgr.send(h.getId(), fence);
			} catch (AgentUnavailableException e) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
				}
				continue;
			} catch (OperationTimedoutException e) {
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Moving on to the next host because " + h.toString() + " is unavailable");
				}
				continue;
			}
			
			if (answer != null && answer.getResult()) {
				return true;
			}
		}
		
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Unable to fence off " + vm.toString() + " on " + host.toString());
		}
		
		return false;
	}

}