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
package org.apache.api.commands.netapp;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.netapp.NetappManager;
import org.apache.server.ManagementService;
import org.apache.server.api.response.netapp.CreateVolumePoolCmdResponse;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.ResourceAllocationException;
import org.apache.exception.ResourceUnavailableException;


@APICommand(name = "createPool", description="Create a pool", responseObject = CreateVolumePoolCmdResponse.class)
public class CreateVolumePoolCmd extends BaseCmd {
	public static final Logger s_logger = Logger.getLogger(CreateVolumePoolCmd.class.getName());
    private static final String s_name = "createpoolresponse";

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required = true, description="pool name.")
	private String poolName;
    @Parameter(name=ApiConstants.ALGORITHM, type=CommandType.STRING, required = true, description="algorithm.")
	private String algorithm;
    
    public String getPoolName() {
    	return poolName;
    }
    
    public String getAlgorithm() {
    	return algorithm;
    }
    @Inject NetappManager netappMgr;

	@Override
	public void execute() throws ResourceUnavailableException,
			InsufficientCapacityException, ServerApiException,
			ConcurrentOperationException, ResourceAllocationException {
    	
    	try {
    		CreateVolumePoolCmdResponse response = new CreateVolumePoolCmdResponse();
    		netappMgr.createPool(getPoolName(), getAlgorithm());
    		response.setResponseName(getCommandName());
    		this.setResponseObject(response);
    	} catch (InvalidParameterValueException e) {
    		throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.toString());
    	}
		
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
