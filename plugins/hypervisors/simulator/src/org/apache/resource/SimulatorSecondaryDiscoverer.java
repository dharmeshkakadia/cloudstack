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
package org.apache.resource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.Listener;
import org.apache.agent.api.AgentControlAnswer;
import org.apache.agent.api.AgentControlCommand;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupSecondaryStorageCommand;
import org.apache.agent.manager.MockStorageManager;
import org.apache.cloudstack.storage.resource.SecondaryStorageDiscoverer;
import org.apache.exception.ConnectionException;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.Status;
import org.apache.log4j.Logger;
import org.apache.resource.Discoverer;
import org.apache.resource.ResourceManager;
import org.apache.resource.ResourceStateAdapter;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.storage.SnapshotVO;
import org.apache.storage.dao.SnapshotDao;
import org.apache.utils.exception.CloudRuntimeException;


@Local(value=Discoverer.class)
public class SimulatorSecondaryDiscoverer extends SecondaryStorageDiscoverer implements ResourceStateAdapter, Listener {
    private static final Logger s_logger = Logger.getLogger(SimulatorSecondaryDiscoverer.class);
    @Inject MockStorageManager _mockStorageMgr = null;
    @Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;
    @Inject SnapshotDao _snapshotDao;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _agentMgr.registerForHostEvents(this, true, false, false);
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        return super.configure(name, params);
    }

    @Override
    public Map<? extends ServerResource, Map<String, String>> find(long dcId, Long podId, Long clusterId, URI uri, String username, String password, List<String> hostTags) {
        if (!uri.getScheme().equalsIgnoreCase("nfs") && !uri.getScheme().equalsIgnoreCase("file")
                && !uri.getScheme().equalsIgnoreCase("iso") && !uri.getScheme().equalsIgnoreCase("dummy")) {
            s_logger.debug("It's not NFS or file or ISO, so not a secondary storage server: " + uri.toString());
            return null;
        }

        if (uri.getScheme().equalsIgnoreCase("nfs") || uri.getScheme().equalsIgnoreCase("iso")) {
            return createNfsSecondaryStorageResource(dcId, podId, uri);
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            return createLocalSecondaryStorageResource(dcId, podId, uri);
        } else if (uri.getScheme().equalsIgnoreCase("dummy")) {
            return createDummySecondaryStorageResource(dcId, podId, uri);
        } else {
            return null;
        }
    }


    @Override
    public void postDiscovery(List<HostVO> hosts, long msId) {
        super.postDiscovery(hosts, msId);
        for (HostVO host: hosts) {
            if(s_logger.isDebugEnabled()) {
                s_logger.debug("Preinstalling simulator templates");
            }
            _mockStorageMgr.preinstallTemplates(host.getStorageUrl(), host.getDataCenterId());
        }
    }

    @Override
    public HostVO createHostVOForConnectedAgent(HostVO host,
            StartupCommand[] cmd) {
        return null;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host,
            StartupCommand[] startup, ServerResource resource,
            Map<String, String> details, List<String> hostTags) {
        //for detecting SSVM dispatch
        StartupCommand firstCmd = startup[0];
        if (!(firstCmd instanceof StartupSecondaryStorageCommand)) {
            return null;
        }

        host.setType(org.apache.host.Host.Type.SecondaryStorageVM);
        return host;
    }

    @Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced,
            boolean isForceDeleteStorage) throws UnableDeleteHostException {
        // no need to handle, since secondary storage is no longer a host anymore.
        return null;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        _resourceMgr.unregisterResourceStateAdapter(this.getClass().getSimpleName());
        return true;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public boolean processAnswers(long agentId, long seq, Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(long agentId, long seq, Command[] commands) {
        return false;
    }

    @Override
    public void processConnect(Host host, StartupCommand cmd,
            boolean forRebalance) throws ConnectionException {

    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId,
            AgentControlCommand cmd) {
        return null;
    }

    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return false;
    }

    @Override
    public boolean processTimeout(long agentId, long seq) {
        return false;
    }
}
