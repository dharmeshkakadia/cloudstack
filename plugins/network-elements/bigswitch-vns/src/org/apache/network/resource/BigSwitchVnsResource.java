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
package org.apache.network.resource;

import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.agent.IAgentControl;
import org.apache.agent.api.Answer;
import org.apache.agent.api.Command;
import org.apache.agent.api.CreateVnsNetworkAnswer;
import org.apache.agent.api.CreateVnsNetworkCommand;
import org.apache.agent.api.CreateVnsPortAnswer;
import org.apache.agent.api.CreateVnsPortCommand;
import org.apache.agent.api.DeleteVnsNetworkAnswer;
import org.apache.agent.api.DeleteVnsNetworkCommand;
import org.apache.agent.api.DeleteVnsPortAnswer;
import org.apache.agent.api.DeleteVnsPortCommand;
import org.apache.agent.api.MaintainAnswer;
import org.apache.agent.api.MaintainCommand;
import org.apache.agent.api.PingCommand;
import org.apache.agent.api.ReadyAnswer;
import org.apache.agent.api.ReadyCommand;
import org.apache.agent.api.StartupBigSwitchVnsCommand;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.UpdateVnsPortAnswer;
import org.apache.agent.api.UpdateVnsPortCommand;
import org.apache.host.Host;
import org.apache.host.Host.Type;
import org.apache.log4j.Logger;
import org.apache.network.bigswitch.AttachmentData;
import org.apache.network.bigswitch.BigSwitchVnsApi;
import org.apache.network.bigswitch.BigSwitchVnsApiException;
import org.apache.network.bigswitch.ControlClusterStatus;
import org.apache.network.bigswitch.NetworkData;
import org.apache.network.bigswitch.PortData;
import org.apache.resource.ServerResource;
import org.apache.utils.component.ManagerBase;

public class BigSwitchVnsResource extends ManagerBase implements ServerResource {
    private static final Logger s_logger = Logger.getLogger(BigSwitchVnsResource.class);

    private String _name;
    private String _guid;
    private String _zoneId;
    private int _numRetries;

    private BigSwitchVnsApi _bigswitchVnsApi;

    protected BigSwitchVnsApi createBigSwitchVnsApi() {
        return new BigSwitchVnsApi();
    }

    @Override
    public boolean configure(String name, Map<String, Object> params)
            throws ConfigurationException {

        _name = (String) params.get("name");
        if (_name == null) {
            throw new ConfigurationException("Unable to find name");
        }

        _guid = (String)params.get("guid");
        if (_guid == null) {
            throw new ConfigurationException("Unable to find the guid");
        }

        _zoneId = (String) params.get("zoneId");
        if (_zoneId == null) {
            throw new ConfigurationException("Unable to find zone");
        }

        _numRetries = 2;

        String ip = (String) params.get("ip");
        if (ip == null) {
            throw new ConfigurationException("Unable to find IP");
        }

        _bigswitchVnsApi = createBigSwitchVnsApi();
        _bigswitchVnsApi.setControllerAddress(ip);

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
        return _name;
    }

    @Override
    public Type getType() {
        // Think up a better name for this Type?
        return Host.Type.L2Networking;
    }

    @Override
    public StartupCommand[] initialize() {
        StartupBigSwitchVnsCommand sc = new StartupBigSwitchVnsCommand();
        sc.setGuid(_guid);
        sc.setName(_name);
        sc.setDataCenter(_zoneId);
        sc.setPod("");
        sc.setPrivateIpAddress("");
        sc.setStorageIpAddress("");
        sc.setVersion("");
        return new StartupCommand[] { sc };
    }

        @Override
        public PingCommand getCurrentStatus(long id) {
        try {
            ControlClusterStatus ccs = _bigswitchVnsApi.getControlClusterStatus();
            if (!ccs.getStatus()) {
                s_logger.error("ControlCluster state is not ready: " + ccs.getStatus());
                return null;
            }
        } catch (BigSwitchVnsApiException e) {
                s_logger.error("getControlClusterStatus failed", e);
                return null;
        }
        return new PingCommand(Host.Type.L2Networking, id);
        }

    @Override
    public Answer executeRequest(Command cmd) {
        return executeRequest(cmd, _numRetries);
    }

    public Answer executeRequest(Command cmd, int numRetries) {
        if (cmd instanceof ReadyCommand) {
            return executeRequest((ReadyCommand) cmd);
        }
        else if (cmd instanceof MaintainCommand) {
            return executeRequest((MaintainCommand)cmd);
        }
        else if (cmd instanceof CreateVnsNetworkCommand) {
            return executeRequest((CreateVnsNetworkCommand)cmd, numRetries);
        }
        else if (cmd instanceof DeleteVnsNetworkCommand) {
            return executeRequest((DeleteVnsNetworkCommand) cmd, numRetries);
        }
        else if (cmd instanceof CreateVnsPortCommand) {
            return executeRequest((CreateVnsPortCommand) cmd, numRetries);
        }
        else if (cmd instanceof DeleteVnsPortCommand) {
            return executeRequest((DeleteVnsPortCommand) cmd, numRetries);
        }
        else if (cmd instanceof UpdateVnsPortCommand) {
                return executeRequest((UpdateVnsPortCommand) cmd, numRetries);
        }
        s_logger.debug("Received unsupported command " + cmd.toString());
        return Answer.createUnsupportedCommandAnswer(cmd);
    }

