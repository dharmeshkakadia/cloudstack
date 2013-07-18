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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.agent.api.CreateVnsNetworkAnswer;
import org.apache.agent.api.CreateVnsNetworkCommand;
import org.apache.agent.api.CreateVnsPortAnswer;
import org.apache.agent.api.CreateVnsPortCommand;
import org.apache.agent.api.DeleteVnsNetworkAnswer;
import org.apache.agent.api.DeleteVnsNetworkCommand;
import org.apache.agent.api.DeleteVnsPortAnswer;
import org.apache.agent.api.DeleteVnsPortCommand;
import org.apache.agent.api.PingCommand;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.UpdateVnsPortAnswer;
import org.apache.agent.api.UpdateVnsPortCommand;
import org.apache.host.Host;
import org.apache.network.bigswitch.AttachmentData;
import org.apache.network.bigswitch.BigSwitchVnsApi;
import org.apache.network.bigswitch.BigSwitchVnsApiException;
import org.apache.network.bigswitch.ControlClusterStatus;
import org.apache.network.bigswitch.NetworkData;
import org.apache.network.bigswitch.PortData;
import org.apache.network.resource.BigSwitchVnsResource;
import org.junit.Before;
import org.junit.Test;

public class BigSwitchVnsResourceTest {
    BigSwitchVnsApi _bigswitchVnsApi = mock(BigSwitchVnsApi.class);
    BigSwitchVnsResource _resource;
    Map<String,Object> _parameters;

    @Before
    public void setUp() throws ConfigurationException {
        _resource = new BigSwitchVnsResource() {
            protected BigSwitchVnsApi createBigSwitchVnsApi() {
                return _bigswitchVnsApi;
            }
        };

        _parameters = new HashMap<String,Object>();
        _parameters.put("name","bigswitchvnstestdevice");
        _parameters.put("ip","127.0.0.1");
        _parameters.put("guid", "aaaaa-bbbbb-ccccc");
        _parameters.put("zoneId", "blublub");
    }

    @Test (expected=ConfigurationException.class)
    public void resourceConfigureFailure() throws ConfigurationException {
        _resource.configure("BigSwitchVnsResource", Collections.<String,Object>emptyMap());
    }

    @Test
    public void resourceConfigure() throws ConfigurationException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        verify(_bigswitchVnsApi).setControllerAddress("127.0.0.1");

        assertTrue("bigswitchvnstestdevice".equals(_resource.getName()));

