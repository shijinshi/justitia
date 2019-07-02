package cn.shijinshi.fabricmanager.dao.entity;

public class UserAndCerts extends FabricCaUser {
    private Certificates certificate;

    public Certificates getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificates certificate) {
        this.certificate = certificate;
    }
}
