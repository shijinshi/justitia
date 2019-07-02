package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class OrdererNode implements Serializable {
    /**
     * 。
    * <p> column ==>orderer_name</p>
     */
    private String ordererName;

    /**
     * 。
    * <p> column ==>server_port</p>
     */
    private Integer serverPort;

    /**
     * 。
    * <p> column ==>ca_server_name</p>
     */
    private String caServerName;

    /**
     * 。
    * <p> column ==>ca_orderer_user</p>
     */
    private String caOrdererUser;

    /**
     * 。
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
    * <p> column ==>system_chain</p>
     */
    private String systemChain;

    /**
     * orderer_node。
    * <p> table ==>OrdererNode</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #ordererName}
     */
    public String getOrdererName() {
        return ordererName;
    }

    /**
     * 设置 。
     * @param ordererName 
     */
    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    /**
     * 获取 。
     * @return {@link #serverPort}
     */
    public Integer getServerPort() {
        return serverPort;
    }

    /**
     * 设置 。
     * @param serverPort 
     */
    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * 获取 。
     * @return {@link #caServerName}
     */
    public String getCaServerName() {
        return caServerName;
    }

    /**
     * 设置 。
     * @param caServerName 
     */
    public void setCaServerName(String caServerName) {
        this.caServerName = caServerName;
    }

    /**
     * 获取 。
     * @return {@link #caOrdererUser}
     */
    public String getCaOrdererUser() {
        return caOrdererUser;
    }

    /**
     * 设置 。
     * @param caOrdererUser 
     */
    public void setCaOrdererUser(String caOrdererUser) {
        this.caOrdererUser = caOrdererUser;
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
     * @return {@link #systemChain}
     */
    public String getSystemChain() {
        return systemChain;
    }

    /**
     * 设置 。
     * @param systemChain 
     */
    public void setSystemChain(String systemChain) {
        this.systemChain = systemChain;
    }
}