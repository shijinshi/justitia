package cn.shijinshi.fabricmanager.service.ca.entity;

import org.hyperledger.fabric.sdk.Enrollment;

import java.util.HashMap;
import java.util.Map;

public class RevokeInfo {
    /**
     * fabric ca service 1.3 版本支持撤销以下三种类型
     */
    public enum RevokeType {
        IDENTITY, ENROLLMENT, CERTIFICATE;
    }

    /**
     * fabric ca service 1.3 版本只支持以下使用原因
     */
    public enum Reason {
        UNSPECIFIED("unspecified"),                         //不明原因
        KEY_COMPROMISE("keycompromise"),                    //秘钥泄露
        CA_COMPROMISE("cacompromise"),                      //ca秘钥泄露
        AFFILIATION_CHANGE("affiliationchange"),            //关联关系改变
        SUPERSEDED("superseded"),                           //作废、被取代
        CESSATION_OF_OPERATION("cessationofoperation"),     //
        CERTIFICATE_HOLD("certificatehold"),
        REMOVE_FROM_CRL("removefromcrl"),                   //从证书吊销列表中移除
        PRIVILEGE_WITH_DRAWN("privilegewithdrawn"),
        AA_COMPROMISE("aacompromise");

        private final static Map<String, Reason> ENUM_MAP = new HashMap<>();

        static {
            for (Reason v : values()) {
                ENUM_MAP.put(v.getReason(), v);
            }
        }

        public static Reason fromString(String reason) {
            Reason res = ENUM_MAP.get(reason);
            return res == null ? UNSPECIFIED : res;
        }

        private String reason;

        Reason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }


    private RevokeType revokeType;
    private Reason reason;
    private boolean genCRL;

    //revoke enrollment
    private Enrollment enrollment;
    //revoke identity
    private String revokee;     //who is to be revoked.
    //revoke certificate
    private String serial;
    private String aki;

    /**
     * revoke enrollment
     *
     * @param reason
     * @param genCRL
     * @param enrollment
     */
    public RevokeInfo(Reason reason, boolean genCRL, Enrollment enrollment) {
        this.reason = reason;
        this.genCRL = genCRL;
        this.enrollment = enrollment;
        revokeType = RevokeType.ENROLLMENT;
    }

    /**
     * revoke identity
     *
     * @param reason
     * @param genCRL
     * @param revokee
     */
    public RevokeInfo(Reason reason, boolean genCRL, String revokee) {
        this.reason = reason;
        this.genCRL = genCRL;
        this.revokee = revokee;
        revokeType = RevokeType.IDENTITY;
    }

    /**
     * revoke certificate
     *
     * @param reason
     * @param genCRL
     * @param serial
     * @param aki
     */
    public RevokeInfo(Reason reason, boolean genCRL, String serial, String aki) {
        this.reason = reason;
        this.genCRL = genCRL;
        this.serial = serial;
        this.aki = aki;
        revokeType = RevokeType.CERTIFICATE;
    }

    public RevokeType getRevokeType() {
        return revokeType;
    }


    public String getReason() {
        return reason.getReason();
    }

    public boolean isGenCRL() {
        return genCRL;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public String getRevokee() {
        return revokee;
    }

    public String getSerial() {
        return serial;
    }

    public String getAki() {
        return aki;
    }
}
