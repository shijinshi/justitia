package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class TlsCertificates implements Serializable {
    /**
     * 。
    * <p> column ==>serial_number</p>
     */
    private String serialNumber;

    /**
     * 。
    * <p> column ==>cert_pem</p>
     */
    private String certPem;

    /**
     * 。
    * <p> column ==>key_pem</p>
     */
    private String keyPem;

    /**
     * tls_certificates。
    * <p> table ==>TlsCertificates</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #serialNumber}
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * 设置 。
     * @param serialNumber 
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * 获取 。
     * @return {@link #certPem}
     */
    public String getCertPem() {
        return certPem;
    }

    /**
     * 设置 。
     * @param certPem 
     */
    public void setCertPem(String certPem) {
        this.certPem = certPem;
    }

    /**
     * 获取 。
     * @return {@link #keyPem}
     */
    public String getKeyPem() {
        return keyPem;
    }

    /**
     * 设置 。
     * @param keyPem 
     */
    public void setKeyPem(String keyPem) {
        this.keyPem = keyPem;
    }
}