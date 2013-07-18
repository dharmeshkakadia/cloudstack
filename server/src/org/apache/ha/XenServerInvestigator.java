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

import java.util.List;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.CheckOnHostAnswer;
import org.apache.agent.api.CheckOnHostCommand;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.log4j.Logger;
import org.apache.resource.ResourceManager;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.VMInstanceVO;

@Local(value=Investigator.class)
public class XenServerInvestigator extends AdapterBase implements Investigator {
    private final static Logger s_logger = Logger.getLogger(XenServerInvestigator.class);
    @Inject HostDao _hostDao;
    @Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;
    
    protected XenServerInvestigator() {
    }
    
    @Override
    public Status isAgentAlive(HostVO agent) {
        if (agent.getHypervisorType() != HypervisorType.XenServer) {
            return null;
        }
        
        CheckOnHostCommand cmd = new CheckOnHostCommand(agent);
        List<HostVO> neighbors = _resourceMgr.listAllHostsInCluster(agent.getClusterId());
        for (HostVO neighbor : neighbors) {
            if (neighbor.getId() == agent.getId() || neighbor.getHypervisorType() != HypervisorType.XenServer) {
                continue;
            }
            Answer answer = _agentMgr.easySend(neighbor.getId(), cmd);
            if (answer != null && answer.getResult()) {
                CheckOnHostAnswer ans = (CheckOnHostAnswer)answer;
                if (!ans.isDetermined()) {
                    s_logger.debug("Host " + neighbor + " couldn't determine the status of " + agent);
                    continue;
                }
                return ans.isAlive() ? Status.Up : Status.Down;
            }
        }
        
        return null;
    }

    @Override
    public Boolean isVmAlive(VMInstanceVO vm, HostVO host) {
        Status status = isAgentAlive(host);
        if (status == null) {
            return null;
        }
        return status == Status.Up ? true : null;
    }
}
