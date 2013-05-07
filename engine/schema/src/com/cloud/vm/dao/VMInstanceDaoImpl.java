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
package com.cloud.vm.dao;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.server.ResourceTag.TaggedResourceType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachine.Type;

@Component
@Local(value = { VMInstanceDao.class })
public class VMInstanceDaoImpl extends GenericDaoBase<VMInstanceVO, Long> implements VMInstanceDao {

    public static final Logger s_logger = Logger.getLogger(VMInstanceDaoImpl.class);

    protected SearchBuilder<VMInstanceVO> VMClusterSearch;
    protected SearchBuilder<VMInstanceVO> LHVMClusterSearch;
    protected SearchBuilder<VMInstanceVO> IdStatesSearch;
    protected SearchBuilder<VMInstanceVO> AllFieldsSearch;
    protected SearchBuilder<VMInstanceVO> ZoneTemplateNonExpungedSearch;
    protected SearchBuilder<VMInstanceVO> NameLikeSearch;
    protected SearchBuilder<VMInstanceVO> StateChangeSearch;
    protected SearchBuilder<VMInstanceVO> TransitionSearch;
    protected SearchBuilder<VMInstanceVO> TypesSearch;
    protected SearchBuilder<VMInstanceVO> IdTypesSearch;
    protected SearchBuilder<VMInstanceVO> HostIdTypesSearch;
    protected SearchBuilder<VMInstanceVO> HostIdUpTypesSearch;
    protected SearchBuilder<VMInstanceVO> HostUpSearch;
    protected SearchBuilder<VMInstanceVO> InstanceNameSearch;
    protected GenericSearchBuilder<VMInstanceVO, Long> CountVirtualRoutersByAccount;
    protected GenericSearchBuilder<VMInstanceVO, Long> CountRunningByHost;
    protected GenericSearchBuilder<VMInstanceVO, Long> CountRunningByAccount;
    protected SearchBuilder<VMInstanceVO> NetworkTypeSearch;
    protected GenericSearchBuilder<VMInstanceVO, String> DistinctHostNameSearch;
    
    @Inject ResourceTagDao _tagsDao;
    @Inject NicDao _nicDao;
    
    protected Attribute _updateTimeAttr;
    
    private static final String ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1 = 
            "SELECT host.cluster_id, SUM(IF(vm.state='Running' AND vm.account_id = ?, 1, 0)) FROM `cloud`.`host` host LEFT JOIN `cloud`.`vm_instance` vm ON host.id = vm.host_id WHERE ";
    private static final String ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2 =
            " AND host.type = 'Routing' GROUP BY host.cluster_id ORDER BY 2 ASC ";
    
    private static final String ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT = "SELECT pod.id, SUM(IF(vm.state='Running' AND vm.account_id = ?, 1, 0)) FROM `cloud`.`host_pod_ref` pod LEFT JOIN `cloud`.`vm_instance` vm ON pod.id = vm.pod_id WHERE pod.data_center_id = ? " +
                                                                       " GROUP BY pod.id ORDER BY 2 ASC ";
    
    private static final String ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT =
            "SELECT host.id, SUM(IF(vm.state='Running' AND vm.account_id = ?, 1, 0)) FROM `cloud`.`host` host LEFT JOIN `cloud`.`vm_instance` vm ON host.id = vm.host_id WHERE host.data_center_id = ? " +
    		                                                            " AND host.pod_id = ? AND host.cluster_id = ? AND host.type = 'Routing' " +
    		                                                            " GROUP BY host.id ORDER BY 2 ASC ";

    @Inject protected HostDao _hostDao;
    
    public VMInstanceDaoImpl() {
    }
    
