package cn.shijinshi.fabricmanager.service.ca.entity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class RegisterInfo {
    @NotEmpty
    private String identityId;
    @NotEmpty
    private String secret;
    @NotNull
    private String affiliation;
    @NotEmpty
    private String userType;
    @NotEmpty
    private String identityType;
    @NotNull
    private Integer maxEnrollments;
    private Map<String, Object> attributes;

    private String owner;

    public RegisterInfo() {
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public Integer getMaxEnrollments() {
        return maxEnrollments;
    }

    public void setMaxEnrollments(int maxEnrollments) {
        this.maxEnrollments = maxEnrollments;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    //
//    public Parent getParent() {
//        return parent;
//    }
//
//    public void setParent(Parent parent) {
//        this.parent = parent;
//    }
//
//    public void setRegistrant(String userName, String secret) {
//        this.parent = new Parent(userName, secret);
//    }

//    public class Parent {
//        @NotEmpty
//        private String identityId;
//        @NotEmpty
//        private String affiliation;
//
//        public Parent() {
//        }
//
//        public Parent(String identityId, String affiliation) {
//            this.identityId = identityId;
//            this.affiliation = affiliation;
//        }
//
//        public String getIdentityId() {
//            return identityId;
//        }
//
//        public void setIdentityId(String identityId) {
//            this.identityId = identityId;
//        }
//
//        public String getAffiliation() {
//            return affiliation;
//        }
//
//        public void setAffiliation(String affiliation) {
//            this.affiliation = affiliation;
//        }
//    }
}
