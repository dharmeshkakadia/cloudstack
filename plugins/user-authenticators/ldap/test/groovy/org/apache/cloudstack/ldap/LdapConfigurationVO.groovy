// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
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
