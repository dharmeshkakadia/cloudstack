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
package org.apache.network.dao;

import java.util.List;

import org.apache.utils.db.GenericDao;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.GenericSearchBuilder;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.SearchCriteria.Op;
import org.springframework.stereotype.Component;

@Component
public class PhysicalNetworkIsolationMethodDaoImpl extends GenericDaoBase<PhysicalNetworkIsolationMethodVO, Long> implements GenericDao<PhysicalNetworkIsolationMethodVO, Long> {
    private final GenericSearchBuilder<PhysicalNetworkIsolationMethodVO, String> IsolationMethodSearch;
    private final SearchBuilder<PhysicalNetworkIsolationMethodVO> AllFieldsSearch;

    protected PhysicalNetworkIsolationMethodDaoImpl() {
        super();
        IsolationMethodSearch = createSearchBuilder(String.class);
        IsolationMethodSearch.selectField(IsolationMethodSearch.entity().getIsolationMethod());
        IsolationMethodSearch.and("physicalNetworkId", IsolationMethodSearch.entity().getPhysicalNetworkId(), Op.EQ);
        IsolationMethodSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.and("isolationMethod", AllFieldsSearch.entity().getIsolationMethod(), Op.EQ);
        AllFieldsSearch.done();
    }

    public List<String> getAllIsolationMethod(long physicalNetworkId) {
        SearchCriteria<String> sc = IsolationMethodSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return customSearch(sc, null);
    }

    public String getIsolationMethod(long physicalNetworkId) {
        SearchCriteria<String> sc = IsolationMethodSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return customSearch(sc, null).get(0);
    }
    
    public int clearIsolationMethods(long physicalNetworkId) {
        SearchCriteria<PhysicalNetworkIsolationMethodVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return remove(sc);
    }

}
