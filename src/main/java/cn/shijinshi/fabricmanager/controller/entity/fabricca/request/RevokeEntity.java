package cn.shijinshi.fabricmanager.controller.entity.fabricca.request;

public class RevokeEntity {
    private String reason;
    boolean genCRL;


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isGenCRL() {
        return genCRL;
    }

    public void setGenCRL(boolean genCRL) {
        this.genCRL = genCRL;
    }
}
