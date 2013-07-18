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
package org.apache.agent.api;

import org.apache.agent.api.Command;

public class CreateLogicalSwitchCommand extends Command {
    
    private String _transportUuid;
    private String _transportType;
    private String _name;
    private String _ownerName;

    public CreateLogicalSwitchCommand(String transportUuid, String transportType, String name, String ownerName) {
        this._transportUuid = transportUuid;
        this._transportType = transportType;
        this._name = name;
        this._ownerName = ownerName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getTransportUuid() {
        return _transportUuid;
    }

    public String getTransportType() {
        return _transportType;
    }

    public String getName() {
        return _name;
    }

    public String getOwnerName() {
        return _ownerName;
    }

}