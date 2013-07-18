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
package org.apache.network.element;


import java.util.List;

import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InsufficientNetworkCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.network.vpc.PrivateGateway;
import org.apache.network.vpc.StaticRouteProfile;
import org.apache.network.vpc.Vpc;
import org.apache.vm.ReservationContext;

public interface VpcProvider extends NetworkElement{
    /**
     * Start vpc element as specified
     * @param vpc fully specified vpc configuration.
     * @throws InsufficientNetworkCapacityException TODO
     */
    boolean implementVpc(Vpc vpc, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * @param vpc
     * @param context TODO
     * @return
     * @throws ConcurrentOperationException
     * @throws ResourceUnavailableException
     */
    boolean shutdownVpc(Vpc vpc, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException;

    boolean createPrivateGateway(PrivateGateway gateway) throws ConcurrentOperationException, ResourceUnavailableException;

    boolean deletePrivateGateway(PrivateGateway privateGateway) throws ConcurrentOperationException, ResourceUnavailableException;

    boolean applyStaticRoutes(Vpc vpc, List<StaticRouteProfile> routes) throws ResourceUnavailableException;

    boolean applyACLItemsToPrivateGw(PrivateGateway gateway) throws ResourceUnavailableException;
}
