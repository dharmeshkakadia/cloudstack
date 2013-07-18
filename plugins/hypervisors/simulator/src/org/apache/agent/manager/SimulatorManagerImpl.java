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

import org.apache.agent.api.Answer;
import org.apache.agent.api.AttachIsoCommand;
import org.apache.agent.api.AttachVolumeCommand;
import org.apache.agent.api.BackupSnapshotCommand;
import org.apache.agent.api.BumpUpPriorityCommand;
import org.apache.agent.api.CheckHealthCommand;
import org.apache.agent.api.CheckNetworkCommand;
import org.apache.agent.api.CheckRouterCommand;
import org.apache.agent.api.CheckS2SVpnConnectionsCommand;
import org.apache.agent.api.CheckVirtualMachineCommand;
import org.apache.agent.api.CleanupNetworkRulesCmd;
import org.apache.agent.api.ClusterSyncCommand;
import org.apache.agent.api.Command;
import org.apache.agent.api.ComputeChecksumCommand;
import org.apache.agent.api.CreatePrivateTemplateFromSnapshotCommand;
import org.apache.agent.api.CreatePrivateTemplateFromVolumeCommand;
import org.apache.agent.api.CreateStoragePoolCommand;
import org.apache.agent.api.CreateVMSnapshotCommand;
import org.apache.agent.api.CreateVolumeFromSnapshotCommand;
import org.apache.agent.api.DeleteStoragePoolCommand;
import org.apache.agent.api.DeleteVMSnapshotCommand;
import org.apache.agent.api.GetDomRVersionCmd;
import org.apache.agent.api.GetHostStatsCommand;
import org.apache.agent.api.GetStorageStatsCommand;
import org.apache.agent.api.GetVmStatsCommand;
import org.apache.agent.api.GetVncPortCommand;
import org.apache.agent.api.MaintainCommand;
import org.apache.agent.api.ManageSnapshotCommand;
import org.apache.agent.api.MigrateCommand;
import org.apache.agent.api.ModifyStoragePoolCommand;
import org.apache.agent.api.NetworkRulesVmSecondaryIpCommand;
import org.apache.agent.api.NetworkUsageCommand;
import org.apache.agent.api.PingTestCommand;
import org.apache.agent.api.PlugNicCommand;
import org.apache.agent.api.PrepareForMigrationCommand;
import org.apache.agent.api.PvlanSetupCommand;
import org.apache.agent.api.RebootCommand;
import org.apache.agent.api.RevertToVMSnapshotCommand;
import org.apache.agent.api.ScaleVmCommand;
import org.apache.agent.api.SecStorageSetupCommand;
import org.apache.agent.api.SecStorageVMSetupCommand;
import org.apache.agent.api.SecurityGroupRulesCmd;
import org.apache.agent.api.SetupGuestNetworkCommand;
import org.apache.agent.api.StartCommand;
import org.apache.agent.api.StopCommand;
import org.apache.agent.api.StoragePoolInfo;
import org.apache.agent.api.UnPlugNicCommand;
import org.apache.agent.api.check.CheckSshCommand;
import org.apache.agent.api.proxy.CheckConsoleProxyLoadCommand;
import org.apache.agent.api.proxy.WatchConsoleProxyLoadCommand;
import org.apache.agent.api.routing.DhcpEntryCommand;
import org.apache.agent.api.routing.IpAssocCommand;
import org.apache.agent.api.routing.IpAssocVpcCommand;
import org.apache.agent.api.routing.LoadBalancerConfigCommand;
import org.apache.agent.api.routing.SavePasswordCommand;
import org.apache.agent.api.routing.SetFirewallRulesCommand;
import org.apache.agent.api.routing.SetNetworkACLCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesVpcCommand;
import org.apache.agent.api.routing.SetSourceNatCommand;
import org.apache.agent.api.routing.SetStaticNatRulesCommand;
import org.apache.agent.api.routing.SetStaticRouteCommand;
import org.apache.agent.api.routing.Site2SiteVpnCfgCommand;
import org.apache.agent.api.routing.VmDataCommand;
import org.apache.agent.api.storage.CopyVolumeCommand;
import org.apache.agent.api.storage.CreateCommand;
import org.apache.agent.api.storage.DestroyCommand;
import org.apache.agent.api.storage.ListTemplateCommand;
import org.apache.agent.api.storage.ListVolumeCommand;
import org.apache.agent.api.storage.PrimaryStorageDownloadCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.DownloadCommand;
import org.apache.cloudstack.storage.command.DownloadProgressCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;
import org.apache.log4j.Logger;
import org.apache.resource.SimulatorStorageProcessor;
import org.apache.simulator.MockConfigurationVO;
import org.apache.simulator.MockHost;
import org.apache.simulator.MockVMVO;
import org.apache.simulator.dao.MockConfigurationDao;
import org.apache.simulator.dao.MockHostDao;
import org.apache.storage.resource.StorageSubsystemCommandHandler;
import org.apache.storage.resource.StorageSubsystemCommandHandlerBase;
import org.apache.utils.Pair;
import org.apache.utils.component.ManagerBase;
import org.apache.utils.db.DB;
import org.apache.utils.db.Transaction;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.vm.VirtualMachine.State;
import org.springframework.stereotype.Component;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

