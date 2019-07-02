package cn.shijinshi.fabricmanager.service.fabric.node.fabricca.entity;

import org.springframework.web.multipart.MultipartFile;

public class CreateRootCaEntity extends CreateCaEntity {
    private boolean uploadCert;
    private MultipartFile certFile;
    private MultipartFile keyFile;

    public boolean isUploadCert() {
        return uploadCert;
    }

    public void setUploadCert(boolean uploadCert) {
        this.uploadCert = uploadCert;
    }

    public MultipartFile getCertFile() {
        return certFile;
    }

    public void setCertFile(MultipartFile certFile) {
        this.certFile = certFile;
    }

    public MultipartFile getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(MultipartFile keyFile) {
        this.keyFile = keyFile;
    }
}
