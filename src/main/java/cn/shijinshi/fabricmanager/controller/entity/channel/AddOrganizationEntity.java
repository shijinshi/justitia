package cn.shijinshi.fabricmanager.controller.entity.channel;

import org.springframework.web.multipart.MultipartFile;

public class AddOrganizationEntity {
    private String channelName;
    private String description;
    private String orgName;
    private MultipartFile orgConfig;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public MultipartFile getOrgConfig() {
        return orgConfig;
    }

    public void setOrgConfig(MultipartFile orgConfig) {
        this.orgConfig = orgConfig;
    }
}
