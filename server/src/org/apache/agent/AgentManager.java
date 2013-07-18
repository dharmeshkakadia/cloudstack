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
package org.apache.agent;

import org.apache.agent.Listener;
import org.apache.agent.StartupCommandProcessor;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.manager.AgentAttache;
import org.apache.agent.manager.Commands;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.ConnectionException;
import org.apache.exception.OperationTimedoutException;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.host.Status.Event;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.resource.ServerResource;
import org.apache.utils.component.Manager;

/**
 * AgentManager manages hosts. It directly coordinates between the DAOs and the connections it manages.
 */
public interface AgentManager extends Manager {
    public enum OnError {
        Continue, Stop
    }

    public enum TapAgentsAction {
        Add,
        Del,
        Contains,
    }

    /**
     * easy send method that returns null if there's any errors. It handles all exceptions.
     *
     * @param hostId
     *            host id
     * @param cmd
     *            command to send.
     * @return Answer if successful; null if not.
     */
    Answer easySend(Long hostId, Command cmd);

    /**
     * Synchronous sending a command to the agent.
     *
     * @param hostId
     *            id of the agent on host
     * @param cmd
     *            command
     * @return an Answer
     */

    Answer send(Long hostId, Command cmd) throws AgentUnavailableException, OperationTimedoutException;

    /**
     * Synchronous sending a list of commands to the agent.
     *
     * @param hostId
     *            id of the agent on host
     * @param cmds
     *            array of commands
     * @param isControl
     *            Commands sent contains control commands
     * @param stopOnError
     *            should the agent stop execution on the first error.
     * @return an array of Answer
     */
    Answer[] send(Long hostId, Commands cmds) throws AgentUnavailableException, OperationTimedoutException;

    Answer[] send(Long hostId, Commands cmds, int timeout) throws AgentUnavailableException, OperationTimedoutException;

    /**
     * Asynchronous sending of a command to the agent.
     *
     * @param hostId
     *            id of the agent on the host.
     * @param cmds
     *            Commands to send.
     * @param stopOnError
     *            should the agent stop execution on the first error.
     * @param listener
     *            the listener to process the answer.
     * @return sequence number.
     */
    long send(Long hostId, Commands cmds, Listener listener) throws AgentUnavailableException;

    /**
     * Register to listen for host events. These are mostly connection and disconnection events.
     *
     * @param listener
     * @param connections
     *            listen for connections
     * @param commands
     *            listen for connections
     * @param priority
     *            in listening for events.
     * @return id to unregister if needed.
     */
    int registerForHostEvents(Listener listener, boolean connections, boolean commands, boolean priority);


    /**
     * Register to listen for initial agent connections.
     * @param creator
     * @param priority in listening for events.
     * @return id to unregister if needed.
     */
    int registerForInitialConnects(StartupCommandProcessor creator,  boolean priority);

    /**
     * Unregister for listening to host events.
     *
     * @param id
     *            returned from registerForHostEvents
     */
    void unregisterForHostEvents(int id);

    public boolean executeUserRequest(long hostId, Event event) throws AgentUnavailableException;

    Answer sendTo(Long dcId, HypervisorType type, Command cmd);


    /* working as a lock while agent is being loaded */
    public boolean tapLoadingAgents(Long hostId, TapAgentsAction action);

    public AgentAttache handleDirectConnectAgent(HostVO host, StartupCommand[] cmds, ServerResource resource, boolean forRebalance) throws ConnectionException;

    public boolean agentStatusTransitTo(HostVO host, Status.Event e, long msId);

    public AgentAttache findAttache(long hostId);

    void disconnectWithoutInvestigation(long hostId, Status.Event event);

    public void pullAgentToMaintenance(long hostId);

    public void pullAgentOutMaintenance(long hostId);

	boolean reconnect(long hostId);
    Answer sendToSSVM(Long dcId, final Command cmd);

    void disconnectWithInvestigation(final long hostId, final Status.Event event);
}
