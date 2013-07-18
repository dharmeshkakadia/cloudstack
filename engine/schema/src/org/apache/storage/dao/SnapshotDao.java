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
package org.apache.storage.dao;

import java.util.List;

import org.apache.storage.DataStoreRole;
import org.apache.storage.Snapshot;
import org.apache.storage.SnapshotVO;
import org.apache.storage.Snapshot.Type;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GenericDao;
import org.apache.utils.fsm.StateDao;

public interface SnapshotDao extends GenericDao<SnapshotVO, Long>, StateDao<Snapshot.State, Snapshot.Event, SnapshotVO> {
    List<SnapshotVO> listByVolumeId(long volumeId);

    List<SnapshotVO> listByVolumeId(Filter filter, long volumeId);

    SnapshotVO findNextSnapshot(long parentSnapId);

    long getLastSnapshot(long volumeId, DataStoreRole role);

    List<SnapshotVO> listByVolumeIdType(long volumeId, Type type);

    List<SnapshotVO> listByVolumeIdIncludingRemoved(long volumeId);

    List<SnapshotVO> listByBackupUuid(long volumeId, String backupUuid);

    long updateSnapshotVersion(long volumeId, String from, String to);

    List<SnapshotVO> listByVolumeIdVersion(long volumeId, String version);

    Long getSecHostId(long volumeId);

    long updateSnapshotSecHost(long dcId, long secHostId);

    public Long countSnapshotsForAccount(long accountId);

    List<SnapshotVO> listByInstanceId(long instanceId, Snapshot.State... status);

    List<SnapshotVO> listByStatus(long volumeId, Snapshot.State... status);

    List<SnapshotVO> listAllByStatus(Snapshot.State... status);

    /**
     * Gets the Total Secondary Storage space (in bytes) used by snapshots
     * allocated for an account
     * 
     * @param account
     * @return total Secondary Storage space allocated
     */
    long secondaryStorageUsedForAccount(long accountId);

}
