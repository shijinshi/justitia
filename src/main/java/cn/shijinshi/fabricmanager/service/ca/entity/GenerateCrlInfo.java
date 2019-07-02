package cn.shijinshi.fabricmanager.service.ca.entity;

import java.util.Date;

public class GenerateCrlInfo {
    private Date revokedBefore;
    private Date revokedAfter;
    private Date expireBefore;
    private Date expireAfter;

    public Date getRevokedBefore() {
        return revokedBefore;
    }

    public void setRevokedBefore(Date revokedBefore) {
        this.revokedBefore = revokedBefore;
    }

    public Date getRevokedAfter() {
        return revokedAfter;
    }

    public void setRevokedAfter(Date revokedAfter) {
        this.revokedAfter = revokedAfter;
    }

    public Date getExpireBefore() {
        return expireBefore;
    }

    public void setExpireBefore(Date expireBefore) {
        this.expireBefore = expireBefore;
    }

    public Date getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(Date expireAfter) {
        this.expireAfter = expireAfter;
    }
}
