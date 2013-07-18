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
import org.apache.agent.api.Command;
import org.apache.agent.transport.Request;
import org.apache.exception.AgentUnavailableException;
import org.apache.host.Status;


public class DummyAttache extends AgentAttache {

	public DummyAttache(AgentManagerImpl agentMgr, long id, boolean maintenance) {
		super(agentMgr, id, maintenance);
	}


	@Override
	public void disconnect(Status state) {

	}

	
	@Override
	protected boolean isClosed() {
		return false;
	}

	
	@Override
	public void send(Request req) throws AgentUnavailableException {

	}


    @Override
    public void updatePassword(Command newPassword) {
        throw new IllegalStateException("Should not have come here ");
    }

}