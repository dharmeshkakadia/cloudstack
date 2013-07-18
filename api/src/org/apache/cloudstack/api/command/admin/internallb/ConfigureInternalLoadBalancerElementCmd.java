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

package org.apache.cloudstack.api.command.admin.internallb;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.network.element.InternalLoadBalancerElementService;
import org.apache.event.EventTypes;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.log4j.Logger;
import org.apache.network.VirtualRouterProvider;
import org.apache.user.Account;
import org.apache.user.UserContext;

import javax.inject.Inject;

import java.util.List;

@APICommand(name = "configureInternalLoadBalancerElement", responseObject=InternalLoadBalancerElementResponse.class,
            description="Configures an Internal Load Balancer element.", since="4.2.0")
public class ConfigureInternalLoadBalancerElementCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(ConfigureInternalLoadBalancerElementCmd.class.getName());
    private static final String s_name = "configureinternalloadbalancerelementresponse";

    @Inject
    private List<InternalLoadBalancerElementService> _service;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ID, type=CommandType.UUID, entityType = InternalLoadBalancerElementResponse.class,
            required=true, description="the ID of the internal lb provider")
    private Long id;

    @Parameter(name=ApiConstants.ENABLED, type=CommandType.BOOLEAN, required=true, description="Enables/Disables the Internal Load Balancer element")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////


    public Long getId() {
        return id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ELEMENT_CONFIGURE;
    }

    @Override
    public String getEventDescription() {
        return  "configuring internal load balancer element: " + id;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException{
        UserContext.current().setEventDetails("Internal load balancer element: " + id);
        VirtualRouterProvider result = _service.get(0).configureInternalLoadBalancerElement(getId(), getEnabled());
        if (result != null){
            InternalLoadBalancerElementResponse routerResponse = _responseGenerator.createInternalLbElementResponse(result);
            routerResponse.setResponseName(getCommandName());
            this.setResponseObject(routerResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to configure the internal load balancer element");
        }
    }
}
