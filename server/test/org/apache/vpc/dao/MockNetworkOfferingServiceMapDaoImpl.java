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
package org.apache.vpc.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;

import org.apache.network.Network.Service;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.offerings.dao.NetworkOfferingServiceMapDaoImpl;
import org.apache.utils.db.DB;
import org.apache.utils.db.SearchCriteria;

@Local(value = NetworkOfferingServiceMapDao.class)
@DB(txn = false)
public class MockNetworkOfferingServiceMapDaoImpl extends NetworkOfferingServiceMapDaoImpl{
    
    @Override
    public boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service... services) {
        if (services.length > 0 && services[0] == Service.SourceNat && networkOfferingId != 2) {
            return true;
        } else if (services.length > 0 && services[0] == Service.Lb && networkOfferingId == 6) {
            return true;
        }
        return false;
    }
    
}