        /* Pretty lame test, but here to assure this plugin fails
         * if the type name ever changes from L2Networking
         */
        assertTrue(_resource.getType() == Host.Type.L2Networking);
    }

    @Test
    public void testInitialization() throws ConfigurationException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        StartupCommand[] sc = _resource.initialize();
        assertTrue(sc.length ==1);
        assertTrue("aaaaa-bbbbb-ccccc".equals(sc[0].getGuid()));
        assertTrue("bigswitchvnstestdevice".equals(sc[0].getName()));
        assertTrue("blublub".equals(sc[0].getDataCenter()));
    }

    @Test
    public void testPingCommandStatusOk() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        ControlClusterStatus ccs = mock(ControlClusterStatus.class);
        when(ccs.getStatus()).thenReturn(true);
        when(_bigswitchVnsApi.getControlClusterStatus()).thenReturn(ccs);

        PingCommand ping = _resource.getCurrentStatus(42);
        assertTrue(ping != null);
        assertTrue(ping.getHostId() == 42);
        assertTrue(ping.getHostType() == Host.Type.L2Networking);
    }

    @Test
    public void testPingCommandStatusFail() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        ControlClusterStatus ccs = mock(ControlClusterStatus.class);
        when(ccs.getStatus()).thenReturn(false);
        when(_bigswitchVnsApi.getControlClusterStatus()).thenReturn(ccs);

        PingCommand ping = _resource.getCurrentStatus(42);
        assertTrue(ping == null);
    }

    @Test
    public void testPingCommandStatusApiException() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        ControlClusterStatus ccs = mock(ControlClusterStatus.class);
        when(ccs.getStatus()).thenReturn(false);
        when(_bigswitchVnsApi.getControlClusterStatus()).thenThrow(new BigSwitchVnsApiException());

        PingCommand ping = _resource.getCurrentStatus(42);
        assertTrue(ping == null);
    }

    @Test
    public void testRetries() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        NetworkData networkdata = mock(NetworkData.class);
        NetworkData.Network network = mock(NetworkData.Network.class);
        when(networkdata.getNetwork()).thenReturn(network);
        when(network.getUuid()).thenReturn("cccc").thenReturn("cccc");

        CreateVnsNetworkCommand cntkc = new CreateVnsNetworkCommand((String)_parameters.get("guid"), "networkName", "tenantid", 1);
        CreateVnsNetworkAnswer cntka = (CreateVnsNetworkAnswer) _resource.executeRequest(cntkc);
        assertTrue(cntka.getResult());
    }

    @Test
    public void testCreateNetwork() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        NetworkData networkdata = mock(NetworkData.class);
        NetworkData.Network network = mock(NetworkData.Network.class);
        when(networkdata.getNetwork()).thenReturn(network);
        when(network.getUuid()).thenReturn("cccc").thenReturn("cccc");

        CreateVnsNetworkCommand cntkc = new CreateVnsNetworkCommand((String)_parameters.get("guid"), "networkName", "tenantid", 1);
        CreateVnsNetworkAnswer cntka = (CreateVnsNetworkAnswer) _resource.executeRequest(cntkc);
        assertTrue(cntka.getResult());
    }

    @Test
    public void testCreateNetworkApiException() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        NetworkData networkdata = mock(NetworkData.class);
        NetworkData.Network network = mock(NetworkData.Network.class);
        when(networkdata.getNetwork()).thenReturn(network);
        when(network.getUuid()).thenReturn("cccc").thenReturn("cccc");
        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).createNetwork((NetworkData)any());

        CreateVnsNetworkCommand cntkc = new CreateVnsNetworkCommand((String)_parameters.get("guid"), "networkName", "tenantid", 1);
        CreateVnsNetworkAnswer cntka = (CreateVnsNetworkAnswer) _resource.executeRequest(cntkc);
        assertFalse(cntka.getResult());
    }

    @Test
    public void testDeleteNetwork() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        DeleteVnsNetworkCommand dntkc = new DeleteVnsNetworkCommand("tenantid", "networkid");
        DeleteVnsNetworkAnswer dntka = (DeleteVnsNetworkAnswer) _resource.executeRequest(dntkc);
        assertTrue(dntka.getResult());
    }

    @Test
    public void testDeleteNetworkApiException() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).deleteNetwork((String)any(), (String)any());

        DeleteVnsNetworkCommand dntkc = new DeleteVnsNetworkCommand("tenantid", "networkid");
        DeleteVnsNetworkAnswer dntka = (DeleteVnsNetworkAnswer) _resource.executeRequest(dntkc);
        assertFalse(dntka.getResult());
    }

    @Test
    public void testCreatePort() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        PortData portdata = mock(PortData.class);
        PortData.Port port = mock(PortData.Port.class);
        when(portdata.getPort()).thenReturn(port);
        when(port.getId()).thenReturn("eeee");

        CreateVnsPortCommand cntkc = new CreateVnsPortCommand("networkid", "portid", "tenantid", "portname", "aa:bb:cc:dd:ee:ff");
        CreateVnsPortAnswer cntka = (CreateVnsPortAnswer) _resource.executeRequest(cntkc);
        assertTrue(cntka.getResult());
    }

    @Test
    public void testCreatePortApiExceptionInCreate() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        PortData portdata = mock(PortData.class);
        PortData.Port port = mock(PortData.Port.class);
        when(portdata.getPort()).thenReturn(port);
        when(port.getId()).thenReturn("eeee");
        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).createPort((String)any(), (PortData)any());

        CreateVnsPortCommand cntkc = new CreateVnsPortCommand("networkid", "portid", "tenantid", "portname", "aa:bb:cc:dd:ee:ff");
        CreateVnsPortAnswer cntka = (CreateVnsPortAnswer) _resource.executeRequest(cntkc);
        assertFalse(cntka.getResult());
    }

    @Test
    public void testCreatePortApiExceptionInModify() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        PortData portdata = mock(PortData.class);
        PortData.Port port = mock(PortData.Port.class);
        when(portdata.getPort()).thenReturn(port);
        when(port.getId()).thenReturn("eeee");
        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).modifyPortAttachment((String)any(), (String)any(), (String)any(), (AttachmentData)any());

        CreateVnsPortCommand cntkc = new CreateVnsPortCommand("networkid", "portid", "tenantid", "portname", "aa:bb:cc:dd:ee:ff");
        CreateVnsPortAnswer cntka = (CreateVnsPortAnswer) _resource.executeRequest(cntkc);
        assertFalse(cntka.getResult());
        verify(_bigswitchVnsApi, atLeastOnce()).deletePort((String) any(), (String) any(), (String) any());
    }

    @Test
    public void testDeletePortException() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).deletePort((String) any(), (String) any(), (String) any());
        DeleteVnsPortAnswer dntkpa = (DeleteVnsPortAnswer) _resource.executeRequest(new DeleteVnsPortCommand("networkId",
                "portid", "tenantid"));
        assertFalse(dntkpa.getResult());
    }

    @Test
    public void testUpdatePortException() throws ConfigurationException, BigSwitchVnsApiException {
        _resource.configure("BigSwitchVnsResource", _parameters);

        doThrow(new BigSwitchVnsApiException()).when(_bigswitchVnsApi).modifyPort((String) any(), (PortData)any());
        UpdateVnsPortAnswer dntkpa = (UpdateVnsPortAnswer) _resource.executeRequest(
                new UpdateVnsPortCommand("networkId","portId","tenantId","portname"));
        assertFalse(dntkpa.getResult());
    }
}
