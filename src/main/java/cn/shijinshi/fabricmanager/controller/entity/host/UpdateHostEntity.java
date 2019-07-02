package cn.shijinshi.fabricmanager.controller.entity.host;

import org.springframework.web.multipart.MultipartFile;

public class UpdateHostEntity {
    private String protocol;
    private Integer port;
    private Boolean tlsEnable;
    private MultipartFile ca;
    private MultipartFile cert;
    private MultipartFile key;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getTlsEnable() {
        return tlsEnable;
    }

    public void setTlsEnable(Boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    public MultipartFile getCa() {
        return ca;
    }

    public void setCa(MultipartFile ca) {
        this.ca = ca;
    }

    public MultipartFile getCert() {
        return cert;
    }

    public void setCert(MultipartFile cert) {
        this.cert = cert;
    }

    public MultipartFile getKey() {
        return key;
    }

    public void setKey(MultipartFile key) {
        this.key = key;
    }
}
