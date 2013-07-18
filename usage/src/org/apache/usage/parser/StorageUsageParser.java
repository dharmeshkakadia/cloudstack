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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.usage.StorageTypes;
import org.apache.usage.UsageServer;
import org.apache.usage.UsageStorageVO;
import org.apache.usage.UsageVO;
import org.apache.usage.dao.UsageDao;
import org.apache.usage.dao.UsageStorageDao;
import org.apache.user.AccountVO;
import org.apache.utils.Pair;
import org.apache.cloudstack.usage.UsageTypes;
import org.springframework.stereotype.Component;

@Component
public class StorageUsageParser {
    public static final Logger s_logger = Logger.getLogger(StorageUsageParser.class.getName());
    
    private static UsageDao m_usageDao;
    private static UsageStorageDao m_usageStorageDao;

    @Inject private UsageDao _usageDao;
    @Inject private UsageStorageDao _usageStorageDao;
 
    @PostConstruct
    void init() {
    	m_usageDao = _usageDao;
    	m_usageStorageDao = _usageStorageDao;
    }
    
    public static boolean parse(AccountVO account, Date startDate, Date endDate) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Parsing all Storage usage events for account: " + account.getId());
        }
        if ((endDate == null) || endDate.after(new Date())) {
            endDate = new Date();
        }

        // - query usage_volume table with the following criteria:
        //     - look for an entry for accountId with start date in the given range
        //     - look for an entry for accountId with end date in the given range
        //     - look for an entry for accountId with end date null (currently running vm or owned IP)
        //     - look for an entry for accountId with start date before given range *and* end date after given range
        List<UsageStorageVO> usageUsageStorages = m_usageStorageDao.getUsageRecords(account.getId(), account.getDomainId(), startDate, endDate, false, 0);
        
        if(usageUsageStorages.isEmpty()){
            s_logger.debug("No Storage usage events for this period");
            return true;
        }

        // This map has both the running time *and* the usage amount.
        Map<String, Pair<Long, Long>> usageMap = new HashMap<String, Pair<Long, Long>>();

        Map<String, StorageInfo> storageMap = new HashMap<String, StorageInfo>();

        // loop through all the usage volumes, create a usage record for each
        for (UsageStorageVO usageStorage : usageUsageStorages) {
            long storageId = usageStorage.getId();
            int storage_type = usageStorage.getStorageType();
            long size = usageStorage.getSize();
            long zoneId = usageStorage.getZoneId();
            Long sourceId = usageStorage.getSourceId();
            
            String key = ""+storageId+"Z"+zoneId+"T"+storage_type;

         // store the info in the storage map
            storageMap.put(key, new StorageInfo(zoneId, storageId, storage_type, sourceId, size));
            
            Date storageCreateDate = usageStorage.getCreated();
            Date storageDeleteDate = usageStorage.getDeleted();

            if ((storageDeleteDate == null) || storageDeleteDate.after(endDate)) {
                storageDeleteDate = endDate;
            }

            // clip the start date to the beginning of our aggregation range if the vm has been running for a while
            if (storageCreateDate.before(startDate)) {
                storageCreateDate = startDate;
            }

            long currentDuration = (storageDeleteDate.getTime() - storageCreateDate.getTime()) + 1; // make sure this is an inclusive check for milliseconds (i.e. use n - m + 1 to find total number of millis to charge)
            
            updateStorageUsageData(usageMap, key, usageStorage.getId(), currentDuration);
        }

        for (String storageIdKey : usageMap.keySet()) {
            Pair<Long, Long> storagetimeInfo = usageMap.get(storageIdKey);
            long useTime = storagetimeInfo.second().longValue();

            // Only create a usage record if we have a runningTime of bigger than zero.
            if (useTime > 0L) {
                StorageInfo info = storageMap.get(storageIdKey);
                createUsageRecord(info.getZoneId(), info.getStorageType(), useTime, startDate, endDate, account, info.getStorageId(), info.getSourceId(), info.getSize());
            }
        }

        return true;
    }

    private static void updateStorageUsageData(Map<String, Pair<Long, Long>> usageDataMap, String key, long storageId, long duration) {
        Pair<Long, Long> volUsageInfo = usageDataMap.get(key);
        if (volUsageInfo == null) {
            volUsageInfo = new Pair<Long, Long>(new Long(storageId), new Long(duration));
        } else {
            Long runningTime = volUsageInfo.second();
            runningTime = new Long(runningTime.longValue() + duration);
            volUsageInfo = new Pair<Long, Long>(volUsageInfo.first(), runningTime);
        }
        usageDataMap.put(key, volUsageInfo);
    }

    private static void createUsageRecord(long zoneId, int type, long runningTime, Date startDate, Date endDate, AccountVO account, long storageId, Long sourceId, long size) {
        // Our smallest increment is hourly for now
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total running time " + runningTime + "ms");
        }

        float usage = runningTime / 1000f / 60f / 60f;

        DecimalFormat dFormat = new DecimalFormat("#.######");
        String usageDisplay = dFormat.format(usage);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating Storage usage record for type: "+ type + " with id: " + storageId + ", usage: " + usageDisplay + ", startDate: " + startDate + ", endDate: " + endDate + ", for account: " + account.getId());
        }
        
        String usageDesc = "";
        Long tmplSourceId = null;
        
        int usage_type = 0;
        switch(type){
            case StorageTypes.TEMPLATE: 
                usage_type = UsageTypes.TEMPLATE;
                usageDesc += "Template ";
                tmplSourceId = sourceId;
                break;
            case StorageTypes.ISO: 
                usage_type = UsageTypes.ISO;
                usageDesc += "ISO ";
                break;
            case StorageTypes.SNAPSHOT: 
                usage_type = UsageTypes.SNAPSHOT;
                usageDesc += "Snapshot ";
                break;                        
        }
        // Create the usage record
        usageDesc += "Id:"+storageId+" Size:"+size;

        //ToDo: get zone id
        UsageVO usageRecord = new UsageVO(zoneId, account.getId(), account.getDomainId(), usageDesc, usageDisplay + " Hrs", usage_type,
                new Double(usage), null, null, null, tmplSourceId, storageId, size, startDate, endDate);
        m_usageDao.persist(usageRecord);
    }

    private static class StorageInfo {
        private long zoneId;
        private long storageId;
        private int storageType;
        private Long sourceId;
        private long size;

        public StorageInfo(long zoneId, long storageId, int storageType, Long sourceId, long size) {
            this.zoneId = zoneId;
            this.storageId = storageId;
            this.storageType = storageType;
            this.sourceId = sourceId;
            this.size = size;
        }

        public long getZoneId() {
            return zoneId;
        }
        
        public long getStorageId() {
            return storageId;
        }

        public int getStorageType() {
            return storageType;
        }

        public long getSourceId() {
            return sourceId;
        }

        
        public long getSize() {
            return size;
        }        
    }
}