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
package org.apache.bridge.persist.dao;

import java.util.List;

import javax.ejb.Local;

import org.apache.bridge.model.SBucket;
import org.apache.bridge.model.SBucketVO;
import org.apache.utils.db.Filter;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value={SBucketDao.class})
public class SBucketDaoImpl extends GenericDaoBase<SBucketVO, Long> implements SBucketDao {
    	
	public SBucketDaoImpl() {
	}

	@Override
	public SBucketVO getByName(String bucketName) {
	    SearchBuilder<SBucketVO> SearchByName = createSearchBuilder();
	    SearchByName.and("Name", SearchByName.entity().getName(), SearchCriteria.Op.EQ);
	    //Transaction txn = Transaction.open(Transaction.AWSAPI_DB);
	    Transaction txn = Transaction.open("cloudbridge", Transaction.AWSAPI_DB, true);
	    try {
		txn.start();
		SearchCriteria<SBucketVO> sc = SearchByName.create();
		sc.setParameters("Name", bucketName);
		return findOneBy(sc);
		
	    }finally {
		txn.close();
	    }
	}
	
	@Override
	public List<SBucketVO> listBuckets(String canonicalId) {
	    SearchBuilder<SBucketVO> ByCanonicalID = createSearchBuilder();
	    ByCanonicalID.and("OwnerCanonicalID", ByCanonicalID.entity().getOwnerCanonicalId(), SearchCriteria.Op.EQ);
	    Filter filter = new Filter(SBucketVO.class, "createTime", Boolean.TRUE, null, null);
	    Transaction txn = Transaction.currentTxn();  // Transaction.open("cloudbridge", Transaction.AWSAPI_DB, true);
	    try {
            txn.start();
            SearchCriteria<SBucketVO> sc = ByCanonicalID.create();
            sc.setParameters("OwnerCanonicalID", canonicalId);
		return listBy(sc, filter);
	    }finally {
            txn.close();
	    }

	}
	

}