    @PostConstruct
    protected void init() {

        IdStatesSearch = createSearchBuilder();
        IdStatesSearch.and("id", IdStatesSearch.entity().getId(), Op.EQ);
        IdStatesSearch.and("states", IdStatesSearch.entity().getState(), Op.IN);
        IdStatesSearch.done();
               
        VMClusterSearch = createSearchBuilder();
        SearchBuilder<HostVO> hostSearch = _hostDao.createSearchBuilder();
        VMClusterSearch.join("hostSearch", hostSearch, hostSearch.entity().getId(), VMClusterSearch.entity().getHostId(), JoinType.INNER);
        hostSearch.and("clusterId", hostSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        VMClusterSearch.done();

        
        LHVMClusterSearch = createSearchBuilder();
        SearchBuilder<HostVO> hostSearch1 = _hostDao.createSearchBuilder();
        LHVMClusterSearch.join("hostSearch1", hostSearch1, hostSearch1.entity().getId(), LHVMClusterSearch.entity().getLastHostId(), JoinType.INNER);
        LHVMClusterSearch.and("hostid", LHVMClusterSearch.entity().getHostId(), Op.NULL);
        hostSearch1.and("clusterId", hostSearch1.entity().getClusterId(), SearchCriteria.Op.EQ);
        LHVMClusterSearch.done();

        
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("host", AllFieldsSearch.entity().getHostId(), Op.EQ);
        AllFieldsSearch.and("lastHost", AllFieldsSearch.entity().getLastHostId(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("zone", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("type", AllFieldsSearch.entity().getType(), Op.EQ);
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.done();

        ZoneTemplateNonExpungedSearch = createSearchBuilder();
        ZoneTemplateNonExpungedSearch.and("zone", ZoneTemplateNonExpungedSearch.entity().getDataCenterId(), Op.EQ);
        ZoneTemplateNonExpungedSearch.and("template", ZoneTemplateNonExpungedSearch.entity().getTemplateId(), Op.EQ);
        ZoneTemplateNonExpungedSearch.and("state", ZoneTemplateNonExpungedSearch.entity().getState(), Op.NEQ);
        ZoneTemplateNonExpungedSearch.done();

        NameLikeSearch = createSearchBuilder();
        NameLikeSearch.and("name", NameLikeSearch.entity().getHostName(), Op.LIKE);
        NameLikeSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), Op.EQ);
        StateChangeSearch.and("states", StateChangeSearch.entity().getState(), Op.EQ);
        StateChangeSearch.and("host", StateChangeSearch.entity().getHostId(), Op.EQ);
        StateChangeSearch.and("update", StateChangeSearch.entity().getUpdated(), Op.EQ);
        StateChangeSearch.done();

        TransitionSearch = createSearchBuilder();
        TransitionSearch.and("updateTime", TransitionSearch.entity().getUpdateTime(), Op.LT);
        TransitionSearch.and("states", TransitionSearch.entity().getState(), Op.IN);
        TransitionSearch.done();

        TypesSearch = createSearchBuilder();
        TypesSearch.and("types", TypesSearch.entity().getType(), Op.IN);
        TypesSearch.done();

        IdTypesSearch = createSearchBuilder();
        IdTypesSearch.and("id", IdTypesSearch.entity().getId(), Op.EQ);
        IdTypesSearch.and("types", IdTypesSearch.entity().getType(), Op.IN);
        IdTypesSearch.done();
        
        HostIdTypesSearch = createSearchBuilder();
        HostIdTypesSearch.and("hostid", HostIdTypesSearch.entity().getHostId(), Op.EQ);
        HostIdTypesSearch.and("types", HostIdTypesSearch.entity().getType(), Op.IN);
        HostIdTypesSearch.done();
        
        HostIdUpTypesSearch = createSearchBuilder();
        HostIdUpTypesSearch.and("hostid", HostIdUpTypesSearch.entity().getHostId(), Op.EQ);
        HostIdUpTypesSearch.and("types", HostIdUpTypesSearch.entity().getType(), Op.IN);
        HostIdUpTypesSearch.and("states", HostIdUpTypesSearch.entity().getState(), Op.NIN);
        HostIdUpTypesSearch.done();
        
        HostUpSearch = createSearchBuilder();
        HostUpSearch.and("host", HostUpSearch.entity().getHostId(), Op.EQ);
        HostUpSearch.and("states", HostUpSearch.entity().getState(), Op.IN);
        HostUpSearch.done();
        
        InstanceNameSearch = createSearchBuilder();
        InstanceNameSearch.and("instanceName", InstanceNameSearch.entity().getInstanceName(), Op.EQ);
        InstanceNameSearch.done();

        CountVirtualRoutersByAccount = createSearchBuilder(Long.class);
        CountVirtualRoutersByAccount.select(null, Func.COUNT, null);
        CountVirtualRoutersByAccount.and("account", CountVirtualRoutersByAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        CountVirtualRoutersByAccount.and("type", CountVirtualRoutersByAccount.entity().getType(), SearchCriteria.Op.EQ);
        CountVirtualRoutersByAccount.and("state", CountVirtualRoutersByAccount.entity().getState(), SearchCriteria.Op.NIN);        
        CountVirtualRoutersByAccount.done();
        
        CountRunningByHost = createSearchBuilder(Long.class);
        CountRunningByHost.select(null, Func.COUNT, null);
        CountRunningByHost.and("host", CountRunningByHost.entity().getHostId(), SearchCriteria.Op.EQ);
        CountRunningByHost.and("state", CountRunningByHost.entity().getState(), SearchCriteria.Op.EQ);
        CountRunningByHost.done();        

        CountRunningByAccount = createSearchBuilder(Long.class);
        CountRunningByAccount.select(null, Func.COUNT, null);
        CountRunningByAccount.and("account", CountRunningByAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        CountRunningByAccount.and("state", CountRunningByAccount.entity().getState(), SearchCriteria.Op.EQ);
        CountRunningByAccount.done();        
        
        _updateTimeAttr = _allAttributes.get("updateTime");
        assert _updateTimeAttr != null : "Couldn't get this updateTime attribute";
    }

    @Override
    public List<VMInstanceVO> listByAccountId(long accountId) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("account", accountId);
        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> findVMInstancesLike(String name) {
        SearchCriteria<VMInstanceVO> sc = NameLikeSearch.create();
        sc.setParameters("name", "%" + name + "%");
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByHostId(long hostid) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("host", hostid);

        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listByZoneId(long zoneId) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("zone", zoneId);

        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listByPodId(long podId) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByClusterId(long clusterId) {
        SearchCriteria<VMInstanceVO> sc = VMClusterSearch.create();
        sc.setJoinParameters("hostSearch", "clusterId", clusterId);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listLHByClusterId(long clusterId) {
        SearchCriteria<VMInstanceVO> sc = LHVMClusterSearch.create();
        sc.setJoinParameters("hostSearch1", "clusterId", clusterId);
        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listByZoneIdAndType(long zoneId, VirtualMachine.Type type) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("zone", zoneId);
        sc.setParameters("type", type.toString());
        return listBy(sc);
    }
    
    
    @Override
    public List<VMInstanceVO> listNonExpungedByZoneAndTemplate(long zoneId, long templateId) {
        SearchCriteria<VMInstanceVO> sc = ZoneTemplateNonExpungedSearch.create();

        sc.setParameters("zone", zoneId);
        sc.setParameters("template", templateId);
        sc.setParameters("state", State.Expunging);

        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> findVMInTransition(Date time, State... states) {
        SearchCriteria<VMInstanceVO> sc = TransitionSearch.create();

        sc.setParameters("states", (Object[]) states);
        sc.setParameters("updateTime", time);

        return search(sc, null);
    }

    @Override
    public List<VMInstanceVO> listByHostIdTypes(long hostid, Type... types) {
        SearchCriteria<VMInstanceVO> sc = HostIdTypesSearch.create();
        sc.setParameters("hostid", hostid);
        sc.setParameters("types", (Object[]) types);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listUpByHostIdTypes(long hostid, Type... types) {
        SearchCriteria<VMInstanceVO> sc = HostIdUpTypesSearch.create();
        sc.setParameters("hostid", hostid);
        sc.setParameters("types", (Object[]) types);
        sc.setParameters("states", new Object[] {State.Destroyed, State.Stopped, State.Expunging});
        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listUpByHostId(Long hostId) {
        SearchCriteria<VMInstanceVO> sc = HostUpSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", new Object[] {State.Starting, State.Running});
        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listByTypes(Type... types) {
        SearchCriteria<VMInstanceVO> sc = TypesSearch.create();
        sc.setParameters("types", (Object[]) types);
        return listBy(sc);
    }
    
    @Override
    public List<VMInstanceVO> listByTypeAndState(VirtualMachine.Type type, State state) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("type", type);
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public VMInstanceVO findByIdTypes(long id, Type... types) {
        SearchCriteria<VMInstanceVO> sc = IdTypesSearch.create();
        sc.setParameters("id", id);
        sc.setParameters("types", (Object[]) types);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public VMInstanceVO findVMByInstanceName(String name) {
        SearchCriteria<VMInstanceVO> sc = InstanceNameSearch.create();
        sc.setParameters("instanceName", name);
        return findOneBy(sc);
    }
   
    @Override
    public void updateProxyId(long id, Long proxyId, Date time) {
        VMInstanceVO vo = createForUpdate();
        vo.setProxyId(proxyId);
        vo.setProxyAssignTime(time);
        update(id, vo);
    }

    @Override
    public boolean updateState(State oldState, Event event,	State newState, VirtualMachine vm, Object opaque) {
    	if (newState == null) {
    		if (s_logger.isDebugEnabled()) {
    			s_logger.debug("There's no way to transition from old state: " + oldState.toString() + " event: " + event.toString());
    		}
    		return false;
    	}

    	@SuppressWarnings("unchecked")
		Pair<Long, Long> hosts = (Pair<Long,Long>)opaque;
		Long newHostId = hosts.second();
    	
    	VMInstanceVO vmi = (VMInstanceVO)vm;
    	Long oldHostId = vmi.getHostId();
    	Long oldUpdated = vmi.getUpdated();
    	Date oldUpdateDate = vmi.getUpdateTime();
    	
    	SearchCriteria<VMInstanceVO> sc = StateChangeSearch.create();
    	sc.setParameters("id", vmi.getId());
    	sc.setParameters("states", oldState);
    	sc.setParameters("host", vmi.getHostId());
    	sc.setParameters("update", vmi.getUpdated());

    	vmi.incrUpdated();
    	UpdateBuilder ub = getUpdateBuilder(vmi);
    	
    	ub.set(vmi, "state", newState);
    	ub.set(vmi, "hostId", newHostId);
    	ub.set(vmi, "podIdToDeployIn", vmi.getPodIdToDeployIn());
    	ub.set(vmi, _updateTimeAttr, new Date());

    	int result = update(vmi, sc);
    	if (result == 0 && s_logger.isDebugEnabled()) {

    		VMInstanceVO vo = findByIdIncludingRemoved(vm.getId());
    		
    		if (vo != null) {
        		StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
        		str.append(": DB Data={Host=").append(vo.getHostId()).append("; State=").append(vo.getState().toString()).append("; updated=").append(vo.getUpdated()).append("; time=").append(vo.getUpdateTime());
        		str.append("} New Data: {Host=").append(vm.getHostId()).append("; State=").append(vm.getState().toString()).append("; updated=").append(vmi.getUpdated()).append("; time=").append(vo.getUpdateTime());
        		str.append("} Stale Data: {Host=").append(oldHostId).append("; State=").append(oldState).append("; updated=").append(oldUpdated).append("; time=").append(oldUpdateDate).append("}");
        		s_logger.debug(str.toString());

    		} else {
    		    s_logger.debug("Unable to update the vm id=" + vm.getId() + "; the vm either doesn't exist or already removed");
    		}
    	}
    	return result > 0;
    }
    
    @Override
	public List<VMInstanceVO> listByLastHostId(Long hostId) {
		SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
		sc.setParameters("lastHost", hostId);
		sc.setParameters("state", State.Stopped);
		return listBy(sc);
	}
    
    @Override
    public Long countAllocatedVirtualRoutersForAccount(long accountId) {
    	SearchCriteria<Long> sc = CountVirtualRoutersByAccount.create();
        sc.setParameters("account", accountId);
        sc.setParameters("type", VirtualMachine.Type.DomainRouter);
		sc.setParameters("state", new Object[] {State.Destroyed, State.Error, State.Expunging});
        return customSearch(sc, null).get(0);
    }
    
    @Override
    public List<VMInstanceVO> listVmsMigratingFromHost(Long hostId) {
        SearchCriteria<VMInstanceVO> sc = AllFieldsSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Migrating);
        return listBy(sc);
    }
    
    @Override
    public Long countRunningByHostId(long hostId){
        SearchCriteria<Long> sc = CountRunningByHost.create();
        sc.setParameters("host", hostId);
        sc.setParameters("state", State.Running);
        return customSearch(sc, null).get(0);
    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listClusterIdsInZoneByVmCount(long zoneId, long accountId) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        Map<Long, Double> clusterVmCountMap = new HashMap<Long, Double>();

        StringBuilder sql = new StringBuilder(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1);
        sql.append("host.data_center_id = ?");
        sql.append(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, zoneId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Long clusterId = rs.getLong(1);
                result.add(clusterId);
                clusterVmCountMap.put(clusterId, rs.getDouble(2));
            }
            return new Pair<List<Long>, Map<Long, Double>>(result, clusterVmCountMap);
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }
    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listClusterIdsInPodByVmCount(long podId, long accountId) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        Map<Long, Double> clusterVmCountMap = new HashMap<Long, Double>();

        StringBuilder sql = new StringBuilder(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1);
        sql.append("host.pod_id = ?");
        sql.append(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, podId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Long clusterId = rs.getLong(1);
                result.add(clusterId);                
                clusterVmCountMap.put(clusterId, rs.getDouble(2));
            }
            return new Pair<List<Long>, Map<Long, Double>>(result, clusterVmCountMap);
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + sql, e);
        }

    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listPodIdsInZoneByVmCount(long dataCenterId, long accountId) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        Map<Long, Double> podVmCountMap = new HashMap<Long, Double>();
        try {
            String sql = ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dataCenterId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Long podId = rs.getLong(1);
                result.add(podId);                
                podVmCountMap.put(podId, rs.getDouble(2));
            }
            return new Pair<List<Long>, Map<Long, Double>>(result, podVmCountMap);
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        }        
    }

    @Override
    public List<Long> listHostIdsByVmCount(long dcId, Long podId, Long clusterId, long accountId) {
        Transaction txn = Transaction.currentTxn();
        PreparedStatement pstmt = null;
        List<Long> result = new ArrayList<Long>();
        try {
            String sql = ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dcId);
            pstmt.setLong(3, podId);
            pstmt.setLong(4, clusterId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        } catch (Throwable e) {
            throw new CloudRuntimeException("Caught: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        }   
    }
    
    @Override
    public Long countRunningByAccount(long accountId){
        SearchCriteria<Long> sc = CountRunningByAccount.create();
        sc.setParameters("account", accountId);
        sc.setParameters("state", State.Running);
        return customSearch(sc, null).get(0);
    }
    
    @Override
    public List<VMInstanceVO> listNonRemovedVmsByTypeAndNetwork(long networkId, VirtualMachine.Type... types) {
        if (NetworkTypeSearch == null) {
            
            SearchBuilder<NicVO> nicSearch = _nicDao.createSearchBuilder();
            nicSearch.and("networkId", nicSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);

            NetworkTypeSearch = createSearchBuilder();
            NetworkTypeSearch.and("types", NetworkTypeSearch.entity().getType(), SearchCriteria.Op.IN);
            NetworkTypeSearch.and("removed", NetworkTypeSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
            NetworkTypeSearch.join("nicSearch", nicSearch, NetworkTypeSearch.entity().getId(), 
                    nicSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
            NetworkTypeSearch.done();
        }

        SearchCriteria<VMInstanceVO> sc = NetworkTypeSearch.create();
        if (types != null && types.length != 0) {
            sc.setParameters("types", (Object[]) types);
        }        
        sc.setJoinParameters("nicSearch", "networkId", networkId);

        return listBy(sc);
    }
    
    
    
    @Override
    public List<String> listDistinctHostNames(long networkId, VirtualMachine.Type... types) {
        if (DistinctHostNameSearch == null) {
            
            SearchBuilder<NicVO> nicSearch = _nicDao.createSearchBuilder();
            nicSearch.and("networkId", nicSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);

            DistinctHostNameSearch = createSearchBuilder(String.class);
            DistinctHostNameSearch.selectField(DistinctHostNameSearch.entity().getHostName());
            
            DistinctHostNameSearch.and("types", DistinctHostNameSearch.entity().getType(), SearchCriteria.Op.IN);
            DistinctHostNameSearch.and("removed", DistinctHostNameSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
            DistinctHostNameSearch.join("nicSearch", nicSearch, DistinctHostNameSearch.entity().getId(), 
                    nicSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
            DistinctHostNameSearch.done();
        }

        SearchCriteria<String> sc = DistinctHostNameSearch.create();
        if (types != null && types.length != 0) {
            sc.setParameters("types", (Object[]) types);
        }        
        sc.setJoinParameters("nicSearch", "networkId", networkId);

        return  customSearch(sc, null);
    }
    
    @Override
    @DB
    public boolean remove(Long id) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        VMInstanceVO vm = findById(id);
        if (vm != null && vm.getType() == Type.User) {
            _tagsDao.removeByIdAndType(id, TaggedResourceType.UserVm);
        }
        boolean result = super.remove(id);
        txn.commit();
        return result;
    }

}