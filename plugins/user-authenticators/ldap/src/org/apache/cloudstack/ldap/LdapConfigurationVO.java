package org.apache.cloudstack.ldap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.cloudstack.api.InternalIdentity;

@Entity
@Table(name = "ldap_configuration")
public class LdapConfigurationVO implements InternalIdentity {
    @Column(name = "hostname")
    private String hostname;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "port")
    private int port;

    public LdapConfigurationVO() {
    }

    public LdapConfigurationVO(final String hostname, final int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public long getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}