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

package org.apache.network.as.dao;

import java.util.List;

import javax.ejb.Local;

import org.apache.network.as.CounterVO;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.SearchCriteria.Op;
import org.springframework.stereotype.Component;

@Component
@Local(value = CounterDao.class)
public class CounterDaoImpl extends GenericDaoBase<CounterVO, Long> implements CounterDao {
    final SearchBuilder<CounterVO> AllFieldsSearch;

    protected CounterDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), Op.LIKE);
        AllFieldsSearch.and("source", AllFieldsSearch.entity().getSource(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public List<CounterVO> listCounters(Long id, String name, String source, String keyword, Filter filter) {
        SearchCriteria<CounterVO> sc = AllFieldsSearch.create();

        if (keyword != null) {
            SearchCriteria<CounterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (name != null) {
            sc.addAnd("name", SearchCriteria.Op.LIKE, "%" + name + "%");
        }

        if (id != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, id);
        }

        if (source != null) {
            sc.addAnd("source", SearchCriteria.Op.EQ, source);
        }
        return listBy(sc, filter);
    }

}