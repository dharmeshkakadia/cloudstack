package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.api.response.LdapConfigurationResponse

class LdapConfigurationResponseSpec extends spock.lang.Specification {
    def "Testing succcessful setting of LdapConfigurationResponse hostname"() {
        given:
        LdapConfigurationResponse response = new LdapConfigurationResponse();
        when:
        response.setHostname("localhost");
        then:
        response.getHostname() == "localhost";
    }

    def "Testing successful setting of LdapConfigurationResponse port"() {
        given:
        LdapConfigurationResponse response = new LdapConfigurationResponse()
        when:
        response.setPort(389)
        then:
        response.getPort() == 389
    }

    def "Testing successful setting of LdapConfigurationResponse hostname and port via constructor"() {
        given:
        LdapConfigurationResponse response
        when:
        response = new LdapConfigurationResponse("localhost", 389)
        then:
        response.getHostname() == "localhost"
        response.getPort() == 389
    }
}
