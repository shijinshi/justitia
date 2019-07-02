package cn.shijinshi.fabricmanager.service.fabric;

import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.Set;

public class FabricUserImpl implements User, Serializable{
    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment = null;
    private String mspId;

    public FabricUserImpl(String name, String affiliation){
        this.name = name;
        this.affiliation = affiliation;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public void setEnrollment(String certPem, String keyPem) throws IOException {
        CertFileHelper helper = new CertFileHelper();
        PrivateKey privateKey = helper.pemToPrivateKey(keyPem);
        this.enrollment = new X509Enrollment(privateKey, certPem);
    }

    @Override
    public String getMspId() {
        return this.mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

}
