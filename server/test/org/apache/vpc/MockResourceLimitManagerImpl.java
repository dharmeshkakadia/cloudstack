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
package org.apache.vpc;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.configuration.ResourceCount;
import org.apache.configuration.ResourceLimit;
import org.apache.configuration.Resource.ResourceType;
import org.apache.domain.Domain;
import org.apache.exception.ResourceAllocationException;
import org.apache.user.Account;
import org.apache.user.ResourceLimitService;
import org.apache.utils.component.ManagerBase;
import org.springframework.stereotype.Component;

@Component
@Local(value = { ResourceLimitService.class })
public class MockResourceLimitManagerImpl extends ManagerBase implements ResourceLimitService {

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#updateResourceLimit(java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Long)
     */
    @Override
    public ResourceLimit updateResourceLimit(Long accountId, Long domainId, Integer resourceType, Long max) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#recalculateResourceCount(java.lang.Long, java.lang.Long, java.lang.Integer)
     */
    @Override
    public List<? extends ResourceCount> recalculateResourceCount(Long accountId, Long domainId, Integer typeId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#searchForLimits(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Long, java.lang.Long)
     */
    @Override
    public List<? extends ResourceLimit> searchForLimits(Long id, Long accountId, Long domainId, Integer type, Long startIndex, Long pageSizeVal) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#findCorrectResourceLimitForAccount(org.apache.user.Account, org.apache.configuration.Resource.ResourceType)
     */
    @Override
    public long findCorrectResourceLimitForAccount(Account account, ResourceType type) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public long findCorrectResourceLimitForAccount(short accountType, Long limit, ResourceType type) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#findCorrectResourceLimitForDomain(org.apache.domain.Domain, org.apache.configuration.Resource.ResourceType)
     */
    @Override
    public long findCorrectResourceLimitForDomain(Domain domain, ResourceType type) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#incrementResourceCount(long, org.apache.configuration.Resource.ResourceType, java.lang.Long[])
     */
    @Override
    public void incrementResourceCount(long accountId, ResourceType type, Long... delta) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#decrementResourceCount(long, org.apache.configuration.Resource.ResourceType, java.lang.Long[])
     */
    @Override
    public void decrementResourceCount(long accountId, ResourceType type, Long... delta) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#checkResourceLimit(org.apache.user.Account, org.apache.configuration.Resource.ResourceType, long[])
     */
    @Override
    public void checkResourceLimit(Account account, ResourceType type, long... count) throws ResourceAllocationException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#countCpusForAccount(long)
     */
    public long countCpusForAccount(long accountId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#calculateRAMForAccount(long)
     */
    public long calculateMemoryForAccount(long accountId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#calculateSecondaryStorageForAccount(long)
     */
    public long calculateSecondaryStorageForAccount(long accountId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.user.ResourceLimitService#getResourceCount(org.apache.user.Account, org.apache.configuration.Resource.ResourceType)
     */
    @Override
    public long getResourceCount(Account account, ResourceType type) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
