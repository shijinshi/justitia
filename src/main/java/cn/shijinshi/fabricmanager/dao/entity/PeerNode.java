package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class PeerNode implements Serializable {
    /**
     * 。
    * <p> column ==>peer_name</p>
     */
    private String peerName;

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
    * <p> column ==>ca_peer_user</p>
     */
    private String caPeerUser;

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
    * <p> column ==>couchdb_enable</p>
     */
    private Boolean couchdbEnable;

    /**
     * 。
    * <p> column ==>couchdb_name</p>
     */
    private String couchdbName;

    /**
     * peer_node。
    * <p> table ==>PeerNode</p>
     */
    private static final long serialVersionUID = 1L;

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
     * @return {@link #caPeerUser}
     */
    public String getCaPeerUser() {
        return caPeerUser;
    }

    /**
     * 设置 。
     * @param caPeerUser 
     */
    public void setCaPeerUser(String caPeerUser) {
        this.caPeerUser = caPeerUser;
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
     * @return {@link #couchdbEnable}
     */
    public Boolean getCouchdbEnable() {
        return couchdbEnable;
    }

    /**
     * 设置 。
     * @param couchdbEnable 
     */
    public void setCouchdbEnable(Boolean couchdbEnable) {
        this.couchdbEnable = couchdbEnable;
    }

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
}