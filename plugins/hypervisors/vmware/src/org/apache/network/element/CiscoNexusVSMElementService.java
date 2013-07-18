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

import org.apache.api.commands.DeleteCiscoNexusVSMCmd;
import org.apache.api.commands.DisableCiscoNexusVSMCmd;
import org.apache.api.commands.EnableCiscoNexusVSMCmd;
import org.apache.api.commands.ListCiscoNexusVSMsCmd;
import org.apache.api.response.CiscoNexusVSMResponse;
import org.apache.exception.ResourceInUseException;
import org.apache.network.CiscoNexusVSMDevice;
import org.apache.network.CiscoNexusVSMDeviceVO;
import org.apache.utils.component.PluggableService;

public interface CiscoNexusVSMElementService extends PluggableService {
    /**
     * removes a Cisco Nexus VSM
     * @param DeleteCiscoNexusVSMCmd 
     * @return true if VSM is deleted successfully
     */
    public boolean deleteCiscoNexusVSM(DeleteCiscoNexusVSMCmd cmd);

    /**
     * Enables a Cisco Nexus VSM. 
     */
    public CiscoNexusVSMDeviceVO enableCiscoNexusVSM(EnableCiscoNexusVSMCmd cmd);
    
    
    /**
     * Disables a Cisco Nexus VSM.
     */
    public CiscoNexusVSMDeviceVO disableCiscoNexusVSM(DisableCiscoNexusVSMCmd cmd);
    
    /**
     * Returns a list of VSMs.
     * @param ListCiscoNexusVSMsCmd
     * @return List<CiscoNexusVSMDeviceVO>
     */
    public List<CiscoNexusVSMDeviceVO> getCiscoNexusVSMs(ListCiscoNexusVSMsCmd cmd);
    
    /**
     * creates API response object for Cisco Nexus VSMs
     * @param vsmDeviceVO VSM VO object
     * @return CiscoNexusVSMResponse
     */
    
    public CiscoNexusVSMResponse createCiscoNexusVSMResponse(CiscoNexusVSMDevice vsmDeviceVO);
    
    /**
     * Creates a detailed API response object for Cisco Nexus VSMs
     * @param CiscoNexusVSMDeviceVO
     * @return CiscoNexusVSMResponse
     */
    public CiscoNexusVSMResponse createCiscoNexusVSMDetailedResponse(CiscoNexusVSMDevice vsmDeviceVO);

    /**
     * Validate Cisco Nexus VSM before associating with cluster
     *
     */
    public boolean validateVsmCluster(String vsmIp, String vsmUser, String vsmPassword, long clusterId, String clusterName) throws ResourceInUseException;
}
