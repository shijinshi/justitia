package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class FabricCaServer implements Serializable {
    /**
     * 。
    * <p> column ==>server_name</p>
     */
    private String serverName;

    /**
     * creator。
    * <p> column ==>creator</p>
     */
    private String creator;

    /**
     * 。
    * <p> column ==>host_name</p>
     */
    private String hostName;

    /**
     * 。
    * <p> column ==>container_id</p>
     */
    private String containerId;

    /**
     * 。
    * <p> column ==>port</p>
     */
    private Integer port;

    /**
     * 。
    * <p> column ==>exposed_port</p>
     */
    private Integer exposedPort;

    /**
     * 。
    * <p> column ==>home</p>
     */
    private String home;

    /**
     * 。
    * <p> column ==>parent_server</p>
     */
    private String parentServer;

    /**
     * 。
    * <p> column ==>type</p>
     */
    private String type;

    /**
     * 。
    * <p> column ==>tls_enable</p>
     */
    private Boolean tlsEnable;

    /**
     * 。
    * <p> column ==>affiliations</p>
     */
    private String affiliations;

    /**
     * 。
    * <p> column ==>tls_ca</p>
     */
    private String tlsCa;

    /**
     * 。
    * <p> column ==>tls_server_cert</p>
     */
    private String tlsServerCert;

    /**
     * 。
    * <p> column ==>tls_server_key</p>
     */
    private String tlsServerKey;

    /**
     * fabric_ca_server。
    * <p> table ==>FabricCaServer</p>
     */
    private static final long serialVersionUID = 1L;

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
     * 获取 creator。
     * @return {@link #creator}
     */
    public String getCreator() {
        return creator;
    }

    /**
     * 设置 creator。
     * @param creator creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 获取 。
     * @return {@link #hostName}
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * 设置 。
     * @param hostName 
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * 获取 。
     * @return {@link #containerId}
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * 设置 。
     * @param containerId 
     */
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    /**
     * 获取 。
     * @return {@link #port}
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置 。
     * @param port 
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 获取 。
     * @return {@link #exposedPort}
     */
    public Integer getExposedPort() {
        return exposedPort;
    }

    /**
     * 设置 。
     * @param exposedPort 
     */
    public void setExposedPort(Integer exposedPort) {
        this.exposedPort = exposedPort;
    }

    /**
     * 获取 。
     * @return {@link #home}
     */
    public String getHome() {
        return home;
    }

    /**
     * 设置 。
     * @param home 
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * 获取 。
     * @return {@link #parentServer}
     */
    public String getParentServer() {
        return parentServer;
    }

    /**
     * 设置 。
     * @param parentServer 
     */
    public void setParentServer(String parentServer) {
        this.parentServer = parentServer;
    }

    /**
     * 获取 。
     * @return {@link #type}
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 。
     * @param type 
     */
    public void setType(String type) {
        this.type = type;
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
     * @return {@link #affiliations}
     */
    public String getAffiliations() {
        return affiliations;
    }

    /**
     * 设置 。
     * @param affiliations 
     */
    public void setAffiliations(String affiliations) {
        this.affiliations = affiliations;
    }

    /**
     * 获取 。
     * @return {@link #tlsCa}
     */
    public String getTlsCa() {
        return tlsCa;
    }

    /**
     * 设置 。
     * @param tlsCa 
     */
    public void setTlsCa(String tlsCa) {
        this.tlsCa = tlsCa;
    }

    /**
     * 获取 。
     * @return {@link #tlsServerCert}
     */
    public String getTlsServerCert() {
        return tlsServerCert;
    }

    /**
     * 设置 。
     * @param tlsServerCert 
     */
    public void setTlsServerCert(String tlsServerCert) {
        this.tlsServerCert = tlsServerCert;
    }

    /**
     * 获取 。
     * @return {@link #tlsServerKey}
     */
    public String getTlsServerKey() {
        return tlsServerKey;
    }

    /**
     * 设置 。
     * @param tlsServerKey 
     */
    public void setTlsServerKey(String tlsServerKey) {
        this.tlsServerKey = tlsServerKey;
    }
}