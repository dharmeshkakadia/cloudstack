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

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;

import org.apache.configuration.ConfigurationVO;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.utils.db.GenericDaoBase;

@Local(value={ConfigurationDao.class})
public class MockConfigurationDaoImpl  extends GenericDaoBase<ConfigurationVO, String> implements ConfigurationDao{

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#getConfiguration(java.lang.String, java.util.Map)
     */
    @Override
    public Map<String, String> getConfiguration(String instance, Map<String, ? extends Object> params) {
        return new HashMap<String, String>();
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#getConfiguration(java.util.Map)
     */
    @Override
    public Map<String, String> getConfiguration(Map<String, ? extends Object> params) {
        return new HashMap<String, String>();
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#getConfiguration()
     */
    @Override
    public Map<String, String> getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#update(java.lang.String, java.lang.String)
     */
    @Override
    public boolean update(String name, String value) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#getValue(java.lang.String)
     */
    @Override
    public String getValue(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#getValueAndInitIfNotExist(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getValueAndInitIfNotExist(String name, String category, String initValue) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#isPremium()
     */
    @Override
    public boolean isPremium() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#findByName(java.lang.String)
     */
    @Override
    public ConfigurationVO findByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.configuration.dao.ConfigurationDao#update(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean update(String name, String category, String value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void invalidateCache() {
    }

}