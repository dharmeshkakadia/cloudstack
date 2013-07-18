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
package org.apache.projects.dao;

import java.util.List;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.projects.Project;
import org.apache.projects.ProjectVO;
import org.apache.server.ResourceTag.TaggedResourceType;
import org.apache.tags.dao.ResourceTagDao;
import org.apache.tags.dao.ResourceTagsDaoImpl;
import org.apache.utils.db.DB;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.GenericSearchBuilder;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.apache.utils.db.SearchCriteria.Func;
import org.springframework.stereotype.Component;

@Component
@Local(value = { ProjectDao.class })
public class ProjectDaoImpl extends GenericDaoBase<ProjectVO, Long> implements ProjectDao {
    private static final Logger s_logger = Logger.getLogger(ProjectDaoImpl.class);
    protected final SearchBuilder<ProjectVO> AllFieldsSearch;
    protected GenericSearchBuilder<ProjectVO, Long> CountByDomain;
    protected GenericSearchBuilder<ProjectVO, Long> ProjectAccountSearch;
    // ResourceTagsDaoImpl _tagsDao = ComponentLocator.inject(ResourceTagsDaoImpl.class);
    @Inject ResourceTagDao _tagsDao;

    protected ProjectDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("domainId", AllFieldsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectAccountId", AllFieldsSearch.entity().getProjectAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        CountByDomain = createSearchBuilder(Long.class);
        CountByDomain.select(null, Func.COUNT, null);
        CountByDomain.and("domainId", CountByDomain.entity().getDomainId(), SearchCriteria.Op.EQ);
        CountByDomain.done();
    }

    @Override
    public ProjectVO findByNameAndDomain(String name, long domainId) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("name", name);
        sc.setParameters("domainId", domainId);

        return findOneBy(sc);
    }

    @Override
    @DB
    public boolean remove(Long projectId) {
        boolean result = false;
        Transaction txn = Transaction.currentTxn();
        txn.start();
        ProjectVO projectToRemove = findById(projectId);
        projectToRemove.setName(null);
        if (!update(projectId, projectToRemove)) {
            s_logger.warn("Failed to reset name for the project id=" + projectId + " as a part of project remove");
            return false;
        } 
        
        _tagsDao.removeByIdAndType(projectId, TaggedResourceType.Project);
        result = super.remove(projectId);
        txn.commit();

        return result;

    }

    @Override
    public Long countProjectsForDomain(long domainId) {
        SearchCriteria<Long> sc = CountByDomain.create();
        sc.setParameters("domainId", domainId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public ProjectVO findByProjectAccountId(long projectAccountId) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectAccountId", projectAccountId);

        return findOneBy(sc);
    }

    @Override
    public List<ProjectVO> listByState(Project.State state) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }
    
    @Override
    public ProjectVO findByProjectAccountIdIncludingRemoved(long projectAccountId) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectAccountId", projectAccountId);

        return findOneIncludingRemovedBy(sc);
    }
}
