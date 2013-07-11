package groovy.org.apache.cloudstack.ldap

import com.cloud.user.UserAccountVO
import com.cloud.user.dao.UserAccountDao
import com.cloud.utils.Pair
import org.apache.cloudstack.ldap.LdapAuthenticator
import org.apache.cloudstack.ldap.LdapConfigurationVO
import org.apache.cloudstack.ldap.LdapManager

class LdapAuthenticatorSpec extends spock.lang.Specification {

    def "Test a failed authentication due to user not being found within cloudstack"() {
        given:
        LdapManager ldapManager = Mock(LdapManager)
        UserAccountDao userAccountDao = Mock(UserAccountDao)
        userAccountDao.getUserAccount(_, _) >> null
        def ldapAuthenticator = new LdapAuthenticator(ldapManager, userAccountDao)
        when:
        def result = ldapAuthenticator.authenticate("rmurphy", "password", 0, null)
        then:
        result == false
    }

    def "Test failed authentication due to ldap not being configured"() {
        given:
        def ldapManager = Mock(LdapManager)
        List<LdapConfigurationVO> ldapConfigurationList = new ArrayList()
        Pair<List<LdapConfigurationVO>, Integer> ldapConfigurations = new Pair<List<LdapConfigurationVO>, Integer>();
        ldapConfigurations.set(ldapConfigurationList, ldapConfigurationList.size())
        ldapManager.listConfigurations(_) >> ldapConfigurations

        UserAccountDao userAccountDao = Mock(UserAccountDao)
        userAccountDao.getUserAccount(_, _) >> new UserAccountVO()

        def ldapAuthenticator = new LdapAuthenticator(ldapManager, userAccountDao)
        when:
        def result = ldapAuthenticator.authenticate("rmurphy", "password", 0, null)
        then:
        result == false
    }

    def "Test failed authentication due to ldap bind being unsuccessful"() {
        given:

        def ldapManager = Mock(LdapManager)
        List<LdapConfigurationVO> ldapConfigurationList = new ArrayList()
        ldapConfigurationList.add(new LdapConfigurationVO("localhost", 389))
        Pair<List<LdapConfigurationVO>, Integer> ldapConfigurations = new Pair<List<LdapConfigurationVO>, Integer>();
        ldapConfigurations.set(ldapConfigurationList, ldapConfigurationList.size())
        ldapManager.listConfigurations(_) >> ldapConfigurations
        ldapManager.canAuthenticate(_, _) >> false

        UserAccountDao userAccountDao = Mock(UserAccountDao)
        userAccountDao.getUserAccount(_, _) >> new UserAccountVO()
        def ldapAuthenticator = new LdapAuthenticator(ldapManager, userAccountDao)

        when:
        def result = ldapAuthenticator.authenticate("rmurphy", "password", 0, null)

        then:
        result == false
    }

    def "Test that encode doesn't change the input"() {
        given:
        LdapManager ldapManager = Mock(LdapManager)
        UserAccountDao userAccountDao = Mock(UserAccountDao)
        def ldapAuthenticator = new LdapAuthenticator(ldapManager, userAccountDao)
        when:
        def result = ldapAuthenticator.encode("password")
        then:
        result == "password"
    }
}