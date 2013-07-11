package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.ServerApiException
import org.apache.cloudstack.api.command.LdapUserSearchCmd
import org.apache.cloudstack.api.response.LdapUserResponse
import org.apache.cloudstack.ldap.LdapManager
import org.apache.cloudstack.ldap.LdapUser
import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException

class LdapSearchUserCmdSpec extends spock.lang.Specification {
    def "Test successful response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        List<LdapUser> users = new ArrayList()
        users.add(new LdapUser("rmurphy", "rmurphy@test.com", "Ryan Murphy", "cn=rmurphy,dc=cloudstack,dc=org"))
        ldapManager.searchUsers(_) >> users
        LdapUserResponse response = new LdapUserResponse("rmurphy", "rmurphy@test.com", "Ryan Murphy", "cn=rmurphy,dc=cloudstack,dc=org")
        ldapManager.createLdapUserResponse(_) >> response
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when:
        ldapUserSearchCmd.execute()
        then:
        ldapUserSearchCmd.responseObject.getResponses().size() != 0
    }

    def "Test successful empty response from execute"() {
        given:
        def ldapManager = Mock(LdapManager)
        ldapManager.searchUsers(_) >> {throw new NoLdapUserMatchingQueryException()}
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when:
        ldapUserSearchCmd.execute()
        then:
        thrown ServerApiException
    }

    def "Test getEntityOwnerId is 0"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when:
        long ownerId = ldapUserSearchCmd.getEntityOwnerId()
        then:
        ownerId == 0
    }

    def "Test successful setting of ldapUserSearchCmd Query"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when:
        ldapUserSearchCmd.setQuery("")
        then:
        ldapUserSearchCmd.getQuery() == ""
    }

    def "Test successful return of getCommandName"() {
        given:
        def ldapManager = Mock(LdapManager)
        def ldapUserSearchCmd = new LdapUserSearchCmd(ldapManager)
        when:
        String commandName = ldapUserSearchCmd.getCommandName()
        then:
        commandName == "ldapuserresponse"
    }
}
