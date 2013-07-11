package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.LdapConfigurationVO


class LdapConfigurationVOSpec extends spock.lang.Specification {
    def "Testing that the hostname is correctly set with the LDAP configuration VO"() {
        given: "You have created a LDAP configuration VO with a hostname set"
        def configuration = new LdapConfigurationVO()
        configuration.setHostname(hostname)
        expect: "The hostname is equal to the given data source"
        configuration.getHostname() == hostname
        where: "The hostname is set to "
        hostname << ["", null, "localhost"]
    }

    def "Testing that the port is correctly set within the LDAP configuration VO"() {
        given: "You have created a LDAP configuration VO with a port set"
        def configuration = new LdapConfigurationVO()
        configuration.setPort(port)
        expect: "The port is equal to the given data source"
        configuration.getPort() == port
        where: "The port is set to "
        port << [0, 1000, -1000, -0]
    }

    def "Testing that the ID is correctly set within the LDAP configuration VO"() {
        given: "You have created an LDAP Configuration VO"
        def configuration = new LdapConfigurationVO("localhost", 389);
        configuration.setId(id);
        expect: "The id is equal to the given data source"
        configuration.getId() == id;
        where: "The id is set to "
        id << [0, 1000, -1000, -0]
    }
}
