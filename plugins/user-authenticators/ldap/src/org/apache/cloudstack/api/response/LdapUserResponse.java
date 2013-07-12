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
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;

import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;

public class LdapUserResponse extends BaseResponse {
    @SerializedName("email")
    @Param(description = "The user's email")
    private String email;

    @SerializedName("principal")
    @Param(description = "The user's principle")
    private String principal;

    @SerializedName("realname")
    @Param(description = "The user's realname")
    private String realname;

    @SerializedName("username")
    @Param(description = "The user's username")
    private String username;

    public LdapUserResponse() {
        super();
    }

    public LdapUserResponse(final String username, final String email, final String realname, final String principal) {
        this.username = username;
        this.email = email;
        this.realname = realname;
        this.principal = principal;
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

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setPrincipal(final String principal) {
        this.principal = principal;
    }

    public void setRealname(final String realname) {
        this.realname = realname;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}