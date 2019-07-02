package cn.shijinshi.fabricmanager.controller.entity.organization;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateOrgEntity {
    @NotEmpty
    private String orgName;
    @NotEmpty
    private String orgMspId;
    @NotEmpty
    private String orgType;
    @NotNull
    private Boolean tlsEnable;
    private String ordererIp;
    private Integer ordererPort;
    private MultipartFile ordererTlsCert;


    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgMspId() {
        return orgMspId;
    }

    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public Boolean getTlsEnable() {
        return tlsEnable;
    }

    public void setTlsEnable(Boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    public String getOrdererIp() {
        return ordererIp;
    }

    public void setOrdererIp(String ordererIp) {
        this.ordererIp = ordererIp;
    }

    public Integer getOrdererPort() {
        return ordererPort;
    }

    public void setOrdererPort(Integer ordererPort) {
        this.ordererPort = ordererPort;
    }

    public MultipartFile getOrdererTlsCert() {
        return ordererTlsCert;
    }

    public void setOrdererTlsCert(MultipartFile ordererTlsCert) {
        this.ordererTlsCert = ordererTlsCert;
    }
}
