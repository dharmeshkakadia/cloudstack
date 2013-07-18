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
package org.apache.usage.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.apache.usage.UsagePortForwardingRuleVO;
import org.apache.utils.DateUtil;
import org.apache.utils.db.GenericDaoBase;
import org.apache.utils.db.Transaction;
import org.springframework.stereotype.Component;

@Component
@Local(value={UsagePortForwardingRuleDao.class})
public class UsagePortForwardingRuleDaoImpl extends GenericDaoBase<UsagePortForwardingRuleVO, Long> implements UsagePortForwardingRuleDao {
	public static final Logger s_logger = Logger.getLogger(UsagePortForwardingRuleDaoImpl.class.getName());

	protected static final String REMOVE_BY_USERID_PFID = "DELETE FROM usage_port_forwarding WHERE account_id = ? AND id = ?";
	protected static final String UPDATE_DELETED = "UPDATE usage_port_forwarding SET deleted = ? WHERE account_id = ? AND id = ? and deleted IS NULL";
    protected static final String GET_USAGE_RECORDS_BY_ACCOUNT = "SELECT id, zone_id, account_id, domain_id, created, deleted " +
                                                                 "FROM usage_port_forwarding " +
                                                                 "WHERE account_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR " +
                                                                 "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_USAGE_RECORDS_BY_DOMAIN = "SELECT id, zone_id, account_id, domain_id, created, deleted " +
                                                                "FROM usage_port_forwarding " +
                                                                "WHERE domain_id = ? AND ((deleted IS NULL) OR (created BETWEEN ? AND ?) OR " +
                                                                "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?)))";
    protected static final String GET_ALL_USAGE_RECORDS = "SELECT id, zone_id, account_id, domain_id, created, deleted " +
                                                          "FROM usage_port_forwarding " +
                                                          "WHERE (deleted IS NULL) OR (created BETWEEN ? AND ?) OR " +
                                                          "      (deleted BETWEEN ? AND ?) OR ((created <= ?) AND (deleted >= ?))";

	public UsagePortForwardingRuleDaoImpl() {}

	public void removeBy(long accountId, long pfId) {
	    Transaction txn = Transaction.open(Transaction.USAGE_DB);
		PreparedStatement pstmt = null;
		try {
		    txn.start();
			String sql = REMOVE_BY_USERID_PFID;
			pstmt = txn.prepareAutoCloseStatement(sql);
			pstmt.setLong(1, accountId);
			pstmt.setLong(2, pfId);
			pstmt.executeUpdate();
			txn.commit();
		} catch (Exception e) {
			txn.rollback();
			s_logger.warn("Error removing UsagePortForwardingRuleVO", e);
		} finally {
		    txn.close();
		}
	}

	public void update(UsagePortForwardingRuleVO usage) {
	    Transaction txn = Transaction.open(Transaction.USAGE_DB);
		PreparedStatement pstmt = null;
		try {
		    txn.start();
			if (usage.getDeleted() != null) {
				pstmt = txn.prepareAutoCloseStatement(UPDATE_DELETED);
				pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), usage.getDeleted()));
				pstmt.setLong(2, usage.getAccountId());
				pstmt.setLong(3, usage.getId());
			}
			pstmt.executeUpdate();
			txn.commit();
		} catch (Exception e) {
			txn.rollback();
			s_logger.warn("Error updating UsagePortForwardingRuleVO", e);
		} finally {
		    txn.close();
		}
	}

    @Override
	public List<UsagePortForwardingRuleVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page) {
        List<UsagePortForwardingRuleVO> usageRecords = new ArrayList<UsagePortForwardingRuleVO>();

        Long param1 = null;
        String sql = null;
        if (accountId != null) {
            sql = GET_USAGE_RECORDS_BY_ACCOUNT;
            param1 = accountId;
        } else if (domainId != null) {
            sql = GET_USAGE_RECORDS_BY_DOMAIN;
            param1 = domainId;
        } else {
            sql = GET_ALL_USAGE_RECORDS;
        }

        if (limit) {
            int startIndex = 0;
            if (page > 0) {
                startIndex = 500 * (page-1);
            }
            sql += " LIMIT " + startIndex + ",500";
        }

        Transaction txn = Transaction.open(Transaction.USAGE_DB);
        PreparedStatement pstmt = null;

        try {
            int i = 1;
            pstmt = txn.prepareAutoCloseStatement(sql);
            if (param1 != null) {
                pstmt.setLong(i++, param1);
            }
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), startDate));
            pstmt.setString(i++, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                //id, zone_id, account_id, domain_id, created, deleted
            	Long pfId = Long.valueOf(rs.getLong(1));
            	Long zoneId = Long.valueOf(rs.getLong(2));
                Long acctId = Long.valueOf(rs.getLong(3));
                Long dId = Long.valueOf(rs.getLong(4));
                Date createdDate = null;
                Date deletedDate = null;
                String createdTS = rs.getString(5);
                String deletedTS = rs.getString(6);
                

                if (createdTS != null) {
                	createdDate = DateUtil.parseDateString(s_gmtTimeZone, createdTS);
                }
                if (deletedTS != null) {
                	deletedDate = DateUtil.parseDateString(s_gmtTimeZone, deletedTS);
                }

                usageRecords.add(new UsagePortForwardingRuleVO(pfId, zoneId, acctId, dId, createdDate, deletedDate));
            }
        } catch (Exception e) {
            txn.rollback();
            s_logger.warn("Error getting usage records", e);
        } finally {
            txn.close();
        }

        return usageRecords;
	}
}