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
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.ldap.LdapConfigurationVO;
import org.apache.cloudstack.ldap.LdapManager;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.utils.Pair;

@APICommand(name = "listLdapConfigurations", responseObject = LdapConfigurationResponse.class, description = "Lists all LDAP configurations", since = "4.2.0")
public class LdapListConfigurationCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(LdapListConfigurationCmd.class.getName());

    private static final String s_name = "ldapconfigurationresponse";

    @Inject
    private LdapManager _ldapManager;

    @Parameter(name = "hostname", type = CommandType.STRING, required = false, description = "Hostname")
    private String hostname;

    @Parameter(name = "port", type = CommandType.INTEGER, required = false, description = "Port")
    private int port;

    public LdapListConfigurationCmd() {
        super();
    }

    public LdapListConfigurationCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
        final Pair<List<? extends LdapConfigurationVO>, Integer> result = _ldapManager.listConfigurations(this);
        if (result.second() > 0) {
            final ListResponse<LdapConfigurationResponse> response = new ListResponse<LdapConfigurationResponse>();
            final List<LdapConfigurationResponse> responses = new ArrayList<LdapConfigurationResponse>();
            for (final LdapConfigurationVO resource : result.first()) {
                final LdapConfigurationResponse configurationResponse = _ldapManager.createLdapConfigurationResponse(resource);
                configurationResponse.setObjectName("LdapConfiguration");
                responses.add(configurationResponse);
            }
            response.setResponses(responses, result.second());
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to find any LDAP Configurations");
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

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
