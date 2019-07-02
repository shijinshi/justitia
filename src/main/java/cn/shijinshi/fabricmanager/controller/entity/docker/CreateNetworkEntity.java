package cn.shijinshi.fabricmanager.controller.entity.docker;

import javax.validation.constraints.NotEmpty;

public class CreateNetworkEntity {
    @NotEmpty
    private String networkName;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
}
