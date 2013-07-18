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
package org.apache.capacity;

import java.math.BigDecimal;
import java.util.List;

import org.apache.agent.Listener;
import org.apache.agent.api.AgentControlAnswer;
import org.apache.agent.api.AgentControlCommand;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupStorageCommand;
import org.apache.capacity.CapacityVO;
import org.apache.capacity.dao.CapacityDao;
import org.apache.capacity.dao.CapacityDaoImpl;
import org.apache.exception.ConnectionException;
import org.apache.host.Host;
import org.apache.host.Status;
import org.apache.log4j.Logger;
import org.apache.storage.Storage;
import org.apache.storage.StorageManager;
import org.apache.utils.db.SearchCriteria;


public class StorageCapacityListener implements Listener {
    
    CapacityDao _capacityDao;
    StorageManager _storageMgr;

    public StorageCapacityListener(CapacityDao capacityDao, StorageManager storageMgr) {
        this._capacityDao = capacityDao;
        this._storageMgr =  storageMgr;
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
    public AgentControlAnswer processControlCommand(long agentId,
            AgentControlCommand cmd) {
        
        return null;
    }


    @Override
    public void processConnect(Host server, StartupCommand startup, boolean forRebalance) throws ConnectionException {
        
        if (!(startup instanceof StartupStorageCommand)) {
            return;
        }

        StartupStorageCommand ssCmd = (StartupStorageCommand) startup;
        if (ssCmd.getResourceType() == Storage.StorageResourceType.STORAGE_HOST) {
            BigDecimal overProvFactor = _storageMgr.getStorageOverProvisioningFactor(server.getDataCenterId());
            CapacityVO capacity = new CapacityVO(server.getId(),
                    server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L,
                    (overProvFactor.multiply(new BigDecimal(server.getTotalSize()))).longValue(),
                    CapacityVO.CAPACITY_TYPE_STORAGE_ALLOCATED);
            _capacityDao.persist(capacity);
        }

    }


    @Override
    public boolean processDisconnect(long agentId, Status state) {
        return false;
    }


    @Override
    public boolean isRecurring() {
        return false;
    }

 
    @Override
    public int getTimeout() {
        return 0;
    }


    @Override
    public boolean processTimeout(long agentId, long seq) {
        return false;
    }

}
