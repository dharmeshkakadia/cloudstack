package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.dao.LdapConfigurationDaoImpl

class LdapConfigurationDaoImplSpec extends spock.lang.Specification {
    def "Test setting up of a LdapConfigurationDao"() {
        given:
        def ldapConfigurationDaoImpl = new LdapConfigurationDaoImpl();
        expect:
        ldapConfigurationDaoImpl.hostnameSearch != null;
        ldapConfigurationDaoImpl.listAllConfigurationsSearch != null
    }
}
