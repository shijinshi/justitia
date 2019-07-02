package cn.shijinshi.fabricmanager.dao.entity;

import java.io.Serializable;

public class FabricCa implements Serializable {
    /**
     * 。
    * <p> column ==>id</p>
     */
    private Integer id;

    /**
     * 。
    * <p> column ==>name</p>
     */
    private String name;

    /**
     * 。
    * <p> column ==>url</p>
     */
    private String url;

    /**
     * 。
    * <p> column ==>type</p>
     */
    private String type;

    /**
     * 。
    * <p> column ==>version</p>
     */
    private String version;

    /**
     * 。
    * <p> column ==>idemix_issuer_public_key</p>
     */
    private String idemixIssuerPublicKey;

    /**
     * 。
    * <p> column ==>idemix_issuer_pevocation_public_key</p>
     */
    private String idemixIssuerPevocationPublicKey;

    /**
     * 。
    * <p> column ==>pem</p>
     */
    private byte[] pem;

    /**
     * 。
    * <p> column ==>ca_chain</p>
     */
    private String caChain;

    /**
     * fabric_ca。
    * <p> table ==>FabricCaManageService</p>
     */
    private static final long serialVersionUID = 1L;

    /**
     * 获取 。
     * @return {@link #id}
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置 。
     * @param id 
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取 。
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 。
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 。
     * @return {@link #url}
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置 。
     * @param url 
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取 。
     * @return {@link #type}
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 。
     * @param type 
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取 。
     * @return {@link #version}
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置 。
     * @param version 
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取 。
     * @return {@link #idemixIssuerPublicKey}
     */
    public String getIdemixIssuerPublicKey() {
        return idemixIssuerPublicKey;
    }

    /**
     * 设置 。
     * @param idemixIssuerPublicKey 
     */
    public void setIdemixIssuerPublicKey(String idemixIssuerPublicKey) {
        this.idemixIssuerPublicKey = idemixIssuerPublicKey;
    }

    /**
     * 获取 。
     * @return {@link #idemixIssuerPevocationPublicKey}
     */
    public String getIdemixIssuerPevocationPublicKey() {
        return idemixIssuerPevocationPublicKey;
    }

    /**
     * 设置 。
     * @param idemixIssuerPevocationPublicKey 
     */
    public void setIdemixIssuerPevocationPublicKey(String idemixIssuerPevocationPublicKey) {
        this.idemixIssuerPevocationPublicKey = idemixIssuerPevocationPublicKey;
    }

    /**
     * 获取 。
     * @return {@link #pem}
     */
    public byte[] getPem() {
        return pem;
    }

    /**
     * 设置 。
     * @param pem 
     */
    public void setPem(byte[] pem) {
        this.pem = pem;
    }

    /**
     * 获取 。
     * @return {@link #caChain}
     */
    public String getCaChain() {
        return caChain;
    }

    /**
     * 设置 。
     * @param caChain 
     */
    public void setCaChain(String caChain) {
        this.caChain = caChain;
    }
}