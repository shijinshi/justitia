package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class FabricCaUser implements Serializable {
    public static final String STATE_REGISTERED = "registered";
    public static final String STATE_ENROLLED = "enrolled";
    public static final String STATE_REVOKED = "revoked";

    /**
     * 。
    * <p> column ==>user_id</p>
     */
    private String userId;

    /**
     * 。
    * <p> column ==>server_name</p>
     */
    private String serverName;

    /**
     * 。
    * <p> column ==>secret</p>
     */
    private String secret;

    /**
     * 。
    * <p> column ==>creator</p>
     */
    private String creator;

    /**
     * 。
    * <p> column ==>owner</p>
     */
    private String owner;

    /**
     * 。
    * <p> column ==>user_type</p>
     */
    private String userType;

    /**
     * 。
    * <p> column ==>Identity_type</p>
     */
    private String identityType;

    /**
     * 。
    * <p> column ==>affiliation</p>
     */
    private String affiliation;

    /**
     * 。
    * <p> column ==>state</p>
     */
    private String state;

    /**
     * 。
    * <p> column ==>max_enrollments</p>
     */
    private Integer maxEnrollments;

    /**
     * 。
    * <p> column ==>roles</p>
     */
    private String roles;

    /**
     * 。
    * <p> column ==>tls_enable</p>
     */
    private Boolean tlsEnable;

    /**
     * 。
    * <p> column ==>attributes</p>
     */
    private String attributes;

    /**
     * 。
    * <p> column ==>tls_cert</p>
     */
    private String tlsCert;

    /**
     * 。
    * <p> column ==>tls_key</p>
     */
    private String tlsKey;

    /**
     * fabric_ca_user。
    * <p> table ==>FabricCaUser</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #userId}
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置 。
     * @param userId 
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 。
     * @return {@link #serverName}
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * 设置 。
     * @param serverName 
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * 获取 。
     * @return {@link #secret}
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 设置 。
     * @param secret 
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 获取 。
     * @return {@link #creator}
     */
    public String getCreator() {
        return creator;
    }

    /**
     * 设置 。
     * @param creator 
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取 。
     * @return {@link #owner}
     */
    public String getOwner() {
        return owner;
    }

    /**
     * 设置 。
     * @param owner 
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 获取 。
     * @return {@link #userType}
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置 。
     * @param userType 
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 获取 。
     * @return {@link #identityType}
     */
    public String getIdentityType() {
        return identityType;
    }

    /**
     * 设置 。
     * @param identityType 
     */
    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    /**
     * 获取 。
     * @return {@link #affiliation}
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * 设置 。
     * @param affiliation 
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * 获取 。
     * @return {@link #state}
     */
    public String getState() {
        return state;
    }

    /**
     * 设置 。
     * @param state 
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * 获取 。
     * @return {@link #maxEnrollments}
     */
    public Integer getMaxEnrollments() {
        return maxEnrollments;
    }

    /**
     * 设置 。
     * @param maxEnrollments 
     */
    public void setMaxEnrollments(Integer maxEnrollments) {
        this.maxEnrollments = maxEnrollments;
    }

    /**
     * 获取 。
     * @return {@link #roles}
     */
    public String getRoles() {
        return roles;
    }

    /**
     * 设置 。
     * @param roles 
     */
    public void setRoles(String roles) {
        this.roles = roles;
    }

    /**
     * 获取 。
     * @return {@link #tlsEnable}
     */
    public Boolean getTlsEnable() {
        return tlsEnable;
    }

    /**
     * 设置 。
     * @param tlsEnable 
     */
    public void setTlsEnable(Boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    /**
     * 获取 。
     * @return {@link #attributes}
     */
    public String getAttributes() {
        return attributes;
    }

    /**
     * 设置 。
     * @param attributes 
     */
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     * 获取 。
     * @return {@link #tlsCert}
     */
    public String getTlsCert() {
        return tlsCert;
    }

    /**
     * 设置 。
     * @param tlsCert 
     */
    public void setTlsCert(String tlsCert) {
        this.tlsCert = tlsCert;
    }

    /**
     * 获取 。
     * @return {@link #tlsKey}
     */
    public String getTlsKey() {
        return tlsKey;
    }

    /**
     * 设置 。
     * @param tlsKey 
     */
    public void setTlsKey(String tlsKey) {
        this.tlsKey = tlsKey;
    }
}