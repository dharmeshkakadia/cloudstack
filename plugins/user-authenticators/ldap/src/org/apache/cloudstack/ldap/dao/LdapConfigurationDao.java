package org.apache.cloudstack.ldap.dao;

import java.util.List;

import org.apache.cloudstack.ldap.LdapConfigurationVO;

import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

public interface LdapConfigurationDao extends GenericDao<LdapConfigurationVO, Long> {
    LdapConfigurationVO findByHostname(String hostname);

    Pair<List<LdapConfigurationVO>, Integer> searchConfigurations(String hostname, int port);
}