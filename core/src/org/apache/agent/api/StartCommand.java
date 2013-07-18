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
import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.host.Host;

/**
 */
public class StartCommand extends Command {
    VirtualMachineTO vm;
    String hostIp;
    boolean executeInSequence = false;

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    protected StartCommand() {
    }

    public StartCommand(VirtualMachineTO vm, Host host, boolean executeInSequence) {
        this.vm = vm;
        this.hostIp = host.getPrivateIpAddress();
        this.executeInSequence = executeInSequence;
    }

    public String getHostIp() {
        return this.hostIp;
    }
}