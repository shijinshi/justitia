package cn.shijinshi.fabricmanager.service.ca.entity;

import java.util.Date;

public class GetCertificatesInfo {
    private String enrollmentId;
    private String serial;
    private String aki;
    private Date revokedStart;
    private Date revokedEnd;
    private Date expiredStart;
    private Date expiredEnd;
    private Boolean expired;
    private Boolean revoked;

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAki() {
        return aki;
    }

    public void setAki(String aki) {
        this.aki = aki;
    }

    public Date getRevokedStart() {
        return revokedStart;
    }

    public void setRevokedStart(Date revokedStart) {
        this.revokedStart = revokedStart;
    }

    public Date getRevokedEnd() {
        return revokedEnd;
    }

    public void setRevokedEnd(Date revokedEnd) {
        this.revokedEnd = revokedEnd;
    }

    public Date getExpiredStart() {
        return expiredStart;
    }

    public void setExpiredStart(Date expiredStart) {
        this.expiredStart = expiredStart;
    }

    public Date getExpiredEnd() {
        return expiredEnd;
    }

    public void setExpiredEnd(Date expiredEnd) {
        this.expiredEnd = expiredEnd;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
