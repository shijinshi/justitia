import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import cn.shijinshi.fabricmanager.service.helper.TlsCertHelper;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import org.bouncycastle.operator.OperatorCreationException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Test {

    @org.junit.Test
    public void test() {
        String os = System.getProperty("os.name");
        System.out.println(os);
//        if(os.toLowerCase().startsWith("win")){
//            System.out.println(os + " can't gunzip");
//        }
    }


    @org.junit.Test
    public void certVerTest() throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        String caCertStr = "-----BEGIN CERTIFICATE-----\n" +
                "MIICNjCCAdygAwIBAgIRAMJWio8C4c3IEdtyhKOQhbEwCgYIKoZIzj0EAwIwbDEL\n" +
                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                "cmFuY2lzY28xFDASBgNVBAoTC2V4YW1wbGUuY29tMRowGAYDVQQDExF0bHNjYS5l\n" +
                "eGFtcGxlLmNvbTAeFw0xOTAyMTgwMjIwMDBaFw0yOTAyMTUwMjIwMDBaMGwxCzAJ\n" +
                "BgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4gRnJh\n" +
                "bmNpc2NvMRQwEgYDVQQKEwtleGFtcGxlLmNvbTEaMBgGA1UEAxMRdGxzY2EuZXhh\n" +
                "bXBsZS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARyqOh1e03xaCWwQabY\n" +
                "gz268vHnVerpjQDKG/r/W9mZu3Vv640oD1Qj2GihDxlUIOpbLmBYNAPe9pUxDweM\n" +
                "xviwo18wXTAOBgNVHQ8BAf8EBAMCAaYwDwYDVR0lBAgwBgYEVR0lADAPBgNVHRMB\n" +
                "Af8EBTADAQH/MCkGA1UdDgQiBCAQx6v/xi9i3aQDCSk/fgl8Dk8HMh7jm1K2HBFM\n" +
                "bLh9MjAKBggqhkjOPQQDAgNIADBFAiEA8+Yzn5FnSKyGsUESJlK1XoPlWEyZd+eu\n" +
                "ambms8Wp0ZQCIATuw9Z07aiFYPyPBV4YSDJUue1PSwY5xCdcmlJ3uQJP\n" +
                "-----END CERTIFICATE-----\n";
        String userCertStr = "-----BEGIN CERTIFICATE-----\n" +
                "MIICSTCCAe+gAwIBAgIQGld8pHtCC3SRLJ1wXGN+1zAKBggqhkjOPQQDAjB2MQsw\n" +
                "CQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\n" +
                "YW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEfMB0GA1UEAxMWdGxz\n" +
                "Y2Eub3JnMS5leGFtcGxlLmNvbTAeFw0xOTAyMTgwMjIwMDBaFw0yOTAyMTUwMjIw\n" +
                "MDBaMHYxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQH\n" +
                "Ew1TYW4gRnJhbmNpc2NvMRkwFwYDVQQKExBvcmcxLmV4YW1wbGUuY29tMR8wHQYD\n" +
                "VQQDExZ0bHNjYS5vcmcxLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0D\n" +
                "AQcDQgAEJf8kCiDL3z6694fA/mJvt3uFG+xUt4DXpjG7sJ/FmJ3n687h9ofWRzbe\n" +
                "LDkJA2PZv/lDUYp/oQCoSytj29juraNfMF0wDgYDVR0PAQH/BAQDAgGmMA8GA1Ud\n" +
                "JQQIMAYGBFUdJQAwDwYDVR0TAQH/BAUwAwEB/zApBgNVHQ4EIgQgsMWyncbeu76w\n" +
                "hRVmkJlQGtX1/VhmPT/nr+CMaicmoQAwCgYIKoZIzj0EAwIDSAAwRQIhAL+w+NfR\n" +
                "isQ5nD4RAsWRpchPVBFV+BfGSNB8cUYjovmJAiACLi4ElG/cTVU/96iud9wWG17W\n" +
                "NmHVGsQ036cEWDsB9w==\n" +
                "-----END CERTIFICATE-----\n";

        TlsCertHelper helper = new TlsCertHelper();
        X509Certificate caCert = helper.pemToX509Cert(caCertStr);
        X509Certificate userCert = helper.pemToX509Cert(userCertStr);
        helper.verifyCert(userCert, caCert);

        System.out.println();


    }


    @org.junit.Test
    public void sign() throws Exception {
        String key = "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgdY+KDBjS50XIHsOv\n" +
                "V3qsP/7wheLF6ksrjWXdYXlUq6ShRANCAARj657dAG3p+2zEJuBhk9VgOv1/TGTZ\n" +
                "SwI+iCVaz75aTqMTaMb3D1jwPIREzLRmDQSb58H604hngS5J8yEaTnu0\n" +
                "-----END PRIVATE KEY-----\n";
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIICaDCCAg6gAwIBAgIRAJWaE5GvwqJH6vGgS9wmn1YwCgYIKoZIzj0EAwIwdjEL\n" +
                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                "cmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHzAdBgNVBAMTFnRs\n" +
                "c2NhLm9yZzEuZXhhbXBsZS5jb20wHhcNMTkwMjE4MDIyMDAwWhcNMjkwMjE1MDIy\n" +
                "MDAwWjBbMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UE\n" +
                "BxMNU2FuIEZyYW5jaXNjbzEfMB0GA1UEAxMWcGVlcjAub3JnMS5leGFtcGxlLmNv\n" +
                "bTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGPrnt0Aben7bMQm4GGT1WA6/X9M\n" +
                "ZNlLAj6IJVrPvlpOoxNoxvcPWPA8hETMtGYNBJvnwfrTiGeBLknzIRpOe7SjgZcw\n" +
                "gZQwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcD\n" +
                "AjAMBgNVHRMBAf8EAjAAMCsGA1UdIwQkMCKAILDFsp3G3ru+sIUVZpCZUBrV9f1Y\n" +
                "Zj0/56/gjGonJqEAMCgGA1UdEQQhMB+CFnBlZXIwLm9yZzEuZXhhbXBsZS5jb22C\n" +
                "BXBlZXIwMAoGCCqGSM49BAMCA0gAMEUCIQCA+OjnYJQ6BGYE2JEV2uEBzzVgrehL\n" +
                "io7E0e/MUt0ZhwIgIruHpSVUkdAgImJ+dfO/zFveqUMNk7SGU88362H5t+A=\n" +
                "-----END CERTIFICATE-----\n";


        String data = "This is a mingwen";
        CertFileHelper helper = new CertFileHelper();
        X509Certificate publicKey = helper.pemToX509Cert(cert);
        PrivateKey privateKey = helper.pemToPrivateKey(key);
        System.out.println("public:" + publicKey.getPublicKey().getAlgorithm());
        System.out.println("private:" +privateKey.getAlgorithm());

        //2.执行签名
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] res = signature.sign();
        System.out.println("签名：" + HexBin.encode(res));

        //3.验证签名
        signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        boolean bool = signature.verify(res);
        System.out.println("验证：" + bool);
    }

    @org.junit.Test
    public void dsadsfa() throws IOException, CertificateException, InstantiationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvalidArgumentException, OperatorCreationException, CryptoException, ClassNotFoundException {
        TlsCertHelper tlsCertHelper =new TlsCertHelper();
//        TlsCertHelper.CertAndKeyEntity ordererOrg = tlsCertHelper.generateCaCert("OrdererOrg", null);
//
//        System.out.print(tlsCertHelper.certToPem(ordererOrg.getCertificate()));
//        System.out.print(tlsCertHelper.privateKeyToPem(ordererOrg.getPrivateKey()));

        String caCert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIBsTCCAVigAwIBAgIUMe/6VO1xOiSATLSyU0jxX59woPgwCgYIKoZIzj0EAwIw\n" +
                "WDELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkFTMQswCQYDVQQHDAJTWjEMMAoGA1UE\n" +
                "CgwDU0pTMQwwCgYDVQQLDANTSlMxEzARBgNVBAMMCk9yZGVyZXJPcmcwIBcNMTkw\n" +
                "MzE4MDg0MDI1WhgPMjA1MTAzMTAwODQwMjVaMFgxCzAJBgNVBAYTAkNOMQswCQYD\n" +
                "VQQIDAJBUzELMAkGA1UEBwwCU1oxDDAKBgNVBAoMA1NKUzEMMAoGA1UECwwDU0pT\n" +
                "MRMwEQYDVQQDDApPcmRlcmVyT3JnMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n" +
                "Po1CIZJaezn+ux3oBJQIDZ74vrfc+7qqQQkBR/sNcJ6pPzT1/aU6M8ILoB+sVaIi\n" +
                "jJ8jFU1Kehh8P9eb+amsqDAKBggqhkjOPQQDAgNHADBEAiB+91zs3WHsz9jN16g7\n" +
                "WtSXbD4J3HHWRusHBQNbVgUQawIgKFZ6XJ8YwmAB77YbHn6VZTlssaLvShMoqj8Q\n" +
                "J1+Fk3U=\n" +
                "-----END CERTIFICATE-----";
        String caKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgVmnRrM4+/BNI98VE\n" +
                "Wm0zB4cxHT/Qk3ysmItgU6zpIKagCgYIKoZIzj0DAQehRANCAAQ+jUIhklp7Of67\n" +
                "HegElAgNnvi+t9z7uqpBCQFH+w1wnqk/NPX9pTozwgugH6xVoiKMnyMVTUp6GHw/\n" +
                "15v5qayo\n" +
                "-----END PRIVATE KEY-----";

        TlsCertHelper.CertAndKeyEntity orderer0 = tlsCertHelper.signCert("user0", caKey, caCert, null);
        System.out.print(tlsCertHelper.certToPem(orderer0.getCertificate()));
        System.out.print(tlsCertHelper.privateKeyToPem(orderer0.getPrivateKey()));
    }

    @org.junit.Test
    public void te() {
        Map map = new HashMap();
        map.put("123", 123);
        System.out.println(map.get("123"));
        map.put("456",456);
        map.put("123",78);
        System.out.println(map.get("123"));
    }
}
