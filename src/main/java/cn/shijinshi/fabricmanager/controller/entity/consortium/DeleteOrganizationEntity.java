package cn.shijinshi.fabricmanager.controller.entity.consortium;

import javax.validation.constraints.NotEmpty;

public class DeleteOrganizationEntity {
    @NotEmpty
    private String ordererName;
    @NotEmpty
    private String consortium;
    @NotEmpty
    private String orgName;

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    public String getConsortium() {
        return consortium;
    }

    public void setConsortium(String consortium) {
        this.consortium = consortium;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
