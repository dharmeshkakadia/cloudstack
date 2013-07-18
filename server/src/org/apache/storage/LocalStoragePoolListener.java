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
package org.apache.storage;

import javax.inject.Inject;

import org.apache.agent.Listener;
import org.apache.agent.api.AgentControlAnswer;
import org.apache.agent.api.AgentControlCommand;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupStorageCommand;
import org.apache.agent.api.StoragePoolInfo;
import org.apache.capacity.dao.CapacityDao;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.dc.dao.DataCenterDao;
import org.apache.exception.ConnectionException;
import org.apache.host.Host;
import org.apache.host.Status;
import org.apache.log4j.Logger;
import org.apache.storage.Storage;
import org.apache.storage.dao.StoragePoolHostDao;
import org.apache.utils.db.DB;

public class LocalStoragePoolListener implements Listener {
    private final static Logger s_logger = Logger.getLogger(LocalStoragePoolListener.class);
    @Inject PrimaryDataStoreDao _storagePoolDao;
    @Inject StoragePoolHostDao _storagePoolHostDao;
    @Inject CapacityDao _capacityDao;
    @Inject StorageManager _storageMgr;
    @Inject DataCenterDao _dcDao;

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
    @DB
    public void processConnect(Host host, StartupCommand cmd, boolean forRebalance) throws ConnectionException {
        if (!(cmd instanceof StartupStorageCommand)) {
            return;
        }
        
        StartupStorageCommand ssCmd = (StartupStorageCommand)cmd;
        
        if (ssCmd.getResourceType() != Storage.StorageResourceType.STORAGE_POOL) {
            return;
        }
        
        StoragePoolInfo pInfo = ssCmd.getPoolInfo();
        if (pInfo == null) {
            return;
        }

        this._storageMgr.createLocalStorage(host, pInfo);
    }
   

    @Override
    public AgentControlAnswer processControlCommand(long agentId, AgentControlCommand cmd) {
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
