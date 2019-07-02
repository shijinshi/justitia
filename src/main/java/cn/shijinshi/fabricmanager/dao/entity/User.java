package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class User implements Serializable {
    /**
     * 。
    * <p> column ==>user_id</p>
     */
    private String userId;

    /**
     * 。
    * <p> column ==>user_name</p>
     */
    private String userName;

    /**
     * 。
    * <p> column ==>password</p>
     */
    private String password;

    /**
     * 。
    * <p> column ==>identity</p>
     */
    private String identity;

    /**
     * 。
    * <p> column ==>affiliation</p>
     */
    private String affiliation;

    /**
     * 。
    * <p> column ==>register_date</p>
     */
    private Long registerDate;

    /**
     * 。
    * <p> column ==>token</p>
     */
    private String token;

    /**
     * user。
    * <p> table ==>User</p>
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
     * @return {@link #userName}
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置 。
     * @param userName 
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取 。
     * @return {@link #password}
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 。
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取 。
     * @return {@link #identity}
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * 设置 。
     * @param identity 
     */
    public void setIdentity(String identity) {
        this.identity = identity;
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
     * @return {@link #registerDate}
     */
    public Long getRegisterDate() {
        return registerDate;
    }

    /**
     * 设置 。
     * @param registerDate 
     */
    public void setRegisterDate(Long registerDate) {
        this.registerDate = registerDate;
    }

    /**
     * 获取 。
     * @return {@link #token}
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置 。
     * @param token 
     */
    public void setToken(String token) {
        this.token = token;
    }
}