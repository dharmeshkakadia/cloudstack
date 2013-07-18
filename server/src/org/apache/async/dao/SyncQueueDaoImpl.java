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

package org.apache.async.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.async.SyncQueueVO;
import org.apache.log4j.Logger;
import org.apache.utils.DateUtil;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value = { SyncQueueDao.class })
public class SyncQueueDaoImpl extends GenericDaoBase<SyncQueueVO, Long> implements SyncQueueDao {
    private static final Logger s_logger = Logger.getLogger(SyncQueueDaoImpl.class.getName());
    
    SearchBuilder<SyncQueueVO> TypeIdSearch = createSearchBuilder();
	
	@Override
	public void ensureQueue(String syncObjType, long syncObjId) {
		Date dt = DateUtil.currentGMTTime();
        String sql = "INSERT IGNORE INTO sync_queue(sync_objtype, sync_objid, created, last_updated)" +
                " values(?, ?, ?, ?)";
		
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, syncObjType);
            pstmt.setLong(2, syncObjId);
            pstmt.setString(3, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), dt));
            pstmt.setString(4, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), dt));
            pstmt.execute();
        } catch (SQLException e) {
        	s_logger.warn("Unable to create sync queue " + syncObjType + "-" + syncObjId + ":" + e.getMessage(), e);
        } catch (Throwable e) {
        	s_logger.warn("Unable to create sync queue " + syncObjType + "-" + syncObjId + ":" + e.getMessage(), e);
        }
	}
	
	@Override
	public SyncQueueVO find(String syncObjType, long syncObjId) {
    	SearchCriteria<SyncQueueVO> sc = TypeIdSearch.create();
    	sc.setParameters("syncObjType", syncObjType);
    	sc.setParameters("syncObjId", syncObjId);
        return findOneBy(sc);
	}

	protected SyncQueueDaoImpl() {
	    super();
	    TypeIdSearch = createSearchBuilder();
        TypeIdSearch.and("syncObjType", TypeIdSearch.entity().getSyncObjType(), SearchCriteria.Op.EQ);
        TypeIdSearch.and("syncObjId", TypeIdSearch.entity().getSyncObjId(), SearchCriteria.Op.EQ);
        TypeIdSearch.done();
	}
}