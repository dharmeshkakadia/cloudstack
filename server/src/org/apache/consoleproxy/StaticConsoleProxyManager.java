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
package org.apache.consoleproxy;


import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupProxyCommand;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.host.HostVO;
import org.apache.host.Host.Type;
import org.apache.host.dao.HostDao;
import org.apache.info.ConsoleProxyInfo;
import org.apache.keystore.KeystoreDao;
import org.apache.keystore.KeystoreManager;
import org.apache.log4j.Logger;
import org.apache.resource.ResourceManager;
import org.apache.resource.ResourceStateAdapter;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.utils.NumbersUtil;
import org.apache.vm.VMInstanceVO;
import org.apache.vm.dao.ConsoleProxyDao;
import org.apache.vm.dao.VMInstanceDao;

@Local(value={ConsoleProxyManager.class})
public class StaticConsoleProxyManager extends AgentBasedConsoleProxyManager implements ConsoleProxyManager,
        ResourceStateAdapter {
    private static final Logger s_logger = Logger.getLogger(StaticConsoleProxyManager.class);

    @Inject
    ConsoleProxyDao _proxyDao;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    private VMInstanceDao _instanceDao;
    @Inject
    KeystoreDao _ksDao;
    @Inject
    private KeystoreManager _ksMgr;
    @Inject
    private HostDao _hostDao;
    private final Random _random = new Random(System.currentTimeMillis());
    private String _hashKey;
    private String _ip = null;



    @Override
    protected HostVO findHost(VMInstanceVO vm) {

        List<HostVO> hosts = _resourceMgr.listAllUpAndEnabledHostsInOneZoneByType(Type.ConsoleProxy, vm.getDataCenterId());

        return hosts.isEmpty() ? null : hosts.get(0);
    }

    @Override
    public ConsoleProxyInfo assignProxy(long dataCenterId, long userVmId) {
        return new ConsoleProxyInfo(_sslEnabled, _ip, _consoleProxyPort, _consoleProxyUrlPort, _consoleProxyUrlDomain);
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _ip = _configDao.getValue("consoleproxy.static.publicIp");
        if (_ip == null) {
            _ip = "127.0.0.1";
        }


        String value = (String) params.get("consoleproxy.sslEnabled");
        if (value != null && value.equalsIgnoreCase("true")) {
            _sslEnabled = true;
        }
        int defaultPort = 8088;
        if (_sslEnabled)
            defaultPort = 8443;
        _consoleProxyUrlPort = NumbersUtil.parseInt(_configDao.getValue("consoleproxy.static.port"), defaultPort);

        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);

        return true;
    }

    @Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
        if (!(cmd[0] instanceof StartupProxyCommand)) {
            return null;
        }

        host.setType(org.apache.host.Host.Type.ConsoleProxy);
        return host;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource,
            Map<String, String> details, List<String> hostTags) {
        return null;
    }

    @Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage)
            throws UnableDeleteHostException {
        return null;
    }

}