    @Override
    public void disconnected() {
    }

    @Override
    public IAgentControl getAgentControl() {
        return null;
    }

    @Override
    public void setAgentControl(IAgentControl agentControl) {
    }

    private Answer executeRequest(CreateVnsNetworkCommand cmd, int numRetries) {
        NetworkData network = new NetworkData();
        network.getNetwork().setTenant_id(cmd.getTenantUuid());
        network.getNetwork().setUuid(cmd.getNetworkUuid());
        network.getNetwork().setDisplay_name(truncate("vns-cloudstack-" + cmd.getName(), 64));
        network.getNetwork().setVlan(cmd.getVlan());

        try {
            _bigswitchVnsApi.createNetwork(network);
            return new CreateVnsNetworkAnswer(cmd, true, "VNS " + network.getNetwork().getUuid() + " created");
        } catch (BigSwitchVnsApiException e) {
                if (numRetries > 0) {
                        return retry(cmd, --numRetries);
                }
                else {
                        return new CreateVnsNetworkAnswer(cmd, e);
                }
        }

    }

    private Answer executeRequest(DeleteVnsNetworkCommand cmd, int numRetries) {
        try {
            _bigswitchVnsApi.deleteNetwork(cmd.get_tenantUuid(), cmd.getNetworkUuid());
            return new DeleteVnsNetworkAnswer(cmd, true, "VNS " + cmd.getNetworkUuid() + " deleted");
        } catch (BigSwitchVnsApiException e) {
                if (numRetries > 0) {
                        return retry(cmd, --numRetries);
                }
                else {
                        return new DeleteVnsNetworkAnswer(cmd, e);
                }
        }
    }

    private Answer executeRequest(CreateVnsPortCommand cmd, int numRetries) {
        PortData port = new PortData();
        port.getPort().setId(cmd.getPortUuid());
        port.getPort().setName(cmd.getPortName());
        port.getPort().setTenant_id(cmd.getTenantUuid());

        try {
            _bigswitchVnsApi.createPort(cmd.getNetworkUuid(), port);
            try {
                AttachmentData attachment = new AttachmentData();
                attachment.getAttachment().setId(cmd.getPortUuid());
                attachment.getAttachment().setMac(cmd.getMac());
                _bigswitchVnsApi.modifyPortAttachment(cmd.getTenantUuid(),
                                cmd.getNetworkUuid(), cmd.getPortUuid(), attachment);

            } catch (BigSwitchVnsApiException ex) {
                s_logger.warn("modifyPortAttachment failed after switchport was created, removing switchport");
                _bigswitchVnsApi.deletePort(cmd.getTenantUuid(), cmd.getNetworkUuid(), cmd.getPortUuid());
                throw (ex); // Rethrow the original exception
            }
            return new CreateVnsPortAnswer(cmd, true, "network port " + cmd.getPortUuid() + " created");
        } catch (BigSwitchVnsApiException e) {
                if (numRetries > 0) {
                        return retry(cmd, --numRetries);
                }
                else {
                        return new CreateVnsPortAnswer(cmd, e);
                }
        }
    }

    private Answer executeRequest(DeleteVnsPortCommand cmd, int numRetries) {
        try {
                _bigswitchVnsApi.deletePortAttachment(cmd.getTenantUuid(), cmd.getNetworkUuid(), cmd.getPortUuid());
                try {
                        _bigswitchVnsApi.deletePort(cmd.getTenantUuid(), cmd.getNetworkUuid(), cmd.getPortUuid());
                } catch (BigSwitchVnsApiException ex) {
                s_logger.warn("deletePort failed after portAttachment was removed");
                throw (ex); // Rethrow the original exception
            }
                return new DeleteVnsPortAnswer(cmd, true, "network port " + cmd.getPortUuid() + " deleted");
        } catch (BigSwitchVnsApiException e) {
                if (numRetries > 0) {
                        return retry(cmd, --numRetries);
                }
                else {
                        return new DeleteVnsPortAnswer(cmd, e);
                }
        }
    }

    private Answer executeRequest(UpdateVnsPortCommand cmd, int numRetries) {
        PortData port = new PortData();
        port.getPort().setId(cmd.getPortUuid());
        port.getPort().setName(cmd.getPortName());
        port.getPort().setTenant_id(cmd.getTenantUuid());

        try {
            _bigswitchVnsApi.modifyPort(cmd.getNetworkUuid(), port);
            return new UpdateVnsPortAnswer(cmd, true, "Network Port  " + cmd.getPortUuid() + " updated");
        } catch (BigSwitchVnsApiException e) {
                if (numRetries > 0) {
                        return retry(cmd, --numRetries);
                }
                else {
                        return new UpdateVnsPortAnswer(cmd, e);
                }
        }

    }

    private Answer executeRequest(ReadyCommand cmd) {
        return new ReadyAnswer(cmd);
    }

    private Answer executeRequest(MaintainCommand cmd) {
        return new MaintainAnswer(cmd);
    }

    private Answer retry(Command cmd, int numRetries) {
        s_logger.warn("Retrying " + cmd.getClass().getSimpleName() + ". Number of retries remaining: " + numRetries);
        return executeRequest(cmd, numRetries);
    }

    private String truncate(String string, int length) {
        if (string.length() <= length) {
                return string;
        }
        else {
                return string.substring(0, length);
        }
    }

}