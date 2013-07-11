package org.apache.cloudstack.api.command;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
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

@APICommand(name = "searchLdap", responseObject = LdapUserResponse.class, description = "Searches LDAP based on the username attribute", since = "4.2.0")
public class LdapUserSearchCmd extends BaseListCmd {

    public static final Logger s_logger = Logger.getLogger(LdapUserSearchCmd.class.getName());
    private static final String s_name = "ldapuserresponse";
    @Inject
    private LdapManager _ldapManager;

    @Parameter(name = "query", type = CommandType.STRING, entityType = LdapUserResponse.class, required = true, description = "query to search using")
    private String query;

    public LdapUserSearchCmd() {
        super();
    }

    public LdapUserSearchCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException {
        try {
            final List<LdapUser> users = _ldapManager.searchUsers(getQuery());

            final ListResponse<LdapUserResponse> response = new ListResponse<LdapUserResponse>();
            final List<LdapUserResponse> ldapUserResponses = new ArrayList<LdapUserResponse>();

            for (final LdapUser user : users) {
                final LdapUserResponse ldapUserResponse = _ldapManager.createLdapUserResponse(user);
                ldapUserResponse.setObjectName("LdapUser");
                ldapUserResponses.add(ldapUserResponse);
            }

            response.setResponses(ldapUserResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final NoLdapUserMatchingQueryException e) {
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, e.getMessage());
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

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }
}