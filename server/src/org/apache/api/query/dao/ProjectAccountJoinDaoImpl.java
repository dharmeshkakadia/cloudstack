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
package org.apache.api.query.dao;

import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.apache.projects.ProjectAccount;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.api.query.vo.ProjectAccountJoinVO;
import org.apache.cloudstack.api.response.ProjectAccountResponse;
import org.springframework.stereotype.Component;

@Component
@Local(value={ProjectAccountJoinDao.class})
public class ProjectAccountJoinDaoImpl extends GenericDaoBase<ProjectAccountJoinVO, Long> implements ProjectAccountJoinDao {
    public static final Logger s_logger = Logger.getLogger(ProjectAccountJoinDaoImpl.class);


    private SearchBuilder<ProjectAccountJoinVO> paIdSearch;

    protected ProjectAccountJoinDaoImpl() {

        paIdSearch = createSearchBuilder();
        paIdSearch.and("accountId", paIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        paIdSearch.and("projectId", paIdSearch.entity().getProjectId(), SearchCriteria.Op.EQ);
        paIdSearch.done();

        this._count = "select count(distinct id) from project_account_view WHERE ";
    }




    @Override
    public ProjectAccountResponse newProjectAccountResponse(ProjectAccountJoinVO proj) {
        ProjectAccountResponse projectAccountResponse = new ProjectAccountResponse();

        projectAccountResponse.setProjectId(proj.getProjectUuid());
        projectAccountResponse.setProjectName(proj.getProjectName());

        projectAccountResponse.setAccountId(proj.getAccountUuid());
        projectAccountResponse.setAccountName(proj.getAccountName());
        projectAccountResponse.setAccountType(proj.getAccountType());
        projectAccountResponse.setRole(proj.getAccountRole().toString());
        projectAccountResponse.setDomainId(proj.getDomainUuid());
        projectAccountResponse.setDomainName(proj.getDomainName());

        projectAccountResponse.setObjectName("projectaccount");

        return projectAccountResponse;
    }




    @Override
    public ProjectAccountJoinVO newProjectAccountView(ProjectAccount proj) {
        SearchCriteria<ProjectAccountJoinVO> sc = paIdSearch.create();
        sc.setParameters("accountId", proj.getAccountId());
        sc.setParameters("projectId", proj.getProjectId());
        List<ProjectAccountJoinVO> grps = searchIncludingRemoved(sc, null, null, false);
        assert grps != null && grps.size() == 1 : "No project account found for account id = " + proj.getAccountId() + " and project id = " + proj.getProjectId();
        return grps.get(0);
    }


}