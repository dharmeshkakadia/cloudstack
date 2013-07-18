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
package org.apache.async;

import org.apache.agent.AgentManager;
import org.apache.async.dao.AsyncJobDao;
import org.apache.event.dao.EventDao;
import org.apache.network.NetworkModel;
import org.apache.network.dao.IPAddressDao;
import org.apache.server.ManagementServer;
import org.apache.storage.StorageManager;
import org.apache.storage.dao.VolumeDao;
import org.apache.storage.snapshot.SnapshotManager;
import org.apache.user.AccountManager;
import org.apache.user.dao.AccountDao;
import org.apache.user.dao.UserDao;
import org.apache.utils.component.Manager;
import org.apache.vm.UserVmManager;
import org.apache.vm.VirtualMachineManager;
import org.apache.vm.dao.DomainRouterDao;
import org.apache.vm.dao.UserVmDao;

public interface AsyncJobExecutorContext extends Manager {
	public ManagementServer getManagementServer();
	public AgentManager getAgentMgr();
	public NetworkModel getNetworkMgr();
	public UserVmManager getVmMgr();
	public SnapshotManager getSnapshotMgr();
	public AccountManager getAccountMgr();
	public StorageManager getStorageMgr();
	public EventDao getEventDao();
	public UserVmDao getVmDao();
	public AccountDao getAccountDao();
	public VolumeDao getVolumeDao();
    public DomainRouterDao getRouterDao();
    public IPAddressDao getIpAddressDao();
    public AsyncJobDao getJobDao();
    public UserDao getUserDao();
    public VirtualMachineManager getItMgr();
}
