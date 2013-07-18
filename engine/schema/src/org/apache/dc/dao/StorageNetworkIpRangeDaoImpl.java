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
package org.apache.dc.dao;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.dc.StorageNetworkIpRangeVO;
import org.apache.utils.db.DB;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.GenericSearchBuilder;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.SearchCriteria2;
import org.apache.utils.db.SearchCriteriaService;
import org.apache.utils.db.SearchCriteria.Func;
import org.apache.utils.db.SearchCriteria.Op;
import org.springframework.stereotype.Component;

@Component
@Local(value={StorageNetworkIpRangeDao.class})
@DB(txn=false)
public class StorageNetworkIpRangeDaoImpl extends GenericDaoBase<StorageNetworkIpRangeVO, Long> implements StorageNetworkIpRangeDao {
	protected final GenericSearchBuilder<StorageNetworkIpRangeVO, Long> countRanges;
	
	protected StorageNetworkIpRangeDaoImpl() {
		countRanges = createSearchBuilder(Long.class);
		countRanges.select(null, Func.COUNT, null);
		countRanges.done();
	}
	
	@Override
    public List<StorageNetworkIpRangeVO> listByPodId(long podId) {
		SearchCriteriaService<StorageNetworkIpRangeVO, StorageNetworkIpRangeVO> sc = SearchCriteria2.create(StorageNetworkIpRangeVO.class);
	    sc.addAnd(sc.getEntity().getPodId(), Op.EQ, podId);
		return sc.list();
    }

	@Override
    public List<StorageNetworkIpRangeVO> listByRangeId(long rangeId) {
		SearchCriteriaService<StorageNetworkIpRangeVO, StorageNetworkIpRangeVO> sc = SearchCriteria2.create(StorageNetworkIpRangeVO.class);
	    sc.addAnd(sc.getEntity().getId(), Op.EQ, rangeId);
		return sc.list();
    }

	@Override
    public List<StorageNetworkIpRangeVO> listByDataCenterId(long dcId) {
		SearchCriteriaService<StorageNetworkIpRangeVO, StorageNetworkIpRangeVO> sc = SearchCriteria2.create(StorageNetworkIpRangeVO.class);
	    sc.addAnd(sc.getEntity().getDataCenterId(), Op.EQ, dcId);
		return sc.list();
    }
	
	@Override
	public long countRanges() {
		SearchCriteria<Long> sc = countRanges.create();
		return customSearch(sc, null).get(0);
	}

}
