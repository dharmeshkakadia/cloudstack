package groovy.org.apache.cloudstack.ldap

import org.apache.cloudstack.ldap.LdapUser

class LdapUserSpec extends spock.lang.Specification {

    def "Testing that the username is correctly set with the ldap object"() {
        given: "You have created a LDAP user object with a username"
        def user = new LdapUser(username, "", "", "")
        expect: "The username is equal to the given data source"
        user.getUsername() == username
        where: "The username is set to "
        username << ["", null, "rmurphy"]
    }

    def "Testing the email is correctly set with the ldap object"() {
        given: "You have created a LDAP user object with a realname"
        def user = new LdapUser("", email, "", "")
        expect: "The email is equal to the given data source"
        user.getEmail() == email
        where: "The email is set to "
        email << ["", null, "test@test.com"]
    }

    def "Testing the realname is correctly set with the ldap object"() {
        given: "You have created a LDAP user object with a realname"
        def user = new LdapUser("", "", realname, "")
        expect: "The realname is equal to the given data source"
        user.getRealname() == realname
        where: "The realname is set to "
        realname << ["", null, "Ryan Murphy"]
    }

    def "Testing the principal is correctly set with the ldap object"() {
        given: "You have created a LDAP user object with a principal"
        def user = new LdapUser("", "", "", principal)
        expect: "The principal is equal to the given data source"
        user.getPrincipal() == principal
        where: "The username is set to "
        principal << ["", null, "cn=rmurphy,dc=cloudstack,dc=org"]
    }

    def "Testing that LdapUser successfully gives the correct result for a compare to"() {
        given: "You have created two LDAP user objects"
        def userA = new LdapUser(usernameA, "", "", "")
        def userB = new LdapUser(usernameB, "", "", "")
        expect: "That when compared the result is less than or equal to 0"
        userA.compareTo(userB) <= 0
        where: "The following values are used"
        usernameA | usernameB
        "A"       | "B"
        "A"       | "A"
    }
}