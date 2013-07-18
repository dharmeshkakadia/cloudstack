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

import java.util.Map;

import org.apache.agent.api.Command;
import org.apache.agent.api.to.NicTO;
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.agent.api.to.VolumeTO;

public class MigrateWithStorageSendCommand extends Command {
    VirtualMachineTO vm;
    Map<VolumeTO, Object> volumeToSr;
    Map<NicTO, Object> nicToNetwork;
    Map<String, String> token;

    public MigrateWithStorageSendCommand(VirtualMachineTO vm, Map<VolumeTO, Object> volumeToSr,
            Map<NicTO, Object> nicToNetwork, Map<String, String> token) {
        this.vm = vm;
        this.volumeToSr = volumeToSr;
        this.nicToNetwork = nicToNetwork;
        this.token = token;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    public Map<VolumeTO, Object> getVolumeToSr() {
        return volumeToSr;
    }

    public Map<NicTO, Object> getNicToNetwork() {
        return nicToNetwork;
    }

    public Map<String, String> getToken() {
        return token;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}