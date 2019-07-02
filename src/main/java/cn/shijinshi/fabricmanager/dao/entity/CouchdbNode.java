package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class CouchdbNode implements Serializable {
    /**
     * 。
    * <p> column ==>couchdb_name</p>
     */
    private String couchdbName;

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
    * <p> column ==>peer_name</p>
     */
    private String peerName;

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
     * couchdb_node。
    * <p> table ==>CouchdbNode</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #couchdbName}
     */
    public String getCouchdbName() {
        return couchdbName;
    }

    /**
     * 设置 。
     * @param couchdbName 
     */
    public void setCouchdbName(String couchdbName) {
        this.couchdbName = couchdbName;
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
     * @return {@link #peerName}
     */
    public String getPeerName() {
        return peerName;
    }

    /**
     * 设置 。
     * @param peerName 
     */
    public void setPeerName(String peerName) {
        this.peerName = peerName;
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
}