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

import java.util.Hashtable;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

public class LdapContextFactory {
    private static final Logger s_logger = Logger.getLogger(LdapContextFactory.class.getName());

    @Inject
    private LdapConfiguration _ldapConfiguration;

    public LdapContextFactory() {
    }

    public LdapContextFactory(final LdapConfiguration ldapConfiguration) {
        _ldapConfiguration = ldapConfiguration;
    }

    public LdapContext createBindContext() throws NamingException {
        return createBindContext(null);
    }

    public LdapContext createBindContext(final String providerUrl) throws NamingException {
        final String bindPrincipal = _ldapConfiguration.getBindPrincipal();
        final String bindPassword = _ldapConfiguration.getBindPassword();
        return createInitialDirContext(bindPrincipal, bindPassword, providerUrl, true);
    }

    private LdapContext createInitialDirContext(final String principal, final String password, final boolean isSystemContext)
            throws NamingException {
        return createInitialDirContext(principal, password, null, isSystemContext);
    }

    private LdapContext createInitialDirContext(final String principal, final String password, final String providerUrl, final boolean isSystemContext)
            throws NamingException {
        return new InitialLdapContext(getEnvironment(principal, password, providerUrl, isSystemContext), null);
    }

    public LdapContext createUserContext(final String principal, final String password) throws NamingException {
        return createInitialDirContext(principal, password, false);
    }

    private Hashtable<String, String> getEnvironment(final String principal, final String password, final String providerUrl, final boolean isSystemContext) {
        final String factory = _ldapConfiguration.getFactory();
        final String url = providerUrl == null ? _ldapConfiguration.getProviderUrl() : providerUrl;
        final String authentication = _ldapConfiguration.getAuthentication();

        final Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, factory);
        environment.put(Context.PROVIDER_URL, url);
        environment.put("com.sun.jndi.ldap.read.timeout", "1000");
        environment.put("com.sun.jndi.ldap.connect.pool", "true");

        if ("none".equals(authentication) && !isSystemContext) {
            environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        } else {
            environment.put(Context.SECURITY_AUTHENTICATION, authentication);
        }

        if (principal != null) {
            environment.put(Context.SECURITY_PRINCIPAL, principal);
        }

        if (password != null) {
            environment.put(Context.SECURITY_CREDENTIALS, password);
        }

        return environment;
    }

    public void testConnection(final String providerUrl) throws NamingException {
        try {
            createBindContext(providerUrl);
            s_logger.info("LDAP Connection was successful");
        } catch (final NamingException e) {
            s_logger.warn("LDAP Connection failed");
            s_logger.error(e.getMessage(), e);
            throw e;
        }
    }
}