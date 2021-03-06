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
package com.cloud.async;

import java.util.List;

import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.command.user.job.QueryAsyncJobResultCmd;
import com.cloud.utils.component.Manager;

public interface AsyncJobManager extends Manager {
    
	public AsyncJobExecutorContext getExecutorContext();
	
	   public AsyncJobVO getAsyncJob(long jobId);
	    public AsyncJobVO findInstancePendingAsyncJob(String instanceType, long instanceId);
	
	public List<? extends AsyncJob> findInstancePendingAsyncJobs(ApiCommandJobType instanceType, Long accountId);
	
	public long submitAsyncJob(AsyncJobVO job);
	public long submitAsyncJob(AsyncJobVO job, boolean scheduleJobExecutionInContext);
	public AsyncJobResult queryAsyncJobResult(long jobId);    
	
    public void completeAsyncJob(long jobId, int jobStatus, int resultCode, Object resultObject);
    public void updateAsyncJobStatus(long jobId, int processStatus, Object resultObject);
    public void updateAsyncJobAttachment(long jobId, String instanceType, Long instanceId);
   
    public void releaseSyncSource(AsyncJobExecutor executor);
    
    public void syncAsyncJobExecution(AsyncJob job, String syncObjType, long syncObjId, long queueSizeLimit);
    
    /**
     * Queries for the status or final result of an async job.
     * @param cmd the command that specifies the job id
     * @return an async-call result object
     */
    public AsyncJob queryAsyncJobResult(QueryAsyncJobResultCmd cmd);
}
