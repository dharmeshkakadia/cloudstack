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
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.FenceAnswer;
import org.apache.agent.api.FenceCommand;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.OperationTimedoutException;
import org.apache.ha.FenceBuilder;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.log4j.Logger;
import org.apache.resource.ResourceManager;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.VMInstanceVO;

@Local(value=FenceBuilder.class)
public class XenServerFencer extends AdapterBase implements FenceBuilder {
    private static final Logger s_logger = Logger.getLogger(XenServerFencer.class);
    String _name;

    @Inject HostDao _hostDao;
    @Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;

    @Override
    public Boolean fenceOff(VMInstanceVO vm, HostVO host) {
        if (host.getHypervisorType() != HypervisorType.XenServer) {
            s_logger.debug("Don't know how to fence non XenServer hosts " + host.getHypervisorType());
            return null;
        }

        List<HostVO> hosts = _resourceMgr.listAllHostsInCluster(host.getClusterId());
        FenceCommand fence = new FenceCommand(vm, host);

        for (HostVO h : hosts) {
            if (h.getHypervisorType() == HypervisorType.XenServer) {
            	if( h.getStatus() != Status.Up ) {
            		continue;
            	}
            	if( h.getId() == host.getId() ) {
            		continue;
            	}
                FenceAnswer answer;
                try {
                    Answer ans = _agentMgr.send(h.getId(), fence);
                    if (!(ans instanceof FenceAnswer)) {
                        s_logger.debug("Answer is not fenceanswer.  Result = " + ans.getResult() + "; Details = " + ans.getDetails());
                        continue;
                    }
                    answer = (FenceAnswer) ans;
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
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Unable to fence off " + vm.toString() + " on " + host.toString());
        }

        return false;
    }

    public XenServerFencer() {
        super();
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name;
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

}