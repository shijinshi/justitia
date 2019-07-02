package cn.shijinshi.fabricmanager.controller.entity.channel;

import javax.validation.constraints.NotEmpty;

public class CreateChannelEntity {
    @NotEmpty
    private String channelName;
    @NotEmpty
    private String consortiumName;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getConsortiumName() {
        return consortiumName;
    }

    public void setConsortiumName(String consortiumName) {
        this.consortiumName = consortiumName;
    }
}
