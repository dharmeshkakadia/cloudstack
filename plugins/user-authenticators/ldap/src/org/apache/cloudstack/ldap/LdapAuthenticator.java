package org.apache.cloudstack.ldap;

import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.command.LdapListConfigurationCmd;

import com.cloud.server.auth.DefaultUserAuthenticator;
import com.cloud.user.UserAccount;
import com.cloud.user.dao.UserAccountDao;

public class LdapAuthenticator extends DefaultUserAuthenticator {
    private static final Logger s_logger = Logger.getLogger(LdapAuthenticator.class.getName());

    @Inject
    private LdapManager _ldapManager;
    @Inject
    private UserAccountDao _userAccountDao;

    public LdapAuthenticator() {
        super();
    }

    public LdapAuthenticator(final LdapManager ldapManager, final UserAccountDao userAccountDao) {
        super();
        _ldapManager = ldapManager;
        _userAccountDao = userAccountDao;
    }

    @Override
    public boolean authenticate(final String username, final String password, final Long domainId, final Map<String, Object[]> requestParameters) {

        final UserAccount user = _userAccountDao.getUserAccount(username, domainId);

        if (user == null) {
            s_logger.debug("Unable to find user with " + username + " in domain " + domainId);
            return false;
        } else if (_ldapManager.listConfigurations(new LdapListConfigurationCmd(_ldapManager)).second() > 0) {
            return _ldapManager.canAuthenticate(username, password);
        } else {
            return false;
        }
    }

    @Override
    public String encode(final String password) {
        return password;
    }

}
