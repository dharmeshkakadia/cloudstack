package org.apache.cloudstack.api.command;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.LdapUserResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.ldap.LdapManager;
import org.apache.cloudstack.ldap.LdapUser;
import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

@APICommand(name = "listAllLdapUsers", responseObject = LdapUserResponse.class, description = "Lists all LDAP Users", since = "4.2.0")
public class LdapListAllUsersCmd extends BaseListCmd {

    public static final Logger s_logger = Logger.getLogger(LdapListAllUsersCmd.class.getName());
    private static final String s_name = "ldapuserresponse";
    @Inject
    private LdapManager _ldapManager;

    public LdapListAllUsersCmd() {
        super();
    }

    public LdapListAllUsersCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException {
        try {
            final List<LdapUser> users = _ldapManager.getUsers();
            final ListResponse<LdapUserResponse> response = new ListResponse<LdapUserResponse>();
            final List<LdapUserResponse> ldapResponses = new ArrayList<LdapUserResponse>();

            for (final LdapUser user : users) {
                final LdapUserResponse ldapResponse = _ldapManager.createLdapUserResponse(user);
                ldapResponse.setObjectName("LdapUser");
                ldapResponses.add(ldapResponse);
            }

            response.setResponses(ldapResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final NoLdapUserMatchingQueryException ex) {
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }
}