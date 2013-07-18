/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.storage.datastore.lifecycle;

import org.apache.agent.api.StoragePoolInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.*;
import org.apache.cloudstack.storage.command.AttachPrimaryDataStoreCmd;
import org.apache.cloudstack.storage.command.CreatePrimaryDataStoreCmd;
import org.apache.cloudstack.storage.datastore.PrimaryDataStoreProviderManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.volume.datastore.PrimaryDataStoreHelper;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.storage.StoragePoolStatus;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;

public class SamplePrimaryDataStoreLifeCycleImpl implements PrimaryDataStoreLifeCycle {
    @Inject
    EndPointSelector selector;
    @Inject
    PrimaryDataStoreDao dataStoreDao;
    @Inject
    HostDao hostDao;
    @Inject
    PrimaryDataStoreHelper primaryStoreHelper;
    @Inject
    PrimaryDataStoreProviderManager providerMgr;

    public SamplePrimaryDataStoreLifeCycleImpl() {
    }

    @Override
    public DataStore initialize(Map<String, Object> dsInfos) {

        DataStore store = primaryStoreHelper.createPrimaryDataStore(null);
        return providerMgr.getPrimaryDataStore(store.getId());
    }

    protected void attachCluster(DataStore store) {
        // send down AttachPrimaryDataStoreCmd command to all the hosts in the
        // cluster
        List<EndPoint> endPoints = selector.selectAll(store);
        CreatePrimaryDataStoreCmd createCmd = new CreatePrimaryDataStoreCmd(store.getUri());
        EndPoint ep = endPoints.get(0);
        HostVO host = hostDao.findById(ep.getId());
        if (host.getHypervisorType() == HypervisorType.XenServer) {
            ep.sendMessage(createCmd);
        }

        endPoints.get(0).sendMessage(createCmd);
        AttachPrimaryDataStoreCmd cmd = new AttachPrimaryDataStoreCmd(store.getUri());
        for (EndPoint endp : endPoints) {
            endp.sendMessage(cmd);
        }
    }

    @Override
    public boolean attachCluster(DataStore dataStore, ClusterScope scope) {
        StoragePoolVO dataStoreVO = dataStoreDao.findById(dataStore.getId());
        dataStoreVO.setDataCenterId(scope.getZoneId());
        dataStoreVO.setPodId(scope.getPodId());
        dataStoreVO.setClusterId(scope.getScopeId());
        dataStoreVO.setStatus(StoragePoolStatus.Attaching);
        dataStoreVO.setScope(scope.getScopeType());
        dataStoreDao.update(dataStoreVO.getId(), dataStoreVO);

        attachCluster(dataStore);

        dataStoreVO = dataStoreDao.findById(dataStore.getId());
        dataStoreVO.setStatus(StoragePoolStatus.Up);
        dataStoreDao.update(dataStoreVO.getId(), dataStoreVO);

        return true;
    }

    @Override
    public boolean attachZone(DataStore dataStore, ZoneScope scope, HypervisorType hypervisorType) {
        return false;
    }

    @Override
    public boolean attachHost(DataStore store, HostScope scope, StoragePoolInfo existingInfo) {
        return false;
    }

    @Override
    public boolean maintain(DataStore store) {
        return false;
    }

    @Override
    public boolean cancelMaintain(DataStore store) {
        return false;
    }

    @Override
    public boolean deleteDataStore(DataStore store) {
        return false;
    }

}
