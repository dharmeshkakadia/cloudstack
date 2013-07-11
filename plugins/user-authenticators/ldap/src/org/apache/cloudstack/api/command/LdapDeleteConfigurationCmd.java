package org.apache.cloudstack.api.command;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.LdapConfigurationResponse;
import org.apache.cloudstack.ldap.LdapManager;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;

@APICommand(name = "deleteLdapConfiguration", description = "Remove an Ldap Configuration", responseObject = LdapConfigurationResponse.class, since = "4.2.0")
public class LdapDeleteConfigurationCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(LdapDeleteConfigurationCmd.class.getName());
    private static final String s_name = "ldapconfigurationresponse";

    @Inject
    private LdapManager _ldapManager;

    @Parameter(name = "hostname", type = CommandType.STRING, required = true, description = "Hostname")
    private String hostname;

    public LdapDeleteConfigurationCmd() {
        super();
    }

    public LdapDeleteConfigurationCmd(final LdapManager ldapManager) {
        super();
        _ldapManager = ldapManager;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException {

        try {
            final LdapConfigurationResponse response = _ldapManager.deleteConfiguration(getHostname());
            response.setObjectName("LdapDeleteConfiguration");
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.toString());
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

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

}
