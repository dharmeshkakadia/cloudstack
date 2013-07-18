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

import java.util.List;

import javax.ejb.Local;

import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.dao.NetworkServiceMapDao;
import org.apache.network.dao.NetworkServiceMapVO;
import org.apache.utils.db.DB;
import org.apache.utils.db.GenericDaoBase;

@Local(value = NetworkServiceMapDao.class)
@DB(txn = false)
public class MockNetworkServiceMapDaoImpl extends GenericDaoBase<NetworkServiceMapVO, Long> implements NetworkServiceMapDao{

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#areServicesSupportedInNetwork(long, org.apache.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedInNetwork(long networkId, Service... services) {
        if (services.length > 0 && services[0] == Service.Lb) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#canProviderSupportServiceInNetwork(long, org.apache.network.Network.Service, org.apache.network.Network.Provider)
     */
    @Override
    public boolean canProviderSupportServiceInNetwork(long networkId, Service service, Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#getServicesInNetwork(long)
     */
    @Override
    public List<NetworkServiceMapVO> getServicesInNetwork(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#getProviderForServiceInNetwork(long, org.apache.network.Network.Service)
     */
    @Override
    public String getProviderForServiceInNetwork(long networkid, Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#deleteByNetworkId(long)
     */
    @Override
    public void deleteByNetworkId(long networkId) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#getDistinctProviders(long)
     */
    @Override
    public List<String> getDistinctProviders(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.dao.NetworkServiceMapDao#isProviderForNetwork(long, org.apache.network.Network.Provider)
     */
    @Override
    public String isProviderForNetwork(long networkId, Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getProvidersForServiceInNetwork(long networkId, Service service) {
        // TODO Auto-generated method stub
        return null;
    }
}
