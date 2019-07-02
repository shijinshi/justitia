package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class PeerRefChannel implements Serializable {

    @Override
    public int hashCode(){
        return channelName.hashCode() ^ peerName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
             return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        PeerRefChannel peerRefChannel = (PeerRefChannel) obj;

        if (null != this.channelName) {
            if (!this.channelName.equals(peerRefChannel.getChannelName())) {
                return false;
            }
        }else {
            if (null != peerRefChannel.getChannelName()) {
                return false;
            }
        }

        if (null != this.getPeerName()) {
            if (!this.peerName.equals(peerRefChannel.getPeerName())) {
                return false;
            }
        } else {
            if (null != peerRefChannel.getPeerName()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 。
    * <p> column ==>channel_name</p>
     */
    private String channelName;

    /**
     * 。
    * <p> column ==>peer_name</p>
     */
    private String peerName;

    /**
     * peer_ref_channel。
    * <p> table ==>PeerRefChannel</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #channelName}
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 设置 。
     * @param channelName 
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
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
}