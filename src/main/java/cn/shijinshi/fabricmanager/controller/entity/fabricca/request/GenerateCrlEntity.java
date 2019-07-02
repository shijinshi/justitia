package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import cn.shijinshi.fabricmanager.service.ca.entity.GenerateCrlInfo;

public class GenerateCrlEntity {
    private GenerateCrlInfo crlInfo;

    public GenerateCrlInfo getCrlInfo() {
        return crlInfo;
    }

    public void setCrlInfo(GenerateCrlInfo crlInfo) {
        this.crlInfo = crlInfo;
    }
}
