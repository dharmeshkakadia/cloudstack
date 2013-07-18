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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.PingTestCommand;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.OperationTimedoutException;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.Host.Type;
import org.apache.host.dao.HostDao;
import org.apache.log4j.Logger;
import org.apache.resource.ResourceManager;
import org.apache.utils.component.AdapterBase;
import org.apache.utils.db.SearchCriteria2;
import org.apache.utils.db.SearchCriteriaService;
import org.apache.utils.db.SearchCriteria.Op;

public abstract class AbstractInvestigatorImpl extends AdapterBase implements Investigator {
    private static final Logger s_logger = Logger.getLogger(AbstractInvestigatorImpl.class);

    @Inject private HostDao _hostDao = null;
    @Inject private AgentManager _agentMgr = null;
    @Inject private ResourceManager _resourceMgr = null;


    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
    
    // Host.status is up and Host.type is routing
    protected List<Long> findHostByPod(long podId, Long excludeHostId) {
    	SearchCriteriaService<HostVO, HostVO> sc = SearchCriteria2.create(HostVO.class);
        sc.addAnd(sc.getEntity().getType(), Op.EQ, Type.Routing);
        sc.addAnd(sc.getEntity().getPodId(), Op.EQ, podId);
        sc.addAnd(sc.getEntity().getStatus(), Op.EQ, Status.Up);
        List<HostVO> hosts = sc.list();
        
        List<Long> hostIds = new ArrayList<Long>(hosts.size());
        for (HostVO h : hosts) {
        	hostIds.add(h.getId());
        }
        
        if (excludeHostId != null) {
            hostIds.remove(excludeHostId);
        }
        
        return hostIds;
    }

    protected Status testIpAddress(Long hostId, String testHostIp) {
        try {
            Answer pingTestAnswer = _agentMgr.send(hostId, new PingTestCommand(testHostIp));
            if(pingTestAnswer == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") returns null answer");
                }
            	return null;
            }
            
            if (pingTestAnswer.getResult()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") has been successfully pinged, returning that host is up");
                }
                // computing host is available, but could not reach agent, return false
                return Status.Up;
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") cannot be pinged, returning null ('I don't know')");
                }
                return null;
            }
        } catch (AgentUnavailableException e) {
            return null;
        } catch (OperationTimedoutException e) {
            return null;
        }
    }
}
