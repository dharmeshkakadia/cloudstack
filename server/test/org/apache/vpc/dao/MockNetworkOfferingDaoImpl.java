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

import java.lang.reflect.Field;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.Network.GuestType;
import org.apache.network.Networks.TrafficType;
import org.apache.offering.NetworkOffering;
import org.apache.offering.NetworkOffering.Availability;
import org.apache.offering.NetworkOffering.State;
import org.apache.offerings.NetworkOfferingVO;
import org.apache.offerings.dao.NetworkOfferingDao;
import org.apache.offerings.dao.NetworkOfferingDaoImpl;
import org.apache.utils.db.DB;
import org.apache.utils.db.GenericDaoBase;

@Local(value = NetworkOfferingDao.class)
@DB(txn = false)
public class MockNetworkOfferingDaoImpl extends NetworkOfferingDaoImpl implements NetworkOfferingDao{
    private static final Logger s_logger = Logger.getLogger(MockNetworkOfferingDaoImpl.class);

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#findByUniqueName(java.lang.String)
     */
    @Override
    public NetworkOfferingVO findByUniqueName(String uniqueName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#persistDefaultNetworkOffering(org.apache.offerings.NetworkOfferingVO)
     */
    @Override
    public NetworkOfferingVO persistDefaultNetworkOffering(NetworkOfferingVO offering) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#listSystemNetworkOfferings()
     */
    @Override
    public List<NetworkOfferingVO> listSystemNetworkOfferings() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#listByAvailability(org.apache.offering.NetworkOffering.Availability, boolean)
     */
    @Override
    public List<NetworkOfferingVO> listByAvailability(Availability availability, boolean isSystem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#getOfferingIdsToUpgradeFrom(org.apache.offering.NetworkOffering)
     */
    @Override
    public List<Long> getOfferingIdsToUpgradeFrom(NetworkOffering originalOffering) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.offerings.dao.NetworkOfferingDao#listByTrafficTypeGuestTypeAndState(org.apache.offering.NetworkOffering.State, org.apache.network.Networks.TrafficType, org.apache.network.Network.GuestType)
     */
    @Override
    public List<NetworkOfferingVO> listByTrafficTypeGuestTypeAndState(State state, TrafficType trafficType, GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    @Override
    public NetworkOfferingVO findById(Long id) {
        NetworkOfferingVO vo = null;
        if (id.longValue() == 1) {
            //network offering valid for vpc
            vo = new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Isolated, false, false, false, false, false);
        } else if (id.longValue() == 2) {
            //invalid offering - source nat is not included
            vo = new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Isolated, false, false, false, false, false);
        } else if (id.longValue() == 3) {
            //network offering invalid for vpc (conserve mode off)
            vo = new NetworkOfferingVO("non vpc", "non vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Isolated, true, false, false, false, false);
        } else if (id.longValue() == 4) {
            //network offering invalid for vpc (Shared)
            vo = new NetworkOfferingVO("non vpc", "non vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Shared, false, false, false, false, false);
        } else if (id.longValue() == 5) {
            //network offering invalid for vpc (has redundant router)
            vo = new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Isolated, false, false, false, false, false);
            vo.setRedundantRouter(true);
        } else if (id.longValue() == 6) {
            //network offering invalid for vpc (has lb service)   
            vo = new NetworkOfferingVO("vpc", "vpc", TrafficType.Guest, false, true, null, null, false,
                    Availability.Optional, null, Network.GuestType.Isolated, false, false, false, false, false);
        }
        
        if (vo != null) {
            vo = setId(vo, id);
        }
        
        return vo;
    }
    
    private NetworkOfferingVO setId(NetworkOfferingVO vo, long id) {
        NetworkOfferingVO voToReturn = vo;
        Class<?> c = voToReturn.getClass();
        try {
            Field f = c.getDeclaredField("id");
            f.setAccessible(true);
            f.setLong(voToReturn, id);
        } catch (NoSuchFieldException ex) {
           s_logger.warn(ex);
           return null;
        } catch (IllegalAccessException ex) {
            s_logger.warn(ex);
            return null;
        }
        
        return voToReturn;
    }

}
