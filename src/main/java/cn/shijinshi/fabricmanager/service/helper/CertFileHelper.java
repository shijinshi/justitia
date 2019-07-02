package cn.shijinshi.fabricmanager.service.helper;

import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.utils.file.FileUtils;
import cn.shijinshi.fabricmanager.service.utils.file.ZipFileUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.hyperledger.fabric.sdk.Enrollment;
import sun.security.ec.ECPublicKeyImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class CertFileHelper {
    //在Security中增加BouncyCastleProvider
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    /**
     * Convert private key in PKCS8 to PEM
     *
     * @param privateKey 私钥
     * @return pem编码的私钥
     * @throws IOException 数据IO异常
     */
    public String privateKeyToPem(PrivateKey privateKey) throws IOException {
        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
        PemObject pemObject = new PemObject("PRIVATE KEY", bcecPrivateKey.getEncoded());
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        pemWriter.close();
        return stringWriter.toString();
    }

    public String certToPem(Certificate cert) throws CertificateException, IOException {
        PemObject pemObject = new PemObject("CERTIFICATE", cert.getEncoded());
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.flush();
        pemWriter.close();
        return stringWriter.toString();
    }

    public PrivateKey pemToPrivateKey(String keyPem) throws IOException {
        final Reader pemReader = new StringReader(keyPem);
        PEMParser pemParser = new PEMParser(pemReader);
        PrivateKeyInfo pemPair = (PrivateKeyInfo) pemParser.readObject();
        return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
    }

    public X509Certificate pemToX509Cert(String certPem) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
    }

    public byte[] privateKeyToBytes(PrivateKey privateKey) {
        return privateKey.getEncoded();
    }

    public PrivateKey bytesToPrivateKey(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(BouncyCastleProvider.PROVIDER_NAME);
        return keyFactory.generatePrivate(keySpec);
    }

    public String getSerialNumberString(String certPem) throws CertificateException {
        BigInteger serialNumber = getSerialNumber(certPem);
        return DatatypeConverter.printHexBinary(serialNumber.toByteArray());
    }

    public BigInteger getSerialNumber(String certPem) throws CertificateException {
        if (certPem == null) {
            throw new ServiceException("Certificate PEM is null");
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        return x509Cert.getSerialNumber();
    }


    public String getAuthorityKeyIdentifierString(String certPem) throws CertificateException {
        AuthorityKeyIdentifier aki = getAuthorityKeyIdentifier(certPem);
        return authorityKeyIdentifierToString(aki);
    }

    public AuthorityKeyIdentifier getAuthorityKeyIdentifier(String certPem) throws CertificateException {
        if (certPem == null) {
            throw new ServiceException("Certificate PEM is null");
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        byte[] fullExtValue = x509Cert.getExtensionValue(Extension.authorityKeyIdentifier.getId());
        byte[] extValue = ASN1OctetString.getInstance(fullExtValue).getOctets();
        return AuthorityKeyIdentifier.getInstance(extValue);
    }

    /**
     * 获取X509证书的SKI（2.5.29.14)
     */
    public SubjectKeyIdentifier getSubjectKeyIdentifier(String certPem) throws CertificateException {
        if (certPem == null) {
            throw new ServiceException("Certificate PEM is null");
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        byte[] fullExtValue = x509Cert.getExtensionValue(Extension.subjectKeyIdentifier.getId());
        byte[] extValue = ASN1OctetString.getInstance(fullExtValue).getOctets();
        return SubjectKeyIdentifier.getInstance(extValue);
    }

    public String getFabricPrivateKeyName(String certPem) throws CertificateException, NoSuchAlgorithmException {
        return getSKI(certPem) + "_sk";
    }

    /**
     * 根据证书中的公钥计算SKI（fabric称之为SKI），计算结果与直接获取证书中的SKI（2.5.29.14)不同。
     * <p>
     * fabric官方代码中在加载证书和私钥时对私钥的名称有要求，私钥的名称由公钥计算出SKI加上"_sk"构成，
     * 由此程序可以在众多的私钥中找到与公钥相匹配的私钥文件。
     */
    private String getSKI(String certPem) throws CertificateException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        ECPublicKeyImpl publicKey = (ECPublicKeyImpl) x509Cert.getPublicKey();
        byte[] encodedPublicValue = publicKey.getEncodedPublicValue();

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha256.digest(encodedPublicValue);
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    private String authorityKeyIdentifierToString(AuthorityKeyIdentifier aki) {
        return DatatypeConverter.printHexBinary(aki.getKeyIdentifier());
    }


    public String packCertAndKey(Enrollment enrollment) throws IOException, CertificateException, NoSuchAlgorithmException {
        String cert = enrollment.getCert();
        PrivateKey priKey = enrollment.getKey();
        return packCertAndKey(cert, priKey);
    }

    private String packCertAndKey(String cert, PrivateKey parKey) throws IOException, CertificateException, NoSuchAlgorithmException {
        String key = privateKeyToPem(parKey);
        return packCertAndKey(cert, key);
    }

    private String packCertAndKey(String cert, String key) throws IOException, CertificateException, NoSuchAlgorithmException {
        return packCertAndKey(cert, key, null);
    }

    public String packCertAndKey(String cert, String key, String certFileName) throws IOException, CertificateException, NoSuchAlgorithmException {
        if (StringUtils.isEmpty(certFileName)) {
            certFileName = "cert.pem";
        }

        String fileName = getSerialNumberString(cert);
        String tempDir = ExternalResources.getTemp(fileName);
        //创建临时目录
        FileUtils.makeDir(tempDir);
        //保存证书和私钥到文件
        FileUtils.writeStringToFile(tempDir, cert, certFileName);
        String privateKeyName = getFabricPrivateKeyName(cert);
        FileUtils.writeStringToFile(tempDir, key, privateKeyName);
        //打包文件
        ZipFileUtils zipFileUtils = new ZipFileUtils();
        String zipFile = zipFileUtils.createZip(tempDir, ExternalResources.getTemp(fileName + ".zip"));
        //删除临时文件
        FileUtils.delete(tempDir);

        return zipFile;
    }

}
