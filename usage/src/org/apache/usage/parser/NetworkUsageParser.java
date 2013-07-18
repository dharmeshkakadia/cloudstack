// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.usage.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.usage.UsageNetworkVO;
import org.apache.usage.UsageVO;
import org.apache.usage.dao.UsageDao;
import org.apache.usage.dao.UsageNetworkDao;
import org.apache.user.AccountVO;
import org.apache.utils.db.SearchCriteria;
import org.apache.cloudstack.usage.UsageTypes;
import org.springframework.stereotype.Component;

@Component
public class NetworkUsageParser {
public static final Logger s_logger = Logger.getLogger(NetworkUsageParser.class.getName());

    private static UsageDao m_usageDao;
    private static UsageNetworkDao m_usageNetworkDao;

    @Inject private UsageDao _usageDao;
    @Inject private UsageNetworkDao _usageNetworkDao;

    @PostConstruct
    void init() {
    	m_usageDao = _usageDao;
    	m_usageNetworkDao = _usageNetworkDao;
    }
    
    public static boolean parse(AccountVO account, Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Network usage events for account: " + account.getId());
        }

        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_network table for all entries for userId with
        // event_date in the given range
        SearchCriteria<UsageNetworkVO> sc = m_usageNetworkDao.createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, account.getId());
        sc.addAnd("eventTimeMillis", SearchCriteria.Op.BETWEEN, startDate.getTime(), endDate.getTime());
        List<UsageNetworkVO> usageNetworkVOs = m_usageNetworkDao.search(sc, null);

        Map<String, NetworkInfo> networkUsageByZone = new HashMap<String, NetworkInfo>();

        // Calculate the total bytes since last parsing
        for (UsageNetworkVO usageNetwork : usageNetworkVOs) {
            long zoneId = usageNetwork.getZoneId();
            String key = ""+zoneId;
            if(usageNetwork.getHostId() != 0){
                key += "-Host"+usageNetwork.getHostId();
            }
            NetworkInfo networkInfo = networkUsageByZone.get(key);

            long bytesSent = usageNetwork.getBytesSent();
            long bytesReceived = usageNetwork.getBytesReceived();
            if (networkInfo != null) {
                bytesSent += networkInfo.getBytesSent();
                bytesReceived += networkInfo.getBytesRcvd();
            }

            networkUsageByZone.put(key, new NetworkInfo(zoneId, usageNetwork.getHostId(), usageNetwork.getHostType(), usageNetwork.getNetworkId(), bytesSent, bytesReceived));
        }

        List<UsageVO> usageRecords = new ArrayList<UsageVO>();
        for (String key : networkUsageByZone.keySet()) {
            NetworkInfo networkInfo = networkUsageByZone.get(key);
            long totalBytesSent = networkInfo.getBytesSent();
            long totalBytesReceived = networkInfo.getBytesRcvd();

            if ((totalBytesSent > 0L) || (totalBytesReceived > 0L)) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Creating usage record, total bytes sent:" + totalBytesSent + ", total bytes received: " + totalBytesReceived + " for account: "
                            + account.getId() + " in availability zone " + networkInfo.getZoneId() + ", start: " + startDate + ", end: " + endDate);
                }

                Long hostId = null;
                
                // Create the usage record for bytes sent
                String usageDesc = "network bytes sent";
                if(networkInfo.getHostId() != 0){
                    hostId = networkInfo.getHostId();
                    usageDesc += " for Host: "+networkInfo.getHostId(); 
                }
                UsageVO usageRecord = new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesSent + " bytes sent",
                        UsageTypes.NETWORK_BYTES_SENT, new Double(totalBytesSent), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate, endDate);
                usageRecords.add(usageRecord);

                // Create the usage record for bytes received
                usageDesc = "network bytes received";
                if(networkInfo.getHostId() != 0){
                    usageDesc += " for Host: "+networkInfo.getHostId(); 
                }
                usageRecord = new UsageVO(networkInfo.getZoneId(), account.getId(), account.getDomainId(), usageDesc, totalBytesReceived + " bytes received",
                        UsageTypes.NETWORK_BYTES_RECEIVED, new Double(totalBytesReceived), hostId, networkInfo.getHostType(), networkInfo.getNetworkId(), startDate, endDate);
                usageRecords.add(usageRecord);
            } else {
                // Don't charge anything if there were zero bytes processed
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No usage record (0 bytes used) generated for account: " + account.getId());
                }
            }
        }
        try {
            m_usageDao.saveUsageRecords(usageRecords);
        } catch (Exception ex) {
            s_logger.error("Exception in usage manager", ex);
        }

        return true;
    }
    
    private static class NetworkInfo {
        private long zoneId;
        private long hostId;
        private String hostType;
        private Long networkId;
        private long bytesSent;
        private long bytesRcvd;

        public NetworkInfo(long zoneId, long hostId, String hostType, Long networkId, long bytesSent, long bytesRcvd) {
            this.zoneId = zoneId;
            this.hostId = hostId;
            this.hostType = hostType;
            this.networkId = networkId;
            this.bytesSent = bytesSent;
            this.bytesRcvd = bytesRcvd;
        }

        public long getZoneId() {
            return zoneId;
        }

        public long getHostId() {
            return hostId;
        }
        
        public Long getNetworkId() {
            return networkId;
        }

        public long getBytesSent() {
            return bytesSent;
        }

        public long getBytesRcvd() {
            return bytesRcvd;
        }
        
        public String getHostType(){
            return hostType;
        }
    
    }
}