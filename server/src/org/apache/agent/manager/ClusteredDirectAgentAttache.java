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

import org.apache.agent.AgentManager;
import org.apache.agent.transport.Request;
import org.apache.agent.transport.Response;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.UnsupportedVersionException;
import org.apache.resource.ServerResource;
import org.apache.utils.exception.CloudRuntimeException;

public class ClusteredDirectAgentAttache extends DirectAgentAttache implements Routable {
    private final ClusteredAgentManagerImpl _mgr;
    private final long _nodeId;

    public ClusteredDirectAgentAttache(AgentManagerImpl agentMgr, long id, long mgmtId, ServerResource resource, boolean maintenance, ClusteredAgentManagerImpl mgr) {
        super(agentMgr, id, resource, maintenance, mgr);
        _mgr = mgr;
        _nodeId = mgmtId;
    }

    @Override
    public void routeToAgent(byte[] data) throws AgentUnavailableException {
        Request req;
        try {
            req = Request.parse(data);
        } catch (ClassNotFoundException e) {
            throw new CloudRuntimeException("Unable to rout to an agent ", e);
        } catch (UnsupportedVersionException e) {
            throw new CloudRuntimeException("Unable to rout to an agent ", e);
        }

        if (req instanceof Response) {
            super.process(((Response) req).getAnswers());
        } else {
            super.send(req);
        }
    }

    @Override
    public boolean processAnswers(long seq, Response response) {
        long mgmtId = response.getManagementServerId();
        if (mgmtId != -1 && mgmtId != _nodeId) {
            _mgr.routeToPeer(Long.toString(mgmtId), response.getBytes());
            if (response.executeInSequence()) {
                sendNext(response.getSequence());
            }
            return true;
        } else {
            return super.processAnswers(seq, response);
        }
    }
    
}
