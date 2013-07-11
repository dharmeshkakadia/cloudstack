package org.apache.cloudstack.ldap;

import java.util.List;

import org.apache.cloudstack.api.command.LdapListConfigurationCmd;
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.api.response.LdapUserResponse;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.Pair;
import com.cloud.utils.component.PluggableService;

public interface LdapManager extends PluggableService {

    LdapConfigurationResponse addConfiguration(String hostname, int port) throws InvalidParameterValueException;

    boolean canAuthenticate(String username, String password);

    LdapConfigurationResponse createLdapConfigurationResponse(LdapConfigurationVO configuration);

    LdapUserResponse createLdapUserResponse(LdapUser user);

    LdapConfigurationResponse deleteConfiguration(String hostname) throws InvalidParameterValueException;

    List<LdapUser> getUsers() throws NoLdapUserMatchingQueryException;

    Pair<List<? extends LdapConfigurationVO>, Integer> listConfigurations(LdapListConfigurationCmd cmd);

    List<LdapUser> searchUsers(String query) throws NoLdapUserMatchingQueryException;
}