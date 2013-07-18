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

import org.apache.agent.api.Answer;
import org.apache.agent.api.AttachIsoCommand;
import org.apache.agent.api.AttachVolumeAnswer;
import org.apache.agent.api.AttachVolumeCommand;
import org.apache.agent.api.BackupSnapshotCommand;
import org.apache.agent.api.ComputeChecksumCommand;
import org.apache.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import org.apache.agent.api.CreatePrivateTemplateFromVolumeCommand;
import org.apache.agent.api.CreateStoragePoolCommand;
import org.apache.agent.api.CreateVolumeFromSnapshotCommand;
import org.apache.agent.api.DeleteStoragePoolCommand;
import org.apache.agent.api.GetStorageStatsAnswer;
import org.apache.agent.api.GetStorageStatsCommand;
import org.apache.agent.api.ManageSnapshotCommand;
import org.apache.agent.api.ModifyStoragePoolCommand;
import org.apache.agent.api.SecStorageSetupCommand;
import org.apache.agent.api.SecStorageVMSetupCommand;
import org.apache.agent.api.StoragePoolInfo;
import org.apache.agent.api.storage.CopyVolumeAnswer;
import org.apache.agent.api.storage.CopyVolumeCommand;
import org.apache.agent.api.storage.CreateAnswer;
import org.apache.agent.api.storage.CreateCommand;
import org.apache.agent.api.storage.DestroyCommand;
import org.apache.agent.api.storage.ListTemplateCommand;
import org.apache.agent.api.storage.ListVolumeCommand;
import org.apache.agent.api.storage.PrimaryStorageDownloadAnswer;
import org.apache.agent.api.storage.PrimaryStorageDownloadCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.command.DownloadProgressCommand;
import org.apache.utils.component.Manager;

public interface MockStorageManager extends Manager {
    public static final long DEFAULT_HOST_STORAGE_SIZE = 1 * 1024 * 1024 * 1024 * 1024L; //1T
    public static final long DEFAULT_TEMPLATE_SIZE = 1 * 1000 * 1000 * 1000L; //1G

	public PrimaryStorageDownloadAnswer primaryStorageDownload(PrimaryStorageDownloadCommand cmd);

	public CreateAnswer createVolume(CreateCommand cmd);
	public AttachVolumeAnswer AttachVolume(AttachVolumeCommand cmd);
	public Answer AttachIso(AttachIsoCommand cmd);

	public Answer DeleteStoragePool(DeleteStoragePoolCommand cmd);
	public Answer ModifyStoragePool(ModifyStoragePoolCommand cmd);
	public Answer CreateStoragePool(CreateStoragePoolCommand cmd);

	public Answer SecStorageSetup(SecStorageSetupCommand cmd);
	public Answer ListTemplates(ListTemplateCommand cmd);
    public Answer ListVolumes(ListVolumeCommand cmd);
	public Answer Destroy(DestroyCommand cmd);
	public Answer Download(DownloadCommand cmd);
	public Answer DownloadProcess(DownloadProgressCommand cmd);
	public GetStorageStatsAnswer GetStorageStats(GetStorageStatsCommand cmd);
	public Answer ManageSnapshot(ManageSnapshotCommand cmd);
	public Answer BackupSnapshot(BackupSnapshotCommand cmd, SimulatorInfo info);
	//public Answer DeleteSnapshotBackup(DeleteSnapshotBackupCommand cmd);
	public Answer CreateVolumeFromSnapshot(CreateVolumeFromSnapshotCommand cmd);
	//public Answer DeleteTemplate(DeleteTemplateCommand cmd);
	public Answer Delete(DeleteCommand cmd);
	public Answer SecStorageVMSetup(SecStorageVMSetupCommand cmd);

	public void preinstallTemplates(String url, long zoneId);

    StoragePoolInfo getLocalStorage(String hostGuid);

    public Answer CreatePrivateTemplateFromSnapshot(CreatePrivateTemplateFromSnapshotCommand cmd);

    public Answer ComputeChecksum(ComputeChecksumCommand cmd);

    public Answer CreatePrivateTemplateFromVolume(CreatePrivateTemplateFromVolumeCommand cmd);

    StoragePoolInfo getLocalStorage(String hostGuid, Long storageSize);

	CopyVolumeAnswer CopyVolume(CopyVolumeCommand cmd);
}
