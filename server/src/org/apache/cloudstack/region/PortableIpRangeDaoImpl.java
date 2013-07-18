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
package org.apache.cloudstack.region;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.dc.AccountVlanMapVO;
import org.apache.dc.PodVlanMapVO;
import org.apache.dc.Vlan;
import org.apache.dc.VlanVO;
import org.apache.dc.Vlan.VlanType;
import org.apache.network.dao.IPAddressDao;
import org.apache.utils.Pair;
import org.apache.utils.db.DB;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.JoinBuilder;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.apache.utils.exception.CloudRuntimeException;
import org.springframework.stereotype.Component;

@Component
@Local(value={PortableIpRangeDao.class})
public class PortableIpRangeDaoImpl extends GenericDaoBase<PortableIpRangeVO, Long> implements PortableIpRangeDao {

    private final SearchBuilder<PortableIpRangeVO> listByRegionIDSearch;

    public PortableIpRangeDaoImpl() {
        listByRegionIDSearch = createSearchBuilder();
        listByRegionIDSearch.and("regionId", listByRegionIDSearch.entity().getRegionId(), SearchCriteria.Op.EQ);
        listByRegionIDSearch.done();
    }

    @Override
    public List<PortableIpRangeVO> listByRegionId(int regionIdId) {
        SearchCriteria<PortableIpRangeVO> sc = listByRegionIDSearch.create();
        sc.setParameters("regionId", regionIdId);
        return listBy(sc);
    }
}
