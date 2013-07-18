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
package org.apache.storage.secondary;

import org.apache.agent.AgentManager;
import org.apache.agent.Listener;
import org.apache.agent.api.AgentControlAnswer;
import org.apache.agent.api.AgentControlCommand;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupSecondaryStorageCommand;
import org.apache.agent.api.StartupStorageCommand;
import org.apache.host.Host;
import org.apache.host.Status;
import org.apache.log4j.Logger;
import org.apache.storage.Storage;

public class SecondaryStorageListener implements Listener {
    private final static Logger s_logger = Logger.getLogger(SecondaryStorageListener.class);
    
    SecondaryStorageVmManager _ssVmMgr = null;
    AgentManager _agentMgr = null;
    public SecondaryStorageListener(SecondaryStorageVmManager ssVmMgr, AgentManager agentMgr) {
        _ssVmMgr = ssVmMgr;
        _agentMgr = agentMgr;
    }
    
    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
    	boolean processed = false;
    	if(answers != null) {
    		for(int i = 0; i < answers.length; i++) {
    		}
    	}
    	
        return processed;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }
    
    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
    	return null;
    }

    @Override
    public void processConnect(Host agent, StartupCommand cmd, boolean forRebalance) {
        if ((cmd instanceof StartupStorageCommand) ) {
            StartupStorageCommand scmd = (StartupStorageCommand)cmd;
            if (scmd.getResourceType() ==  Storage.StorageResourceType.SECONDARY_STORAGE ) {
                _ssVmMgr.generateSetupCommand(agent.getId());
                return;
            }
        } else if (cmd instanceof StartupSecondaryStorageCommand) {
            if(s_logger.isInfoEnabled()) {
                s_logger.info("Received a host startup notification " + cmd);
            }
            _ssVmMgr.onAgentConnect(agent.getDataCenterId(), cmd);
            _ssVmMgr.generateSetupCommand(agent.getId());
            _ssVmMgr.generateFirewallConfiguration(agent.getId());
            _ssVmMgr.generateVMSetupCommand(agent.getId());
            return;
        } 
        return;
    }
    
    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return true;
    }
    
    @Override
    public boolean processTimeout(long agentId, long seq) {
    	return true;
    }
    
    @Override
    public int getTimeout() {
    	return -1;
    }
}
