package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.response.LdapUserResponse


class LdapUserResponseSpec extends spock.lang.Specification {
    def "Testing succcessful setting of LdapUserResponse email"() {
        given:
        LdapUserResponse response = new LdapUserResponse();
        when:
        response.setEmail("rmurphy@test.com");
        then:
        response.getEmail() == "rmurphy@test.com";
    }

    def "Testing successful setting of LdapUserResponse principal"() {
        given:
        LdapUserResponse response = new LdapUserResponse()
        when:
        response.setPrincipal("dc=cloudstack,dc=org")
        then:
        response.getPrincipal() == "dc=cloudstack,dc=org"
    }

    def "Testing successful setting of LdapUserResponse username"() {
        given:
        LdapUserResponse response = new LdapUserResponse()
        when:
        response.setUsername("rmurphy")
        then:
        response.getUsername() == "rmurphy"
    }

    def "Testing successful setting of LdapUserResponse realname"() {
        given:
        LdapUserResponse response = new LdapUserResponse()
        when:
        response.setRealname("Ryan Murphy")
        then:
        response.getRealname() == "Ryan Murphy"
    }
}
