package org.apache.cloudstack.ldap;

public class LdapUser implements Comparable<LdapUser> {
    private final String email;
    private final String principal;
    private final String realname;
    private final String username;

    public LdapUser(final String username, final String email, final String realname, final String principal) {
        this.username = username;
        this.email = email;
        this.realname = realname;
        this.principal = principal;
    }

    @Override
    public int compareTo(final LdapUser other) {
        return getUsername().compareTo(other.getUsername());
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof LdapUser) {
            final LdapUser otherLdapUser = (LdapUser)other;
            return getUsername().equals(otherLdapUser.getUsername());
        }
        return false;
    }

    public String getEmail() {
        return email;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getRealname() {
        return realname;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int hashCode() {
        return getUsername().hashCode();
    }
}