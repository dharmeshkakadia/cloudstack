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

import org.apache.bridge.model.MultipartMetaVO;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value={MultipartMetaDao.class})
public class MultipartMetaDaoImpl extends GenericDaoBase<MultipartMetaVO, Long> implements MultipartMetaDao {
    
    @Override
    public List<MultipartMetaVO> getByUploadID (long uploadID) {
        SearchBuilder <MultipartMetaVO> searchByUID = createSearchBuilder();
        searchByUID.and("UploadID", searchByUID.entity().getUploadID(), SearchCriteria.Op.EQ);
        searchByUID.done();
        Transaction txn = Transaction.open(Transaction.AWSAPI_DB);
        try {
            txn.start();
            SearchCriteria<MultipartMetaVO> sc = searchByUID.create();
            sc.setParameters("UploadID", uploadID);
            return  listBy(sc);
        
        }finally {
            txn.close();
        }
        
    }
}
