package cn.shijinshi.fabricmanager.service.ca.entity;

import org.hyperledger.fabric.sdk.Enrollment;

import javax.validation.constraints.NotEmpty;

public class EnrollInfo extends EnrollExtend {
    @NotEmpty
    private String identityId;
    @NotEmpty
    private String secret;
    @NotEmpty
    private String affiliation;

    private Enrollment enrollment;

    public void setExtend (EnrollExtend extend) {
        if (extend != null) {
            setHost(extend.getHost());
            setCsr(extend.getCsr());
            setKeypair(extend.getKeypair());
            setLabel(extend.getLabel());
            setProfile(extend.getProfile());
            setAttrReq(extend.getAttrReq());
        }
    }


    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }
}
