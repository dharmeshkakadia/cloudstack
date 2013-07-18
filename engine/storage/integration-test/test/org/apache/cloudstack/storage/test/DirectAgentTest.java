/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.storage.test;

import java.util.UUID;

import javax.inject.Inject;

import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import org.apache.cloudstack.storage.to.ImageStoreTO;
import org.apache.cloudstack.storage.to.PrimaryDataStoreTO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.dc.DataCenter.NetworkType;
import org.apache.agent.AgentManager;
import org.apache.agent.api.Command;
import org.apache.agent.api.ReadyCommand;
import org.apache.dc.ClusterVO;
import org.apache.dc.DataCenterVO;
import org.apache.dc.HostPodVO;
import org.apache.dc.dao.ClusterDao;
import org.apache.dc.dao.DataCenterDao;
import org.apache.dc.dao.HostPodDao;
import org.apache.exception.AgentUnavailableException;
import org.apache.exception.OperationTimedoutException;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.org.Cluster.ClusterType;
import org.apache.org.Managed.ManagedState;
import org.apache.resource.ResourceState;

@ContextConfiguration(locations = "classpath:/storageContext.xml")
public class DirectAgentTest extends CloudStackTestNGBase {
    @Inject
    AgentManager agentMgr;
    @Inject
    HostDao hostDao;
    @Inject
    HostPodDao podDao;
    @Inject
    ClusterDao clusterDao;
    @Inject
    DataCenterDao dcDao;
    private long dcId;
    private long clusterId;
    private long hostId;

    @Test(priority = -1)
    public void setUp() {
        HostVO host = hostDao.findByGuid(getHostGuid());
        if (host != null) {
            hostId = host.getId();
            dcId = host.getDataCenterId();
            clusterId = host.getClusterId();
            return;
        }
        // create data center
        DataCenterVO dc = new DataCenterVO(UUID.randomUUID().toString(), "test", "8.8.8.8", null, "10.0.0.1", null,
                "10.0.0.1/24", null, null, NetworkType.Basic, null, null, true, true, null, null);
        dc = dcDao.persist(dc);
        dcId = dc.getId();
        // create pod

        HostPodVO pod = new HostPodVO(UUID.randomUUID().toString(), dc.getId(), getHostGateway(), getHostCidr(), 8,
                "test");
        pod = podDao.persist(pod);
        // create xen cluster
        ClusterVO cluster = new ClusterVO(dc.getId(), pod.getId(), "devcloud cluster");
        cluster.setHypervisorType(HypervisorType.XenServer.toString());
        cluster.setClusterType(ClusterType.CloudManaged);
        cluster.setManagedState(ManagedState.Managed);
        cluster = clusterDao.persist(cluster);
        clusterId = cluster.getId();
        // create xen host

        host = new HostVO(getHostGuid());
        host.setName("devcloud xen host");
        host.setType(Host.Type.Routing);
        host.setHypervisorType(HypervisorType.XenServer);
        host.setPrivateIpAddress(getHostIp());
        host.setDataCenterId(dc.getId());
        host.setVersion("6.0.1");
        host.setAvailable(true);
        host.setSetup(true);
        host.setLastPinged(0);
        host.setResourceState(ResourceState.Enabled);
        host.setClusterId(cluster.getId());

        host = hostDao.persist(host);
        hostId = host.getId();
    }

    @Test
    public void testInitResource() {
        ReadyCommand cmd = new ReadyCommand(dcId);
        try {
            agentMgr.send(hostId, cmd);
        } catch (AgentUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationTimedoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testDownloadTemplate() {
        ImageStoreTO image = Mockito.mock(ImageStoreTO.class);
        PrimaryDataStoreTO primaryStore = Mockito.mock(PrimaryDataStoreTO.class);
        Mockito.when(primaryStore.getUuid()).thenReturn(getLocalStorageUuid());
        // Mockito.when(image.get).thenReturn(primaryStore);

        ImageStoreTO imageStore = Mockito.mock(ImageStoreTO.class);
        Mockito.when(imageStore.getProtocol()).thenReturn("http");

        TemplateObjectTO template = Mockito.mock(TemplateObjectTO.class);
        Mockito.when(template.getPath()).thenReturn(getTemplateUrl());
        Mockito.when(template.getDataStore()).thenReturn(imageStore);

        // Mockito.when(image.getTemplate()).thenReturn(template);
        // CopyTemplateToPrimaryStorageCmd cmd = new
        // CopyTemplateToPrimaryStorageCmd(image);
        Command cmd = null;
        try {
            agentMgr.send(hostId, cmd);
        } catch (AgentUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationTimedoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
