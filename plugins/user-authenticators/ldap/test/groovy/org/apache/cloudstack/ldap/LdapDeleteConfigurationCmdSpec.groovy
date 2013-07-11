package groovy.org.apache.cloudstack.ldap

import com.cloud.exception.InvalidParameterValueException
import org.apache.cloudstack.api.ServerApiException
import org.apache.cloudstack.api.command.LdapDeleteConfigurationCmd
import org.apache.cloudstack.api.response.LdapConfigurationResponse
import org.apache.cloudstack.ldap.LdapManager

class LdapDeleteConfigurationCmdSpec extends spock.lang.Specification {

    def "Test successful response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        ldapManager.deleteConfiguration(_) >> new LdapConfigurationResponse("localhost")
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when:
        ldapDeleteConfigurationCmd.execute()
        then:
        ldapDeleteConfigurationCmd.responseObject.hostname == "localhost"
    }

    def "Test failed response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        ldapManager.deleteConfiguration(_) >> { throw new InvalidParameterValueException() }
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when:
        ldapDeleteConfigurationCmd.execute()
        then:
        thrown ServerApiException
    }

    def "Test successful setting of hostname"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when:
        ldapDeleteConfigurationCmd.setHostname("localhost")
        then:
        ldapDeleteConfigurationCmd.getHostname() == "localhost"
    }

    def "Test getEntityOwnerId is 0"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when:
        long ownerId = ldapDeleteConfigurationCmd.getEntityOwnerId()
        then:
        ownerId == 0
    }

    def "Test successful return of getCommandName"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapDeleteConfigurationCmd = new LdapDeleteConfigurationCmd(ldapManager)
        when:
        String commandName = ldapDeleteConfigurationCmd.getCommandName()
        then:
        commandName == "ldapconfigurationresponse"
    }
}
