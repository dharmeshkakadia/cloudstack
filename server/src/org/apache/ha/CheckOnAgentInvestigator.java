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
package org.apache.ha;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.AgentManager;
import org.apache.agent.api.CheckVirtualMachineAnswer;
import org.apache.agent.api.CheckVirtualMachineCommand;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.OperationTimedoutException;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.log4j.Logger;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.VirtualMachine.State;

@Local(value=Investigator.class)
public class CheckOnAgentInvestigator extends AdapterBase implements Investigator {
    private final static Logger s_logger = Logger.getLogger(CheckOnAgentInvestigator.class);
	@Inject AgentManager _agentMgr;
	
	
	protected CheckOnAgentInvestigator() {
	}
	
	@Override
	public Status isAgentAlive(HostVO agent) {
		return null;
	}

	@Override
	public Boolean isVmAlive(VMInstanceVO vm, HostVO host) {
		CheckVirtualMachineCommand cmd = new CheckVirtualMachineCommand(vm.getInstanceName());
		try {
			CheckVirtualMachineAnswer answer = (CheckVirtualMachineAnswer)_agentMgr.send(vm.getHostId(), cmd);
			if (!answer.getResult()) {
				s_logger.debug("Unable to get vm state on " + vm.toString());
				return null;
			}

			s_logger.debug("Agent responded with state " + answer.getState().toString());
			return answer.getState() == State.Running;
		} catch (AgentUnavailableException e) {
			s_logger.debug("Unable to reach the agent for " + vm.toString() + ": " + e.getMessage());
			return null;
		} catch (OperationTimedoutException e) {
			s_logger.debug("Operation timed out for " + vm.toString() + ": " + e.getMessage());
			return null;
		}
	}
}