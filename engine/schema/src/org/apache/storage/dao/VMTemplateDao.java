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
package org.apache.storage.dao;

import java.util.List;
import java.util.Map;

import org.apache.cloudstack.engine.subsystem.api.storage.TemplateEvent;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateState;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.storage.VMTemplateVO;
import org.apache.utils.db.GenericDao;
import org.apache.utils.fsm.StateDao;

/*
 * Data Access Object for vm_templates table
 */
public interface VMTemplateDao extends GenericDao<VMTemplateVO, Long> {

    public List<VMTemplateVO> listByPublic();

    public VMTemplateVO findByName(String templateName);

    public VMTemplateVO findByTemplateName(String templateName);

    // public void update(VMTemplateVO template);

    public List<VMTemplateVO> listAllSystemVMTemplates();

    public List<VMTemplateVO> listDefaultBuiltinTemplates();

    public String getRoutingTemplateUniqueName();

    public List<VMTemplateVO> findIsosByIdAndPath(Long domainId, Long accountId, String path);

    public List<VMTemplateVO> listReadyTemplates();

    public List<VMTemplateVO> listByAccountId(long accountId);

    public long addTemplateToZone(VMTemplateVO tmplt, long zoneId);

    public List<VMTemplateVO> listAllInZone(long dataCenterId);

    public List<VMTemplateVO> listAllActive();

    public List<VMTemplateVO> listByHypervisorType(List<HypervisorType> hyperTypes);

    public List<VMTemplateVO> publicIsoSearch(Boolean bootable, boolean listRemoved, Map<String, String> tags);

    public List<VMTemplateVO> userIsoSearch(boolean listRemoved);

    VMTemplateVO findSystemVMTemplate(long zoneId);

    VMTemplateVO findSystemVMTemplate(long zoneId, HypervisorType hType);

    VMTemplateVO findRoutingTemplate(HypervisorType type, String templateName);

    List<Long> listPrivateTemplatesByHost(Long hostId);

    public Long countTemplatesForAccount(long accountId);

    List<VMTemplateVO> findTemplatesToSyncToS3();

}
