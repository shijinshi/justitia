package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;
import java.util.Date;

public class Certificates implements Serializable {
    public static final String STATE_GOOD = "good";
    public static final String STATE_REVOKED = "revoked";
    /**
     * 。
    * <p> column ==>serial_number</p>
     */
    private String serialNumber;

    /**
     * 。
    * <p> column ==>authority_key_identifier</p>
     */
    private String authorityKeyIdentifier;

    /**
     * 。
    * <p> column ==>ca_user_id</p>
     */
    private String caUserId;

    /**
     * 。
    * <p> column ==>server_name</p>
     */
    private String serverName;

    /**
     * 。
    * <p> column ==>not_before</p>
     */
    private Date notBefore;

    /**
     * 。
    * <p> column ==>not_after</p>
     */
    private Date notAfter;

    /**
     * 。
    * <p> column ==>state</p>
     */
    private String state;

    /**
     * 。
    * <p> column ==>cert_pem</p>
     */
    private String certPem;

    /**
     * 。
    * <p> column ==>key_pem</p>
     */
    private String keyPem;

    /**
     * certificates。
    * <p> table ==>Certificates</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #serialNumber}
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * 设置 。
     * @param serialNumber 
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * 获取 。
     * @return {@link #authorityKeyIdentifier}
     */
    public String getAuthorityKeyIdentifier() {
        return authorityKeyIdentifier;
    }

    /**
     * 设置 。
     * @param authorityKeyIdentifier 
     */
    public void setAuthorityKeyIdentifier(String authorityKeyIdentifier) {
        this.authorityKeyIdentifier = authorityKeyIdentifier;
    }

    /**
     * 获取 。
     * @return {@link #caUserId}
     */
    public String getCaUserId() {
        return caUserId;
    }

    /**
     * 设置 。
     * @param caUserId 
     */
    public void setCaUserId(String caUserId) {
        this.caUserId = caUserId;
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
     * @return {@link #notBefore}
     */
    public Date getNotBefore() {
        return notBefore;
    }

    /**
     * 设置 。
     * @param notBefore 
     */
    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    /**
     * 获取 。
     * @return {@link #notAfter}
     */
    public Date getNotAfter() {
        return notAfter;
    }

    /**
     * 设置 。
     * @param notAfter 
     */
    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
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
     * @return {@link #certPem}
     */
    public String getCertPem() {
        return certPem;
    }

    /**
     * 设置 。
     * @param certPem 
     */
    public void setCertPem(String certPem) {
        this.certPem = certPem;
    }

    /**
     * 获取 。
     * @return {@link #keyPem}
     */
    public String getKeyPem() {
        return keyPem;
    }

    /**
     * 设置 。
     * @param keyPem 
     */
    public void setKeyPem(String keyPem) {
        this.keyPem = keyPem;
    }
}