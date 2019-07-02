package cn.shijinshi.fabricmanager.controller.entity.consortium;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AddOrganizationEntity {
    @NotEmpty
    private String ordererName;
    @NotEmpty
    private String consortiumName;
    @NotEmpty
    private String orgName;
    @NotNull
    private MultipartFile orgConfig;

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    public String getConsortiumName() {
        return consortiumName;
    }

    public void setConsortiumName(String consortiumName) {
        this.consortiumName = consortiumName;
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
