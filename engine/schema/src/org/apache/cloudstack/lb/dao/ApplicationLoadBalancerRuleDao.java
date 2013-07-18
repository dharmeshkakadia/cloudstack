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

package org.apache.cloudstack.lb.dao;

import java.util.List;

import org.apache.cloudstack.lb.ApplicationLoadBalancerRuleVO;
import org.apache.network.rules.LoadBalancerContainer.Scheme;
import org.apache.utils.db.GenericDao;
import org.apache.utils.net.Ip;

public interface ApplicationLoadBalancerRuleDao extends GenericDao<ApplicationLoadBalancerRuleVO, Long>{
    List<ApplicationLoadBalancerRuleVO> listBySrcIpSrcNtwkId(Ip sourceIp, long sourceNetworkId);
    List<String> listLbIpsBySourceIpNetworkId(long sourceIpNetworkId);
    long countBySourceIp(Ip sourceIp, long sourceIpNetworkId);
    List<ApplicationLoadBalancerRuleVO> listBySourceIpAndNotRevoked(Ip sourceIp, long sourceNetworkId);
    List<String> listLbIpsBySourceIpNetworkIdAndScheme(long sourceIpNetworkId, Scheme scheme);
    long countBySourceIpAndNotRevoked(Ip sourceIp, long sourceIpNetworkId);
    long countActiveBySourceIp(Ip sourceIp, long sourceIpNetworkId);

}
