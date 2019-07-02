package cn.shijinshi.fabricmanager.controller.entity.host;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AddHostEntity {
    @NotEmpty
    private String hostName;    //唯一标识的主机名
    @NotEmpty
    private String protocol;
    @NotEmpty
    private String ip;
    @NotNull
    private Integer port;
    @NotNull
    private Boolean tlsEnable;
    private MultipartFile ca;
    private MultipartFile cert;
    private MultipartFile key;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
