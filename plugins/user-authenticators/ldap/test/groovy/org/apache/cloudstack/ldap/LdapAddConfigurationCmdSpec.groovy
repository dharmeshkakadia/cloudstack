package groovy.org.apache.cloudstack.ldap

import com.cloud.exception.InvalidParameterValueException
import org.apache.cloudstack.api.ServerApiException
import org.apache.cloudstack.api.command.LdapAddConfigurationCmd
import org.apache.cloudstack.api.response.LdapConfigurationResponse
import org.apache.cloudstack.ldap.LdapManager

class LdapAddConfigurationCmdSpec extends spock.lang.Specification {

    def "Test successful response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        ldapManager.addConfiguration(_, _) >> new LdapConfigurationResponse("localhost", 389)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        ldapAddConfigurationCmd.execute()
        then:
        ldapAddConfigurationCmd.responseObject.hostname == "localhost"
        ldapAddConfigurationCmd.responseObject.port == 389
    }

    def "Test failed response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        ldapManager.addConfiguration(_, _) >> { throw new InvalidParameterValueException() }
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        ldapAddConfigurationCmd.execute()
        then:
        thrown ServerApiException
    }

    def "Test successful setting of hostname"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        ldapAddConfigurationCmd.setHostname("localhost")
        then:
        ldapAddConfigurationCmd.getHostname() == "localhost"
    }

    def "Test successful setting of port"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        ldapAddConfigurationCmd.setPort(389)
        then:
        ldapAddConfigurationCmd.getPort() == 389
    }

    def "Test getEntityOwnerId is 0"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        long ownerId = ldapAddConfigurationCmd.getEntityOwnerId()
        then:
        ownerId == 0
    }

    def "Test successful return of getCommandName"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapAddConfigurationCmd = new LdapAddConfigurationCmd(ldapManager)
        when:
        String commandName = ldapAddConfigurationCmd.getCommandName()
        then:
        commandName == "ldapconfigurationresponse"
    }
}
