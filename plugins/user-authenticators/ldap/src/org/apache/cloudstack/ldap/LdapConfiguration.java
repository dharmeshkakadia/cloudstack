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
package org.apache.cloudstack.ldap;

import java.util.List;

import javax.inject.Inject;
import javax.naming.directory.SearchControls;

import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.LdapListConfigurationCmd;

import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.utils.Pair;

public class LdapConfiguration {
    private final static String factory = "com.sun.jndi.ldap.LdapCtxFactory";

    private final static int scope = SearchControls.SUBTREE_SCOPE;

    @Inject
    private ConfigurationDao _configDao;

    @Inject
    private LdapManager _ldapManager;

    public LdapConfiguration() {
    }

    public LdapConfiguration(final ConfigurationDao configDao, final LdapManager ldapManager) {
        _configDao = configDao;
        _ldapManager = ldapManager;
    }

    public String getAuthentication() {
        if ((getBindPrincipal() == null) && (getBindPassword() == null)) {
            return "none";
        } else {
            return "simple";
        }
    }

    public String getBaseDn() {
        return _configDao.getValue("ldap.basedn");
    }

    public String getBindPassword() {
        return _configDao.getValue("ldap.bind.password");
    }

    public String getBindPrincipal() {
        return _configDao.getValue("ldap.bind.principal");
    }

    public String getEmailAttribute() {
        final String emailAttribute = _configDao.getValue("ldap.email.attribute");
        return emailAttribute == null ? "mail" : emailAttribute;
    }

    public String getFactory() {
        return factory;
    }

    public String getProviderUrl() {
        final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager
                .listConfigurations(new LdapListConfigurationCmd(_ldapManager));
        if (result.second() > 0) {
            final StringBuilder providerUrls = new StringBuilder();
            String delim = "";
            for (final LdapConfigurationVO resource : result.first()) {
                final String providerUrl = "ldap://" + resource.getHostname() + ":" + resource.getPort();
                providerUrls.append(delim).append(providerUrl);
                delim = " ";
            }
            return providerUrls.toString();
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to get Ldap Configuration(s)");
        }
    }

    public String getRealnameAttribute() {
        final String realnameAttribute = _configDao.getValue("ldap.realname.attribute");
        return realnameAttribute == null ? "cn" : realnameAttribute;
    }

    public String[] getReturnAttributes() {
        return new String[] {getUsernameAttribute(), getEmailAttribute(), getRealnameAttribute()};
    }

    public int getScope() {
        return scope;
    }

    public String getUsernameAttribute() {
        final String usernameAttribute = _configDao.getValue("ldap.username.attribute");
        return usernameAttribute == null ? "uid" : usernameAttribute;
    }

    public String getUserObject() {
        final String userObject = _configDao.getValue("ldap.user.object");
        return userObject == null ? "inetOrgPerson" : userObject;
    }
}