@Component
@Local(value = { SimulatorManager.class })
public class SimulatorManagerImpl extends ManagerBase implements SimulatorManager {
    private static final Logger s_logger = Logger.getLogger(SimulatorManagerImpl.class);
    @Inject
    MockVmManager _mockVmMgr;
    @Inject
    MockStorageManager _mockStorageMgr;
    @Inject
    MockAgentManager _mockAgentMgr;
    @Inject
    MockNetworkManager _mockNetworkMgr;
    @Inject
    MockConfigurationDao _mockConfigDao;
    @Inject
    MockHostDao _mockHost = null;
    protected StorageSubsystemCommandHandler storageHandler;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        SimulatorStorageProcessor processor = new SimulatorStorageProcessor(this);
        this.storageHandler = new StorageSubsystemCommandHandlerBase(processor);
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public MockVmManager getVmMgr() {
        return _mockVmMgr;
    }

    @Override
    public MockStorageManager getStorageMgr() {
        return _mockStorageMgr;
    }

    @Override
    public MockAgentManager getAgentMgr() {
        return _mockAgentMgr;
    }

    @DB
    @Override
    public Answer simulate(Command cmd, String hostGuid) {
        Transaction txn = Transaction.open(Transaction.SIMULATOR_DB);
        try {
            MockHost host = _mockHost.findByGuid(hostGuid);
            String cmdName = cmd.toString();
            int index = cmdName.lastIndexOf(".");
            if (index != -1) {
                cmdName = cmdName.substring(index + 1);
            }
            MockConfigurationVO config = _mockConfigDao.findByNameBottomUP(host.getDataCenterId(), host.getPodId(), host.getClusterId(), host.getId(), cmdName);

            SimulatorInfo info = new SimulatorInfo();
            info.setHostUuid(hostGuid);

            if (config != null) {
                Map<String, String> configParameters = config.getParameters();
                for (Map.Entry<String, String> entry : configParameters.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase("enabled")) {
                        info.setEnabled(Boolean.parseBoolean(entry.getValue()));
                    } else if (entry.getKey().equalsIgnoreCase("timeout")) {
                        try {
                            info.setTimeout(Integer.valueOf(entry.getValue()));
                        } catch (NumberFormatException e) {
                            s_logger.debug("invalid timeout parameter: " + e.toString());
                        }
                    } else if (entry.getKey().equalsIgnoreCase("wait")) {
                        try {
                            int wait = Integer.valueOf(entry.getValue());
                            Thread.sleep(wait);
                        } catch (NumberFormatException e) {
                            s_logger.debug("invalid timeout parameter: " + e.toString());
                        } catch (InterruptedException e) {
                            s_logger.debug("thread is interrupted: " + e.toString());
                        }
                    }
                }
            }

            if (cmd instanceof GetHostStatsCommand) {
                return _mockAgentMgr.getHostStatistic((GetHostStatsCommand) cmd);
            } else if (cmd instanceof CheckHealthCommand) {
                return _mockAgentMgr.checkHealth((CheckHealthCommand) cmd);
            } else if (cmd instanceof PingTestCommand) {
                return _mockAgentMgr.pingTest((PingTestCommand) cmd);
            } else if (cmd instanceof PrepareForMigrationCommand) {
                return _mockVmMgr.prepareForMigrate((PrepareForMigrationCommand) cmd);
            } else if (cmd instanceof MigrateCommand) {
                return _mockVmMgr.Migrate((MigrateCommand) cmd, info);
            } else if (cmd instanceof StartCommand) {
                return _mockVmMgr.startVM((StartCommand) cmd, info);
            } else if (cmd instanceof CheckSshCommand) {
                return _mockVmMgr.checkSshCommand((CheckSshCommand) cmd);
            } else if (cmd instanceof CheckVirtualMachineCommand) {
                return _mockVmMgr.checkVmState((CheckVirtualMachineCommand) cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand) {
                return _mockNetworkMgr.SetStaticNatRules((SetStaticNatRulesCommand) cmd);
            } else if (cmd instanceof SetFirewallRulesCommand) {
                return _mockNetworkMgr.SetFirewallRules((SetFirewallRulesCommand) cmd);
            } else if (cmd instanceof SetPortForwardingRulesCommand) {
                return _mockNetworkMgr.SetPortForwardingRules((SetPortForwardingRulesCommand) cmd);
            } else if (cmd instanceof NetworkUsageCommand) {
                return _mockNetworkMgr.getNetworkUsage((NetworkUsageCommand) cmd);
            } else if (cmd instanceof IpAssocCommand) {
                return _mockNetworkMgr.IpAssoc((IpAssocCommand) cmd);
            } else if (cmd instanceof LoadBalancerConfigCommand) {
                return _mockNetworkMgr.LoadBalancerConfig((LoadBalancerConfigCommand) cmd);
            } else if (cmd instanceof DhcpEntryCommand) {
                return _mockNetworkMgr.AddDhcpEntry((DhcpEntryCommand) cmd);
            } else if (cmd instanceof VmDataCommand) {
                return _mockVmMgr.setVmData((VmDataCommand) cmd);
            } else if (cmd instanceof CleanupNetworkRulesCmd) {
                return _mockVmMgr.CleanupNetworkRules((CleanupNetworkRulesCmd) cmd, info);
            } else if (cmd instanceof CheckNetworkCommand) {
                return _mockAgentMgr.checkNetworkCommand((CheckNetworkCommand) cmd);
            }else if (cmd instanceof StopCommand) {
                return _mockVmMgr.stopVM((StopCommand)cmd);
            } else if (cmd instanceof RebootCommand) {
                return _mockVmMgr.rebootVM((RebootCommand) cmd);
            } else if (cmd instanceof GetVncPortCommand) {
                return _mockVmMgr.getVncPort((GetVncPortCommand)cmd);
            } else if (cmd instanceof CheckConsoleProxyLoadCommand) {
                return _mockVmMgr.CheckConsoleProxyLoad((CheckConsoleProxyLoadCommand)cmd);
            } else if (cmd instanceof WatchConsoleProxyLoadCommand) {
                return _mockVmMgr.WatchConsoleProxyLoad((WatchConsoleProxyLoadCommand)cmd);
            } else if (cmd instanceof SecurityGroupRulesCmd) {
                return _mockVmMgr.AddSecurityGroupRules((SecurityGroupRulesCmd)cmd, info);
            } else if (cmd instanceof SavePasswordCommand) {
                return _mockVmMgr.SavePassword((SavePasswordCommand)cmd);
            } else if (cmd instanceof PrimaryStorageDownloadCommand) {
                return _mockStorageMgr.primaryStorageDownload((PrimaryStorageDownloadCommand)cmd);
            } else if (cmd instanceof CreateCommand) {
                return _mockStorageMgr.createVolume((CreateCommand)cmd);
            } else if (cmd instanceof AttachVolumeCommand) {
                return _mockStorageMgr.AttachVolume((AttachVolumeCommand)cmd);
            } else if (cmd instanceof AttachIsoCommand) {
                return _mockStorageMgr.AttachIso((AttachIsoCommand)cmd);
            } else if (cmd instanceof DeleteStoragePoolCommand) {
                return _mockStorageMgr.DeleteStoragePool((DeleteStoragePoolCommand)cmd);
            } else if (cmd instanceof ModifyStoragePoolCommand) {
                return _mockStorageMgr.ModifyStoragePool((ModifyStoragePoolCommand)cmd);
            } else if (cmd instanceof CreateStoragePoolCommand) {
                return _mockStorageMgr.CreateStoragePool((CreateStoragePoolCommand)cmd);
            } else if (cmd instanceof SecStorageSetupCommand) {
                return _mockStorageMgr.SecStorageSetup((SecStorageSetupCommand)cmd);
            } else if (cmd instanceof ListTemplateCommand) {
                return _mockStorageMgr.ListTemplates((ListTemplateCommand)cmd);
            } else if (cmd instanceof ListVolumeCommand) {
                return _mockStorageMgr.ListVolumes((ListVolumeCommand)cmd);
            } else if (cmd instanceof DestroyCommand) {
                return _mockStorageMgr.Destroy((DestroyCommand)cmd);
            } else if (cmd instanceof DownloadProgressCommand) {
                return _mockStorageMgr.DownloadProcess((DownloadProgressCommand)cmd);
            } else if (cmd instanceof DownloadCommand) {
                return _mockStorageMgr.Download((DownloadCommand)cmd);
            } else if (cmd instanceof GetStorageStatsCommand) {
                return _mockStorageMgr.GetStorageStats((GetStorageStatsCommand)cmd);
            } else if (cmd instanceof ManageSnapshotCommand) {
                return _mockStorageMgr.ManageSnapshot((ManageSnapshotCommand)cmd);
            } else if (cmd instanceof BackupSnapshotCommand) {
                return _mockStorageMgr.BackupSnapshot((BackupSnapshotCommand)cmd, info);
            } else if (cmd instanceof CreateVolumeFromSnapshotCommand) {
                return _mockStorageMgr.CreateVolumeFromSnapshot((CreateVolumeFromSnapshotCommand)cmd);
            } else if (cmd instanceof DeleteCommand) {
                return _mockStorageMgr.Delete((DeleteCommand)cmd);
            } else if (cmd instanceof SecStorageVMSetupCommand) {
                return _mockStorageMgr.SecStorageVMSetup((SecStorageVMSetupCommand)cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromSnapshotCommand) {
                return _mockStorageMgr.CreatePrivateTemplateFromSnapshot((CreatePrivateTemplateFromSnapshotCommand)cmd);
            } else if (cmd instanceof ComputeChecksumCommand) {
                return _mockStorageMgr.ComputeChecksum((ComputeChecksumCommand)cmd);
            } else if (cmd instanceof CreatePrivateTemplateFromVolumeCommand) {
                return _mockStorageMgr.CreatePrivateTemplateFromVolume((CreatePrivateTemplateFromVolumeCommand)cmd);
            } else if (cmd instanceof MaintainCommand) {
                return _mockAgentMgr.maintain((MaintainCommand)cmd);
            } else if (cmd instanceof GetVmStatsCommand) {
                return _mockVmMgr.getVmStats((GetVmStatsCommand)cmd);
            } else if (cmd instanceof CheckRouterCommand) {
                return _mockVmMgr.checkRouter((CheckRouterCommand) cmd);
            } else if (cmd instanceof BumpUpPriorityCommand) {
                return _mockVmMgr.bumpPriority((BumpUpPriorityCommand) cmd);
            } else if (cmd instanceof GetDomRVersionCmd) {
                return _mockVmMgr.getDomRVersion((GetDomRVersionCmd) cmd);
            } else if (cmd instanceof ClusterSyncCommand) {
                return new Answer(cmd);
            } else if (cmd instanceof CopyVolumeCommand) {
                return _mockStorageMgr.CopyVolume((CopyVolumeCommand) cmd);
            } else if (cmd instanceof PlugNicCommand) {
                return _mockNetworkMgr.plugNic((PlugNicCommand) cmd);
            } else if (cmd instanceof UnPlugNicCommand) {
                return _mockNetworkMgr.unplugNic((UnPlugNicCommand) cmd);
            } else if (cmd instanceof IpAssocVpcCommand) {
                return _mockNetworkMgr.ipAssoc((IpAssocVpcCommand) cmd);
            } else if (cmd instanceof SetSourceNatCommand) {
                return _mockNetworkMgr.setSourceNat((SetSourceNatCommand) cmd);
            } else if (cmd instanceof SetNetworkACLCommand) {
                return _mockNetworkMgr.setNetworkAcl((SetNetworkACLCommand) cmd);
            } else if (cmd instanceof SetupGuestNetworkCommand) {
                return _mockNetworkMgr.setUpGuestNetwork((SetupGuestNetworkCommand) cmd);
            } else if (cmd instanceof SetPortForwardingRulesVpcCommand) {
                return _mockNetworkMgr.setVpcPortForwards((SetPortForwardingRulesVpcCommand) cmd);
            } else if (cmd instanceof SetStaticNatRulesCommand) {
                return _mockNetworkMgr.setVPCStaticNatRules((SetStaticNatRulesCommand) cmd);
            } else if (cmd instanceof SetStaticRouteCommand) {
                return _mockNetworkMgr.setStaticRoute((SetStaticRouteCommand) cmd);
            } else if (cmd instanceof Site2SiteVpnCfgCommand) {
                return _mockNetworkMgr.siteToSiteVpn((Site2SiteVpnCfgCommand) cmd);
            } else if (cmd instanceof CheckS2SVpnConnectionsCommand) {
                return _mockNetworkMgr.checkSiteToSiteVpnConnection((CheckS2SVpnConnectionsCommand) cmd);
            } else if (cmd instanceof CreateVMSnapshotCommand) {
                return _mockVmMgr.createVmSnapshot((CreateVMSnapshotCommand) cmd);
            } else if (cmd instanceof DeleteVMSnapshotCommand) {
                return _mockVmMgr.deleteVmSnapshot((DeleteVMSnapshotCommand) cmd);
            } else if (cmd instanceof RevertToVMSnapshotCommand) {
                return _mockVmMgr.revertVmSnapshot((RevertToVMSnapshotCommand) cmd);
            } else if (cmd instanceof NetworkRulesVmSecondaryIpCommand) {
                return _mockVmMgr.plugSecondaryIp((NetworkRulesVmSecondaryIpCommand) cmd);
            } else if (cmd instanceof ScaleVmCommand) {
                return _mockVmMgr.scaleVm((ScaleVmCommand) cmd);
            } else if (cmd instanceof PvlanSetupCommand) {
                return _mockNetworkMgr.setupPVLAN((PvlanSetupCommand) cmd);
            } else if (cmd instanceof StorageSubSystemCommand) {
                return this.storageHandler.handleStorageCommands((StorageSubSystemCommand)cmd);
            } else {
                s_logger.error("Simulator does not implement command of type "+cmd.toString());
                return Answer.createUnsupportedCommandAnswer(cmd);
            }
        } catch(Exception e) {
            s_logger.error("Failed execute cmd: ", e);
            txn.rollback();
            return new Answer(cmd, false, e.toString());
        } finally {
            txn.close();
            txn = Transaction.open(Transaction.CLOUD_DB);
            txn.close();
        }
    }

    @Override
    public StoragePoolInfo getLocalStorage(String hostGuid) {
        return _mockStorageMgr.getLocalStorage(hostGuid);
    }

    @Override
    public Map<String, State> getVmStates(String hostGuid) {
        return _mockVmMgr.getVmStates(hostGuid);
    }

    @Override
    public Map<String, MockVMVO> getVms(String hostGuid) {
        return _mockVmMgr.getVms(hostGuid);
    }

    @Override
    public HashMap<String, Pair<Long, Long>> syncNetworkGroups(String hostGuid) {
        SimulatorInfo info = new SimulatorInfo();
        info.setHostUuid(hostGuid);
        return _mockVmMgr.syncNetworkGroups(info);
    }

    @Override
    public boolean configureSimulator(Long zoneId, Long podId, Long clusterId, Long hostId, String command,
            String values) {
        Transaction txn = Transaction.open(Transaction.SIMULATOR_DB);
        try {
            txn.start();
            MockConfigurationVO config = _mockConfigDao.findByCommand(zoneId, podId, clusterId, hostId, command);
            if (config == null) {
                config = new MockConfigurationVO();
                config.setClusterId(clusterId);
                config.setDataCenterId(zoneId);
                config.setPodId(podId);
                config.setHostId(hostId);
                config.setName(command);
                config.setValues(values);
                _mockConfigDao.persist(config);
                txn.commit();
            } else {
                config.setValues(values);
                _mockConfigDao.update(config.getId(), config);
                txn.commit();
            }
        } catch (Exception ex) {
            txn.rollback();
            throw new CloudRuntimeException("Unable to configure simulator because of " + ex.getMessage(), ex);
        } finally {
            txn.close();
        }
        return true;
    }
}
