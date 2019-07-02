package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class Organization implements Serializable {
    public static final String ORG_TYPE_PEER = "peerOrg";
    public static final String ORG_TYPE_ORDERER = "ordererOrg";
    public static final String DEFAULT_ORDERER_NAME = "orderer";

    /**
     * 。
    * <p> column ==>org_name</p>
     */
    private String orgName;

    /**
     * 。
    * <p> column ==>org_msp_id</p>
     */
    private String orgMspId;

    /**
     * 。
    * <p> column ==>org_type</p>
     */
    private String orgType;

    /**
     * 。
    * <p> column ==>tls_enable</p>
     */
    private Boolean tlsEnable;

    /**
     * 。
    * <p> column ==>tls_ca_server</p>
     */
    private String tlsCaServer;

    /**
     * 。
    * <p> column ==>orderer_ip</p>
     */
    private String ordererIp;

    /**
     * 。
    * <p> column ==>orderer_port</p>
     */
    private Integer ordererPort;

    /**
     * 。
    * <p> column ==>tls_ca_cert</p>
     */
    private String tlsCaCert;

    /**
     * 。
    * <p> column ==>tls_ca_key</p>
     */
    private String tlsCaKey;

    /**
     * 。
    * <p> column ==>orderer_tls_cert</p>
     */
    private String ordererTlsCert;

    /**
     * organization。
    * <p> table ==>Organization</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #orgName}
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 设置 。
     * @param orgName 
     */
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    /**
     * 获取 。
     * @return {@link #orgMspId}
     */
    public String getOrgMspId() {
        return orgMspId;
    }

    /**
     * 设置 。
     * @param orgMspId 
     */
    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId;
    }

    /**
     * 获取 。
     * @return {@link #orgType}
     */
    public String getOrgType() {
        return orgType;
    }

    /**
     * 设置 。
     * @param orgType 
     */
    public void setOrgType(String orgType) {
        this.orgType = orgType;
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
     * @return {@link #tlsCaServer}
     */
    public String getTlsCaServer() {
        return tlsCaServer;
    }

    /**
     * 设置 。
     * @param tlsCaServer 
     */
    public void setTlsCaServer(String tlsCaServer) {
        this.tlsCaServer = tlsCaServer;
    }

    /**
     * 获取 。
     * @return {@link #ordererIp}
     */
    public String getOrdererIp() {
        return ordererIp;
    }

    /**
     * 设置 。
     * @param ordererIp 
     */
    public void setOrdererIp(String ordererIp) {
        this.ordererIp = ordererIp;
    }

    /**
     * 获取 。
     * @return {@link #ordererPort}
     */
    public Integer getOrdererPort() {
        return ordererPort;
    }

    /**
     * 设置 。
     * @param ordererPort 
     */
    public void setOrdererPort(Integer ordererPort) {
        this.ordererPort = ordererPort;
    }

    /**
     * 获取 。
     * @return {@link #tlsCaCert}
     */
    public String getTlsCaCert() {
        return tlsCaCert;
    }

    /**
     * 设置 。
     * @param tlsCaCert 
     */
    public void setTlsCaCert(String tlsCaCert) {
        this.tlsCaCert = tlsCaCert;
    }

    /**
     * 获取 。
     * @return {@link #tlsCaKey}
     */
    public String getTlsCaKey() {
        return tlsCaKey;
    }

    /**
     * 设置 。
     * @param tlsCaKey 
     */
    public void setTlsCaKey(String tlsCaKey) {
        this.tlsCaKey = tlsCaKey;
    }

    /**
     * 获取 。
     * @return {@link #ordererTlsCert}
     */
    public String getOrdererTlsCert() {
        return ordererTlsCert;
    }

    /**
     * 设置 。
     * @param ordererTlsCert 
     */
    public void setOrdererTlsCert(String ordererTlsCert) {
        this.ordererTlsCert = ordererTlsCert;
    }
}