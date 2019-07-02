package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

import cn.shijinshi.fabricmanager.service.ca.entity.GetCertificatesInfo;

public class GetCertFromCaEntity {
    private GetCertificatesInfo certInfo;


    public GetCertificatesInfo getCertInfo() {
        return certInfo;
    }

    public void setCertInfo(GetCertificatesInfo certInfo) {
        this.certInfo = certInfo;
    }
}
