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

package org.apache.vm.snapshot.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.UpdateBuilder;
import org.apache.utils.db.SearchCriteria.Op;
import org.apache.vm.snapshot.VMSnapshot;
import org.apache.vm.snapshot.VMSnapshotVO;
import org.apache.vm.snapshot.VMSnapshot.Event;
import org.apache.vm.snapshot.VMSnapshot.State;
import org.springframework.stereotype.Component;
@Component
@Local(value = { VMSnapshotDao.class })
public class VMSnapshotDaoImpl extends GenericDaoBase<VMSnapshotVO, Long>
        implements VMSnapshotDao {
    private static final Logger s_logger = Logger.getLogger(VMSnapshotDaoImpl.class);
    private final SearchBuilder<VMSnapshotVO> SnapshotSearch;
    private final SearchBuilder<VMSnapshotVO> ExpungingSnapshotSearch;
    private final SearchBuilder<VMSnapshotVO> SnapshotStatusSearch;
    private final SearchBuilder<VMSnapshotVO> AllFieldsSearch;

    protected VMSnapshotDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("vm_id", AllFieldsSearch.entity().getVmId(), Op.EQ);
        AllFieldsSearch.and("deviceId", AllFieldsSearch.entity().getVmId(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("removed", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("parent", AllFieldsSearch.entity().getParent(), Op.EQ);
        AllFieldsSearch.and("current", AllFieldsSearch.entity().getCurrent(), Op.EQ);
        AllFieldsSearch.and("vm_snapshot_type", AllFieldsSearch.entity().getType(), Op.EQ);
        AllFieldsSearch.and("updatedCount", AllFieldsSearch.entity().getUpdatedCount(), Op.EQ);
        AllFieldsSearch.and("display_name", AllFieldsSearch.entity().getDisplayName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
        
        SnapshotSearch = createSearchBuilder();
        SnapshotSearch.and("vm_id", SnapshotSearch.entity().getVmId(),
                SearchCriteria.Op.EQ);
        SnapshotSearch.done();

        ExpungingSnapshotSearch = createSearchBuilder();
        ExpungingSnapshotSearch.and("state", ExpungingSnapshotSearch.entity()
                .getState(), SearchCriteria.Op.EQ);
        ExpungingSnapshotSearch.and("removed", ExpungingSnapshotSearch.entity()
                .getRemoved(), SearchCriteria.Op.NULL);
        ExpungingSnapshotSearch.done();

        SnapshotStatusSearch = createSearchBuilder();
        SnapshotStatusSearch.and("vm_id", SnapshotStatusSearch.entity()
                .getVmId(), SearchCriteria.Op.EQ);
        SnapshotStatusSearch.and("state", SnapshotStatusSearch.entity()
                .getState(), SearchCriteria.Op.IN);
        SnapshotStatusSearch.done();
    }

    @Override
    public List<VMSnapshotVO> findByVm(Long vmId) {
        SearchCriteria<VMSnapshotVO> sc = SnapshotSearch.create();
        sc.setParameters("vm_id", vmId);
        return listBy(sc, null);
    }

    @Override
    public List<VMSnapshotVO> listExpungingSnapshot() {
        SearchCriteria<VMSnapshotVO> sc = ExpungingSnapshotSearch.create();
        sc.setParameters("state", State.Expunging);
        return listBy(sc, null);
    }

    @Override
    public List<VMSnapshotVO> listByInstanceId(Long vmId, State... status) {
        SearchCriteria<VMSnapshotVO> sc = SnapshotStatusSearch.create();
        sc.setParameters("vm_id", vmId);
        sc.setParameters("state", (Object[]) status);
        return listBy(sc, null);
    }

	@Override
	public VMSnapshotVO findCurrentSnapshotByVmId(Long vmId) {
        SearchCriteria<VMSnapshotVO> sc = AllFieldsSearch.create();
        sc.setParameters("vm_id", vmId);
        sc.setParameters("current", 1);
        return findOneBy(sc);
	}

    @Override
    public List<VMSnapshotVO> listByParent(Long vmSnapshotId) {
        SearchCriteria<VMSnapshotVO> sc = AllFieldsSearch.create();
        sc.setParameters("parent", vmSnapshotId);
        sc.setParameters("state", State.Ready );
        return listBy(sc, null);
    }

    @Override
    public VMSnapshotVO findByName(Long vm_id, String name) {
        SearchCriteria<VMSnapshotVO> sc = AllFieldsSearch.create();
        sc.setParameters("vm_id", vm_id);
        sc.setParameters("display_name", name );
        return null;
    }

    @Override
    public boolean updateState(State currentState, Event event, State nextState, VMSnapshot vo, Object data) {
        
        Long oldUpdated = vo.getUpdatedCount();
        Date oldUpdatedTime = vo.getUpdated();
        
        SearchCriteria<VMSnapshotVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", vo.getUpdatedCount());
        
        vo.incrUpdatedCount();
        
        UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());
        
        int rows = update((VMSnapshotVO)vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            VMSnapshotVO dbVol = findByIdIncludingRemoved(vo.getId()); 
            if (dbVol != null) {
                StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=").append(dbVol.getId()).append("; state=").append(dbVol.getState()).append("; updatecount=").append(dbVol.getUpdatedCount()).append(";updatedTime=").append(dbVol.getUpdated());
                str.append(": New Data={id=").append(vo.getId()).append("; state=").append(nextState).append("; event=").append(event).append("; updatecount=").append(vo.getUpdatedCount()).append("; updatedTime=").append(vo.getUpdated());
                str.append(": stale Data={id=").append(vo.getId()).append("; state=").append(currentState).append("; event=").append(event).append("; updatecount=").append(oldUpdated).append("; updatedTime=").append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update VM snapshot: id=" + vo.getId() + ", as there is no such snapshot exists in the database anymore");
            }
        }
        return rows > 0;
    }

}