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
package org.apache.storage.listener;

import java.util.List;

import javax.inject.Inject;

import org.apache.agent.Listener;
import org.apache.agent.api.AgentControlAnswer;
import org.apache.agent.api.AgentControlCommand;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupRoutingCommand;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.exception.ConnectionException;
import org.apache.host.Host;
import org.apache.host.Status;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.log4j.Logger;
import org.apache.storage.OCFS2Manager;
import org.apache.storage.ScopeType;
import org.apache.storage.StorageManagerImpl;
import org.apache.storage.StoragePoolStatus;
import org.apache.storage.Storage.StoragePoolType;


public class StoragePoolMonitor implements Listener {
    private static final Logger s_logger = Logger.getLogger(StoragePoolMonitor.class);
    private final StorageManagerImpl _storageManager;
    private final PrimaryDataStoreDao _poolDao;
    @Inject OCFS2Manager _ocfs2Mgr;

    public StoragePoolMonitor(StorageManagerImpl mgr, PrimaryDataStoreDao poolDao) {
        this._storageManager = mgr;
        this._poolDao = poolDao;

    }


    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public synchronized boolean processAnswers(long agentId, long seq, Answer[] resp) {
        return true;
    }

    @Override
    public synchronized boolean processDisconnect(long agentId, Status state) {
        return true;
    }

    @Override
    public void processConnect(Host host, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
        if (cmd instanceof StartupRoutingCommand) {
            StartupRoutingCommand scCmd = (StartupRoutingCommand)cmd;
            if (scCmd.getHypervisorType() == HypervisorType.XenServer || scCmd.getHypervisorType() ==  HypervisorType.KVM ||
                    scCmd.getHypervisorType() == HypervisorType.VMware || scCmd.getHypervisorType() ==  HypervisorType.Simulator || scCmd.getHypervisorType() == HypervisorType.Ovm) {
                List<StoragePoolVO> pools = _poolDao.listBy(host.getDataCenterId(), host.getPodId(), host.getClusterId(), ScopeType.CLUSTER);
                List<StoragePoolVO> zoneStoragePoolsByTags = _poolDao.findZoneWideStoragePoolsByTags(host.getDataCenterId(), null);
                List<StoragePoolVO> zoneStoragePoolsByHypervisor = _poolDao.findZoneWideStoragePoolsByHypervisor(host.getDataCenterId(), scCmd.getHypervisorType());
                zoneStoragePoolsByTags.retainAll(zoneStoragePoolsByHypervisor);
                pools.addAll(zoneStoragePoolsByTags);
                for (StoragePoolVO pool : pools) {
                    if (pool.getStatus() != StoragePoolStatus.Up) {
                        continue;
                    }
                    if (!pool.isShared()) {
                        continue;
                    }

                    if (pool.getPoolType() == StoragePoolType.OCFS2 && !_ocfs2Mgr.prepareNodes(pool.getClusterId())) {
                        throw new ConnectionException(true, "Unable to prepare OCFS2 nodes for pool " + pool.getId());
                    }

                    Long hostId = host.getId();
                    s_logger.debug("Host " + hostId + " connected, sending down storage pool information ...");
                    try {
                        _storageManager.connectHostToSharedPool(hostId, pool.getId());
                        _storageManager.createCapacityEntry(pool.getId());
                    } catch (Exception e) {
                        s_logger.warn("Unable to connect host " + hostId + " to pool " + pool + " due to " + e.toString(), e);
                    }
                }
            }
        }
    }


    @Override
    public boolean processCommands(long agentId, long seq, Command[] req) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
        return null;
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
