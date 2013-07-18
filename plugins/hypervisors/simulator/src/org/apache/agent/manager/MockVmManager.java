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
package org.apache.agent.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.agent.api.Answer;
import org.apache.agent.api.BumpUpPriorityCommand;
import org.apache.agent.api.CheckRouterAnswer;
import org.apache.agent.api.CheckRouterCommand;
import org.apache.agent.api.CheckVirtualMachineCommand;
import org.apache.agent.api.CleanupNetworkRulesCmd;
import org.apache.agent.api.CreateVMSnapshotCommand;
import org.apache.agent.api.DeleteVMSnapshotCommand;
import org.apache.agent.api.GetDomRVersionAnswer;
import org.apache.agent.api.GetDomRVersionCmd;
import org.apache.agent.api.GetVmStatsCommand;
import org.apache.agent.api.GetVncPortCommand;
import org.apache.agent.api.MigrateAnswer;
import org.apache.agent.api.MigrateCommand;
import org.apache.agent.api.NetworkRulesVmSecondaryIpCommand;
import org.apache.agent.api.PrepareForMigrationAnswer;
import org.apache.agent.api.PrepareForMigrationCommand;
import org.apache.agent.api.RebootAnswer;
import org.apache.agent.api.RebootCommand;
import org.apache.agent.api.RevertToVMSnapshotCommand;
import org.apache.agent.api.ScaleVmCommand;
import org.apache.agent.api.SecurityGroupRuleAnswer;
import org.apache.agent.api.SecurityGroupRulesCmd;
import org.apache.agent.api.StartAnswer;
import org.apache.agent.api.StartCommand;
import org.apache.agent.api.StopAnswer;
import org.apache.agent.api.StopCommand;
import org.apache.agent.api.check.CheckSshAnswer;
import org.apache.agent.api.check.CheckSshCommand;
import org.apache.agent.api.proxy.CheckConsoleProxyLoadCommand;
import org.apache.agent.api.proxy.WatchConsoleProxyLoadCommand;
import org.apache.agent.api.routing.SavePasswordCommand;
import org.apache.agent.api.routing.VmDataCommand;
import org.apache.simulator.MockVMVO;
import org.apache.utils.Pair;
import org.apache.utils.component.Manager;
import org.apache.vm.VirtualMachine.State;

public interface MockVmManager extends Manager {

    Map<String, State> getVmStates(String hostGuid);

    Map<String, MockVMVO> getVms(String hostGuid);

    HashMap<String, Pair<Long, Long>> syncNetworkGroups(SimulatorInfo info);

    StartAnswer startVM(StartCommand cmd, SimulatorInfo info);

    StopAnswer stopVM(StopCommand cmd);

    RebootAnswer rebootVM(RebootCommand cmd);

    Answer checkVmState(CheckVirtualMachineCommand cmd);

    Answer getVncPort(GetVncPortCommand cmd);

	Answer getVmStats(GetVmStatsCommand cmd);

    CheckSshAnswer checkSshCommand(CheckSshCommand cmd);

    Answer setVmData(VmDataCommand cmd);

    Answer CheckConsoleProxyLoad(CheckConsoleProxyLoadCommand cmd);

    Answer WatchConsoleProxyLoad(WatchConsoleProxyLoadCommand cmd);

    Answer SavePassword(SavePasswordCommand cmd);

    MigrateAnswer Migrate(MigrateCommand cmd, SimulatorInfo info);

    PrepareForMigrationAnswer prepareForMigrate(PrepareForMigrationCommand cmd);

    SecurityGroupRuleAnswer AddSecurityGroupRules(SecurityGroupRulesCmd cmd, SimulatorInfo info);

    GetDomRVersionAnswer getDomRVersion(GetDomRVersionCmd cmd);

    CheckRouterAnswer checkRouter(CheckRouterCommand cmd);

    Answer bumpPriority(BumpUpPriorityCommand cmd);

    Answer CleanupNetworkRules(CleanupNetworkRulesCmd cmd, SimulatorInfo info);

    Answer scaleVm(ScaleVmCommand cmd);

    Answer plugSecondaryIp(NetworkRulesVmSecondaryIpCommand cmd);

    Answer createVmSnapshot(CreateVMSnapshotCommand cmd);

    Answer deleteVmSnapshot(DeleteVMSnapshotCommand cmd);

    Answer revertVmSnapshot(RevertToVMSnapshotCommand cmd);
}
