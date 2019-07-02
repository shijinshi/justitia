package cn.shijinshi.fabricmanager.service.ca.helper;

import cn.shijinshi.fabricmanager.service.helper.TlsCertHelper;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;
import sun.security.x509.X509CertImpl;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class TlsCertHelperTest {

    @Test
    public void generateCertTest() {
        TlsCertHelper tlsCertHelper = new TlsCertHelper();
        try {
            TlsCertHelper.CertAndKeyEntity certAndKeyEntity = tlsCertHelper.generateCaCert("shijinshi", null);
            X509Certificate cert = (X509Certificate) certAndKeyEntity.getCertificate();
            PublicKey publicKey = cert.getPublicKey();
            PrivateKey privateKey = certAndKeyEntity.getPrivateKey();

            String s = cert.getIssuerDN().toString();
            BigInteger serialNumber = cert.getSerialNumber();

            cert.verify(publicKey);
            PemObject pemObject = new PemObject("CERTIFICATE", cert.getEncoded());
            StringWriter stringWriter = new StringWriter();
            PemWriter pemWriter = new PemWriter(stringWriter);
            pemWriter.writeObject(pemObject);
            pemWriter.flush();
            pemWriter.close();

            System.out.println(stringWriter.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void signCertTest() {
        TlsCertHelper tlsCertHelper = new TlsCertHelper();
        try {
            TlsCertHelper.CertAndKeyEntity certAndKeyEntity = tlsCertHelper.generateCaCert("shijinshi", null);
            X509CertImpl caCert = (X509CertImpl) certAndKeyEntity.getCertificate();

            TlsCertHelper.CertAndKeyEntity certAndKeyEntity2 = tlsCertHelper.generateCaCert("shijinshi", null);
            X509CertImpl caCert2 = (X509CertImpl) certAndKeyEntity2.getCertificate();



//            KeyStore.PrivateKeyEntry privateKeyEntry1 = tlsCertHelper.signCert("Bob", privateKeyEntry.getPrivateKey(),caCert, null);
//            X509CertImpl cert = (X509CertImpl) privateKeyEntry1.getCertificate();

            //验证证书有效性
//            cert.verify(caCert.getPublicKey());
//            cert.verify(caCert2.getPublicKey());

//            PublicKey publicKey = cert.getPublicKey();
//            cert.verify(publicKey);

            System.out.println();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}