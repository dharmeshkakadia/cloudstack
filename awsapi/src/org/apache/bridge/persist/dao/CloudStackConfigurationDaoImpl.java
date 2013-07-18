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

import javax.ejb.Local;

import org.apache.bridge.model.CloudStackConfigurationVO;
import org.apache.log4j.Logger;
import org.apache.utils.db.DB;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.SearchBuilder;
import org.apache.utils.db.SearchCriteria;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value={CloudStackConfigurationDao.class})
public class CloudStackConfigurationDaoImpl extends GenericDaoBase<CloudStackConfigurationVO, String> implements CloudStackConfigurationDao {
    private static final Logger s_logger = Logger.getLogger(CloudStackConfigurationDaoImpl.class);

    final SearchBuilder<CloudStackConfigurationVO> NameSearch= createSearchBuilder();

    public CloudStackConfigurationDaoImpl() { }


    @Override
    @DB
    public String getConfigValue(String name) {
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        Transaction txn = Transaction.open("cloud", Transaction.CLOUD_DB, true);
        try {
            txn.start();
            SearchCriteria<CloudStackConfigurationVO> sc = NameSearch.create();
            sc.setParameters("name", name);
            CloudStackConfigurationVO configItem = findOneBy(sc);
            if (configItem == null) {
                s_logger.warn("No configuration item found with name " + name);
                return null;
            }
            return configItem.getValue();
        }finally {
            txn.commit();
            txn.close();
        }
    }

}
