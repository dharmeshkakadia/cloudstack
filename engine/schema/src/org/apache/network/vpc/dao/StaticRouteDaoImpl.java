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
package org.apache.network.vpc.dao;

import java.util.List;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.network.vpc.StaticRoute;
import org.apache.network.vpc.StaticRouteVO;
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
import org.apache.utils.db.SearchCriteria.Op;
import org.springframework.stereotype.Component;

@Component
@Local(value = StaticRouteDao.class)
@DB(txn = false)
public class StaticRouteDaoImpl extends GenericDaoBase<StaticRouteVO, Long> implements StaticRouteDao{
    protected final SearchBuilder<StaticRouteVO> AllFieldsSearch;
    protected final SearchBuilder<StaticRouteVO> NotRevokedSearch;
    protected final GenericSearchBuilder<StaticRouteVO, Long> RoutesByGatewayCount;
    @Inject ResourceTagDao _tagsDao;
    
    protected StaticRouteDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("gatewayId", AllFieldsSearch.entity().getVpcGatewayId(), Op.EQ);
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.done();
        
        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("gatewayId", NotRevokedSearch.entity().getVpcGatewayId(), Op.EQ);
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), Op.NEQ);
        NotRevokedSearch.done();
        
        RoutesByGatewayCount = createSearchBuilder(Long.class);
        RoutesByGatewayCount.select(null, Func.COUNT, RoutesByGatewayCount.entity().getId());
        RoutesByGatewayCount.and("gatewayId", RoutesByGatewayCount.entity().getVpcGatewayId(), Op.EQ);
        RoutesByGatewayCount.done();
    }

    
    @Override
    public boolean setStateToAdd(StaticRouteVO rule) {
        SearchCriteria<StaticRouteVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", rule.getId());
        sc.setParameters("state", StaticRoute.State.Staged);

        rule.setState(StaticRoute.State.Add);

        return update(rule, sc) > 0;
    }


    @Override
    public List<? extends StaticRoute> listByGatewayIdAndNotRevoked(long gatewayId) {
        SearchCriteria<StaticRouteVO> sc = NotRevokedSearch.create();
        sc.setParameters("gatewayId", gatewayId);
        sc.setParameters("state", StaticRoute.State.Revoke);
        return listBy(sc);
    }

    @Override
    public List<StaticRouteVO> listByVpcId(long vpcId) {
        SearchCriteria<StaticRouteVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        return listBy(sc);
    }

    @Override
    public long countRoutesByGateway(long gatewayId) {
        SearchCriteria<Long> sc = RoutesByGatewayCount.create();
        sc.setParameters("gatewayId", gatewayId);
        return customSearch(sc, null).get(0);
    }
    
    @Override
    @DB
    public boolean remove(Long id) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        StaticRouteVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, TaggedResourceType.StaticRoute);
        }
        boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
