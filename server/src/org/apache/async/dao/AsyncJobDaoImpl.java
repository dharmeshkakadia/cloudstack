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
import java.util.List;

import javax.ejb.Local;

import org.apache.async.AsyncJob;
import org.apache.async.AsyncJobResult;
import org.apache.async.AsyncJobVO;
import org.apache.log4j.Logger;
import org.apache.utils.db.DB;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value = { AsyncJobDao.class })
public class AsyncJobDaoImpl extends GenericDaoBase<AsyncJobVO, Long> implements AsyncJobDao {
    private static final Logger s_logger = Logger.getLogger(AsyncJobDaoImpl.class.getName());
	
	private final SearchBuilder<AsyncJobVO> pendingAsyncJobSearch;	
	private final SearchBuilder<AsyncJobVO> pendingAsyncJobsSearch;	
	private final SearchBuilder<AsyncJobVO> expiringUnfinishedAsyncJobSearch;
	private final SearchBuilder<AsyncJobVO> expiringCompletedAsyncJobSearch;

	
	public AsyncJobDaoImpl() {
		pendingAsyncJobSearch = createSearchBuilder();
		pendingAsyncJobSearch.and("instanceType", pendingAsyncJobSearch.entity().getInstanceType(), 
			SearchCriteria.Op.EQ);
		pendingAsyncJobSearch.and("instanceId", pendingAsyncJobSearch.entity().getInstanceId(), 
			SearchCriteria.Op.EQ);
		pendingAsyncJobSearch.and("status", pendingAsyncJobSearch.entity().getStatus(), 
				SearchCriteria.Op.EQ);
		pendingAsyncJobSearch.done();
		
		pendingAsyncJobsSearch = createSearchBuilder();
		pendingAsyncJobsSearch.and("instanceType", pendingAsyncJobsSearch.entity().getInstanceType(), 
			SearchCriteria.Op.EQ);
		pendingAsyncJobsSearch.and("accountId", pendingAsyncJobsSearch.entity().getAccountId(), 
			SearchCriteria.Op.EQ);
		pendingAsyncJobsSearch.and("status", pendingAsyncJobsSearch.entity().getStatus(), 
				SearchCriteria.Op.EQ);
		pendingAsyncJobsSearch.done();
		
		expiringUnfinishedAsyncJobSearch = createSearchBuilder();
		expiringUnfinishedAsyncJobSearch.and("created", expiringUnfinishedAsyncJobSearch.entity().getCreated(),
			SearchCriteria.Op.LTEQ);
		expiringUnfinishedAsyncJobSearch.and("completeMsId", expiringUnfinishedAsyncJobSearch.entity().getCompleteMsid(), SearchCriteria.Op.NULL);
		expiringUnfinishedAsyncJobSearch.and("jobStatus", expiringUnfinishedAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.EQ);
		expiringUnfinishedAsyncJobSearch.done();
		
		expiringCompletedAsyncJobSearch = createSearchBuilder();
		expiringCompletedAsyncJobSearch.and("created", expiringCompletedAsyncJobSearch.entity().getCreated(),
			SearchCriteria.Op.LTEQ);
		expiringCompletedAsyncJobSearch.and("completeMsId", expiringCompletedAsyncJobSearch.entity().getCompleteMsid(), SearchCriteria.Op.NNULL);
		expiringCompletedAsyncJobSearch.and("jobStatus", expiringCompletedAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.NEQ);
		expiringCompletedAsyncJobSearch.done();
	}
	
	public AsyncJobVO findInstancePendingAsyncJob(String instanceType, long instanceId) {
        SearchCriteria<AsyncJobVO> sc = pendingAsyncJobSearch.create();
        sc.setParameters("instanceType", instanceType);
        sc.setParameters("instanceId", instanceId);
        sc.setParameters("status", AsyncJobResult.STATUS_IN_PROGRESS);
        
        List<AsyncJobVO> l = listIncludingRemovedBy(sc);
        if(l != null && l.size() > 0) {
        	if(l.size() > 1) {
        		s_logger.warn("Instance " + instanceType + "-" + instanceId + " has multiple pending async-job");
        	}
        	
        	return l.get(0);
        }
        return null;
	}
	
	public List<AsyncJobVO> findInstancePendingAsyncJobs(AsyncJob.Type instanceType, Long accountId) {
		SearchCriteria<AsyncJobVO> sc = pendingAsyncJobsSearch.create();
        sc.setParameters("instanceType", instanceType);
        
        if (accountId != null) {
            sc.setParameters("accountId", accountId);
        }
        sc.setParameters("status", AsyncJobResult.STATUS_IN_PROGRESS);
        
        return listBy(sc);
	}
	
	@Override
	public List<AsyncJobVO> getExpiredUnfinishedJobs(Date cutTime, int limit) {
		SearchCriteria<AsyncJobVO> sc = expiringUnfinishedAsyncJobSearch.create();
		sc.setParameters("created", cutTime);
		sc.setParameters("jobStatus", 0);
		Filter filter = new Filter(AsyncJobVO.class, "created", true, 0L, (long)limit);
		return listIncludingRemovedBy(sc, filter);
	}
	
	@Override
	public List<AsyncJobVO> getExpiredCompletedJobs(Date cutTime, int limit) {
		SearchCriteria<AsyncJobVO> sc = expiringCompletedAsyncJobSearch.create();
		sc.setParameters("created", cutTime);
		sc.setParameters("jobStatus", 0);
		Filter filter = new Filter(AsyncJobVO.class, "created", true, 0L, (long)limit);
		return listIncludingRemovedBy(sc, filter);
	}

	@DB
	public void resetJobProcess(long msid, int jobResultCode, String jobResultMessage) {
		String sql = "UPDATE async_job SET job_status=" + AsyncJobResult.STATUS_FAILED + ", job_result_code=" + jobResultCode 
			+ ", job_result='" + jobResultMessage + "' where job_status=0 AND (job_complete_msid=? OR (job_complete_msid IS NULL AND job_init_msid=?))";
		
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, msid);
            pstmt.setLong(2, msid);
            pstmt.execute();
        } catch (SQLException e) {
        	s_logger.warn("Unable to reset job status for management server " + msid, e);
        } catch (Throwable e) {
        	s_logger.warn("Unable to reset job status for management server " + msid, e);
        }
	}
}