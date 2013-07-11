package org.apache.cloudstack.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

public class LdapUserManager {

    @Inject
    private LdapConfiguration _ldapConfiguration;

    public LdapUserManager() {
    }

    public LdapUserManager(final LdapConfiguration ldapConfiguration) {
        _ldapConfiguration = ldapConfiguration;
    }

    private LdapUser createUser(final SearchResult result) throws NamingException {
        final Attributes attributes = result.getAttributes();

        final String username = LdapUtils.getAttributeValue(attributes, _ldapConfiguration.getUsernameAttribute());
        final String email = LdapUtils.getAttributeValue(attributes, _ldapConfiguration.getEmailAttribute());
        final String realname = LdapUtils.getAttributeValue(attributes, _ldapConfiguration.getRealnameAttribute());
        final String principal = result.getName() + "," + _ldapConfiguration.getBaseDn();

        return new LdapUser(username, email, realname, principal);
    }

    public LdapUser getUser(final String username, final LdapContext context) throws NamingException {
        final NamingEnumeration<SearchResult> result = searchUsers(username, context);
        if (result.hasMoreElements()) {
            return createUser(result.nextElement());
        } else {
            throw new NamingException("No user found for username " + username);
        }
    }

    public List<LdapUser> getUsers(final LdapContext context) throws NamingException {
        return getUsers(null, context);
    }

    public List<LdapUser> getUsers(final String username, final LdapContext context) throws NamingException {
        final NamingEnumeration<SearchResult> results = searchUsers(username, context);

        final List<LdapUser> users = new ArrayList<LdapUser>();

        while (results.hasMoreElements()) {
            final SearchResult result = results.nextElement();
            users.add(createUser(result));
        }

        Collections.sort(users);

        return users;
    }

    public NamingEnumeration<SearchResult> searchUsers(final LdapContext context) throws NamingException {
        return searchUsers(null, context);
    }

    public NamingEnumeration<SearchResult> searchUsers(final String username, final LdapContext context) throws NamingException {
        final SearchControls controls = new SearchControls();

        controls.setSearchScope(_ldapConfiguration.getScope());
        controls.setReturningAttributes(_ldapConfiguration.getReturnAttributes());

        final String filter = "(&(objectClass=" + _ldapConfiguration.getUserObject() + ")" + "("
                + _ldapConfiguration.getUsernameAttribute() + "=" + (username == null ? "*" : username) + "))";

        return context.search(_ldapConfiguration.getBaseDn(), filter, controls);
    }
}