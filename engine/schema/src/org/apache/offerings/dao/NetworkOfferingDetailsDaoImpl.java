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
package org.apache.offerings.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.offering.NetworkOffering;
import org.apache.offering.NetworkOffering.Detail;
import org.apache.offerings.NetworkOfferingDetailsVO;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.GenericSearchBuilder;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.SearchCriteria.Func;
import org.apache.utils.db.SearchCriteria.Op;

public class NetworkOfferingDetailsDaoImpl extends GenericDaoBase<NetworkOfferingDetailsVO, Long> implements NetworkOfferingDetailsDao{
    protected final SearchBuilder<NetworkOfferingDetailsVO> DetailSearch;
    private final GenericSearchBuilder<NetworkOfferingDetailsVO, String> ValueSearch;

    
    public NetworkOfferingDetailsDaoImpl() {
        
        DetailSearch = createSearchBuilder();
        DetailSearch.and("offeringId", DetailSearch.entity().getOfferingId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();
        
        ValueSearch = createSearchBuilder(String.class);
        ValueSearch.select(null, Func.DISTINCT, ValueSearch.entity().getValue());
        ValueSearch.and("offeringId", ValueSearch.entity().getOfferingId(), SearchCriteria.Op.EQ);
        ValueSearch.and("name", ValueSearch.entity().getName(), Op.EQ);
        ValueSearch.done();
    }
    
    @Override
    public Map<NetworkOffering.Detail,String> getNtwkOffDetails(long offeringId) {
        SearchCriteria<NetworkOfferingDetailsVO> sc = DetailSearch.create();
        sc.setParameters("offeringId", offeringId);
        
        List<NetworkOfferingDetailsVO> results = search(sc, null);
        Map<NetworkOffering.Detail, String> details = new HashMap<NetworkOffering.Detail, String>(results.size());
        for (NetworkOfferingDetailsVO result : results) {
            details.put(result.getName(), result.getValue());
        }
        
        return details;
    }

    @Override
    public String getDetail(long offeringId, Detail detailName) {
        SearchCriteria<String> sc = ValueSearch.create();
        sc.setParameters("name", detailName);
        sc.setParameters("offeringId", offeringId);
        List<String> results = customSearch(sc, null);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

}
