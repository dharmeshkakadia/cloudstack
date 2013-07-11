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