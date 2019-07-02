package cn.shijinshi.fabricmanager.service.ca.entity;

import java.security.KeyPair;
import java.util.ArrayList;

/**
 * enroll时候的可选扩展参数
 */
public class EnrollExtend {

    private String host;
    private String csr;
    private KeyPair keypair;
    private String label;
    private String profile;
    private ArrayList<String> attrReq;



    public ArrayList<String> getAttrReq() {
        return attrReq;
    }

    public String getCsr() {
        return csr;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public KeyPair getKeypair() {
        return keypair;
    }

    public void setKeypair(KeyPair keypair) {
        this.keypair = keypair;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setAttrReq(ArrayList<String> attrReq) {
        this.attrReq = attrReq;
    }
}
