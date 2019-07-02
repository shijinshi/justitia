package cn.shijinshi.fabricmanager.service.helper;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TlsCertHelper extends CertFileHelper {
    // Ca config
    private final String CA_C = "CN";           //国家
    private final String CA_ST = "AS";          //洲
    private final String CA_L = "SZ";           //地区或城市
    private final String CA_O = "SJS";          //组织
    private final String CA_OU = "SJS";         //组织成员
    private final String CA_ROOT_ISSUER = "C=CN,ST=AS,L=SZ,O=SJS,OU=SJS,CN=SICCA";
    private final String CA_DEFAULT_SUBJECT = "C=CN,ST=AS,L=SZ,O=SJS,OU=SJS,CN=";


    /**
     * BouncyCastleProvider
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static class CertAndKeyEntity {
        private X509Certificate certificate;
        private PrivateKey privateKey;

        CertAndKeyEntity(X509Certificate certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }

        public void setCertificate(X509Certificate certificate) {
            this.certificate = certificate;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }
    }

    /**
     * 生成根CA证书
     */
    public CertAndKeyEntity generateCaCert(String user, List<Extension> extensions) throws IOException,
            CertificateException, InstantiationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException,
            InvalidArgumentException, OperatorCreationException, CryptoException, ClassNotFoundException {

        String subject = CA_DEFAULT_SUBJECT + user;
        String issuer = subject;
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 32);

        KeyPair keyPair = generateKeyPair();
        PublicKey pubKey = keyPair.getPublic();
        PrivateKey priKey = keyPair.getPrivate();
        X509Certificate x509Cert = generateCert(issuer, subject, notBefore, notAfter, pubKey, priKey, extensions);
        return new CertAndKeyEntity(x509Cert, priKey);
    }

    /**
     * 生成用户证书，并把根证书设为信任证书
     */
    public CertAndKeyEntity signCert(String user, String caPriKeyPem, String caCertPem, List<Extension> extensions)
            throws IOException, CertificateException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            IllegalAccessException, InvalidArgumentException, OperatorCreationException, CryptoException, ClassNotFoundException {

        X509CertImpl caCert = (X509CertImpl) pemToX509Cert(caCertPem);
        Date notBefore = new Date();
        Date notAfter = caCert.getNotAfter();
        return signCert(user, caPriKeyPem, caCertPem, notBefore, notAfter, extensions);
    }

    /**
     * 生成用户证书，并把根证书设为信任证书
     */
    public CertAndKeyEntity signCert(String user, String caPriKeyPem, String caCertPem, Date notBefore, Date notAfter, List<Extension> extensions)
            throws IOException, CertificateException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            IllegalAccessException, InvalidArgumentException, OperatorCreationException, CryptoException, ClassNotFoundException {

        X509CertImpl caCert = (X509CertImpl) pemToX509Cert(caCertPem);
        PrivateKey caPriKey = pemToPrivateKey(caPriKeyPem);

        String subject = CA_DEFAULT_SUBJECT + user;
        String issuer = caCert.getIssuerDN().toString();

        KeyPair keyPair = generateKeyPair();
        PublicKey pubKey = keyPair.getPublic();
        X509Certificate x509Cert = generateCert(issuer, subject, notBefore, notAfter, pubKey, caPriKey, extensions);
        return new CertAndKeyEntity(x509Cert, keyPair.getPrivate());
    }


    /**
     * 创建ECDSA加密算法的一对公私钥
     *
     * @return KeyPair包含公钥和私钥
     */
    private KeyPair generateKeyPair() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException,
            InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // generate ECDSA keys: signing and encryption keys
        return cryptoSuite.keyGen();
    }

    /**
     * 生成 X509 证书
     *
     * @param issuer     证书签发者信息
     * @param subject    证书主体（拥有者）信息
     * @param notBefore  证书有效期，在此时间之后
     * @param notAfter   证书有效期，在此时间之前
     * @param pubKey     证书中包含的公钥
     * @param priKey     签发证书使用的私钥，签发CA根证书时使用公钥对应的私钥自签，用户证书使用CA证书的私钥签发
     * @param extensions 扩展信息
     * @return PrivateKeyEntry中包含证书和私钥
     */
    private X509Certificate generateCert(String issuer, String subject, Date notBefore, Date notAfter,
                                         PublicKey pubKey, PrivateKey priKey, List<Extension> extensions)
            throws IOException, OperatorCreationException, CertificateException {

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(pubKey.getEncoded());
        X509v3CertificateBuilder x509CertBuilder = new X509v3CertificateBuilder(
                new X500Name(issuer),
                getSerialNumber(),
                notBefore,
                notAfter,
                new X500Name(subject),
                subjectPublicKeyInfo
        );
        if (extensions != null) {
            for (Extension extension : extensions) {
                x509CertBuilder.addExtension(extension);
            }
        } else {
            extensions = new ArrayList<>();
//            new Extension(Extension.authorityKeyIdentifier, true, ASN1OctetString.fromByteArray())
        }

        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withECDSA");
        csBuilder.setProvider("BC");
        ContentSigner signer = csBuilder.build(priKey);
        X509CertificateHolder holder = x509CertBuilder.build(signer);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is = new ByteArrayInputStream(holder.toASN1Structure().getEncoded());
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
        is.close();

        return cert;
    }


    private BigInteger getSerialNumber() {
        // RFC 5280 4.1.2.2:
        // Certificate users MUST be able to handle serialNumber
        // values up to 20 octets.  Conforming CAs MUST NOT use
        // serialNumber values longer than 20 octets.
        //
        // If CFSSL is providing the serial numbers, it makes
        // sense to use the max supported size.
        SecureRandom csprng = new SecureRandom();
        byte[] serialNumber = new byte[20];
        csprng.nextBytes(serialNumber);

        // SetBytes interprets buf as the bytes of a big-endian
        // unsigned integer. The leading byte should be masked
        // off to ensure it isn't negative.
        serialNumber[0] &= 0x7F;
        return new BigInteger(serialNumber);
    }
    /**
     * 验证userCert是否为caCert签发的证书
     *
     * @param caCert   CA根证书
     * @param userCert 用户证书
     */
    public void verifyCert(X509Certificate caCert, X509Certificate userCert) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        userCert.verify(caCert.getPublicKey());
    }

}
