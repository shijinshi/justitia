package cn.shijinshi.fabricmanager.controller.entity.channel;

import javax.validation.constraints.NotEmpty;

public class JoinChannelEntity {
    @NotEmpty
    private String channelName;
    @NotEmpty
    private String peerName;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }
}
