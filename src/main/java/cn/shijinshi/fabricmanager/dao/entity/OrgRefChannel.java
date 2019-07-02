package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class OrgRefChannel implements Serializable {
    /**
     * 。
    * <p> column ==>channel_name</p>
     */
    private String channelName;

    /**
     * 。
    * <p> column ==>org_msp</p>
     */
    private String orgMsp;

    /**
     * 。
    * <p> column ==>anchor_peers</p>
     */
    private String anchorPeers;

    /**
     * org_ref_channel。
    * <p> table ==>OrgRefChannel</p>
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
     * @return {@link #orgMsp}
     */
    public String getOrgMsp() {
        return orgMsp;
    }

    /**
     * 设置 。
     * @param orgMsp 
     */
    public void setOrgMsp(String orgMsp) {
        this.orgMsp = orgMsp;
    }

    /**
     * 获取 。
     * @return {@link #anchorPeers}
     */
    public String getAnchorPeers() {
        return anchorPeers;
    }

    /**
     * 设置 。
     * @param anchorPeers 
     */
    public void setAnchorPeers(String anchorPeers) {
        this.anchorPeers = anchorPeers;
    }
}