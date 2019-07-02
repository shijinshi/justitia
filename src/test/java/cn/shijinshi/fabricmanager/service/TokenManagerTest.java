package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.service.helper.CertFileHelper;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.junit.Test;
import sun.security.ec.ECPublicKeyImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class TokenManagerTest {

    @Test
    public void test() throws CertificateException {
        String certPem = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIJKAIBAAKCAgEAq8SBdRoA+tbDbvzY2BuztivinuGY4Me/3LtK/4UlEVUbSglQ\n" +
                "G0I7zF64se+JeCpHXUFtQzoqUp8+UJscwx04b2bMR9sJZ5ByTXvPXpwk4GYGOidf\n" +
                "yfVMhqNwmuOdC2pI3N7C/bZx6O/r2Nloib/28NIVh6zzwF4mDFWbM+RqVPVNn6cH\n" +
                "yvWprCEW6xyNA3n7/WXGxXzcXHlHsrCjlY72y0yhaxDwaa4Z+Xzb/n0Er+42rEIx\n" +
                "4aUtSdca2cdp8yCFHe5U6XMsX8P1oXWecheAqtgqz+PnsYtS8EilCaovTu4MwVjn\n" +
                "/ScXNPLpM4SlhZ4Dp+Q2nycqaPPyyypTEHgIrDctiM21AYq0aKuGShDk1n8MFQhk\n" +
                "wfASuymWGUoIe/jz14C8KA7GbYl9ZUlJmbv9KAPM0b31DiYrb23Pu+9EybxUh3qw\n" +
                "a7aHL7oNeUFulkfNFmFeC1aXZu6Q7dEmp/N31JZiq89JYXLlJmUE9VnDh+clb0Kk\n" +
                "3soKRbQKZFvWduxWx4SEW0z0e143R/PSYtNM29I0Eidu1T1+rE8RUkwPjrUAJkHi\n" +
                "r46zbThE4tqQZKxcOGBFgHxXXMqky0Qtz6C21IQfvlX/juKQ3o9Ouds4d2FMFfNs\n" +
                "QdyaHjnsmLm+7NmQNEQHZsMDQbbtc3ynF45S5r3JDZg0at/bsu32CY12uY8CAwEA\n" +
                "AQKCAgBrpN94co5wpIyfjoeaj2nFXaVM6pZzL9tWSm3KyjcbsJlEgaeZHZ36CtZ2\n" +
                "dGZTTzOgBrOhsN77Hl7gBwU0ZaiLcAHPej5fj8+CLXgck4AsvONkKG7IUxjdO6oC\n" +
                "92fg0n/NIzFR7LdgQpMsN7tisFHiQ9ZIS6QKRzZS/ocgMyJMEdrOlc+1atUlUSta\n" +
                "tm7M4fKAr/qsov46WasMGdS6knipmWGDhn8Ty47tokkAKHr6gi/zLhyyi2BhaROj\n" +
                "rWwBeLzTsBu4nXwOm/DgeaSNXZs99fCGjg4SXvzgJrZ+4w94yIhn1v6z0H6jqSNq\n" +
                "LNPKc+dYnRV6EgJq+Y6Lsf+ST5cmxwT57NX8At6KTwuSoyiU7H1K9edKiYuszh73\n" +
                "eiKW4iLTRXiLVcW/FtXHbVebANmd+Y1McajUKXwwRPN51AcDzKzyCqPqXJTw4YQx\n" +
                "1RPFb1fdsnquPG6K17oaPzdMmkfFy2PI9blGejxHsOYCini+fp4kk2gSMrGj/XF9\n" +
                "C4EhTI9BxFMxm6TlFB3rZAyUB3t3UKZHfRoPirEHj+dKKOb7LjzSj3YDz8J0i9px\n" +
                "tMBZZE7o4lCyxOppIyHPU4eZvs9EOh0cUwo7hAOLtJ38h2QaNd3httRHr3FNnQUc\n" +
                "WLxAIYBqHnhA2IuFDyc+IHoESI2hL+bvYhhr9rC6C2QaR1PDuQKCAQEA2Q1NA7kR\n" +
                "9MByrvD/QdPSLi6WaXsmF4sPULI032ZiXp8jXsSxK7/UlSnsmCe0ROT59llxEd49\n" +
                "eqDxqNSzqTOTWDZ7U5t/b1zN641pZZ8jVtfeHu85nB0mdxt6etrZaENqkbxH5Bcw\n" +
                "mYNDjgXiqlMvU+2nWNNa+RjbgKK+9MaPxc7SnzXw92KOKTvKNcFLwBX9aoRFfYem\n" +
                "a2VvHpeHOCCqU5Q0CBW/Q/Ll/WGwGEgl2X6vtHfDhazhnMqZxJMtPGNAi9z13/bR\n" +
                "jwEoM2AGE94hTVscMSs/bIpoVZNkheOxQvkOjrdfEZikh48o/H2/42EqcRQankTe\n" +
                "clw3r3saT15CIwKCAQEAypb7GMPTjm6yJ4FN+7G0tMfU7ev+SUxMecfW69UpWXIv\n" +
                "B273fhlJAtsztZhZIusvJgUYYxqcuK2dy1cHNGdRVR9CKkX6PkgPrSV68RYffd4u\n" +
                "k8wrBfMMOauanCQT/uUnSRX8VwoyrBmwPpx3/TBZ018N9SVr1sDXmpKCFfl6OP8O\n" +
                "jC3o/fXmsXuFtWP1daNlChlf/+EO1xTeN0VQ+4eYtZGQc5PvZu4nFrEfzYUpInXV\n" +
                "GzeDcmLDP8kJd6HhuVViqENXX3K3tw9jCK3Amqa86XMbWc/gGe0V0uD3CCPOQTyw\n" +
                "RV6x0IRz5Im4uPG4PW9yTZdXD6cxuqQPF7jJqRSTpQKCAQB2RyI9sh9P0YNtQ5TF\n" +
                "yWkSk36RknMqhzPcVJAtYPyf9XZv0R1+6rUsZ8EvFQvOMzznesv74bB837FwwB/7\n" +
                "R0i23FGOOQFTrS3le5UAtJysWUwIbXJmWRg59bZjtlic0YgETKaca+qm7PnmLdzb\n" +
                "1iLHSI1WAS7k6R/MVGNcvcthDpiF+/4IwSMY4yUI8AGYdiM3ZUb85evRQGAyOn6M\n" +
                "HnjSvMMx2K/2limwTwiH4uEFSVz0KgNZF+GF8H2kulL/QlLNJKaz2itOE2JVxLDr\n" +
                "KLPbFJgjTx7rxeyfHYe80lwpU9EUAmcDDrwxxtid5uSnQq6/GiZ7MqpJhWku7hVj\n" +
                "RMkjAoIBAQCfPDzxIjrO4UM2ufPJucwHrGo4+B1PD6Gstt6/TzTJEW7VsOrnOoig\n" +
                "rhreyXiGccUFZUvd2bUFk7FLeoQ3NF5BQeiHwakH+ywXNDaF+JWYIWMK2JeYfYlG\n" +
                "axODVvhAiW1vjNBiualKjIZzMBk/8RkpgrU90sQIcFsHK9ED5Kuk0RVmKU/RDc7e\n" +
                "AfD+uirc5Cku3vMN9J/tfoGLf/g/PD1Tua6IqeaqSNaAh4gvylKiu2tdkSzIY8iE\n" +
                "Akhmt97sBakeCJBMfabZREbbM3cIF7a057y/wxyqRCe7MQf6ZepgbItAe0XYV8Og\n" +
                "BoAjHhtqboaxZhsQOqJjyT/zrVMUiw/hAoIBAB8BzUeBXXomWi1yuzteTEO+cLna\n" +
                "3AOESYEQmwpADK7oxDw4MQLJI6Q9UYk7Yv4+3cy/4xKi+JSGITMmodPkxUGkCCXe\n" +
                "ymMi+vW7/q8WQVccofq8P2Q7w+L59IMfogwzGLUaquKhrB74hf673dES5Ro16Vgs\n" +
                "76m0/uQzZbkaIJh6tlVP5B9IbYbRXvVsURfkxNL/aB7XfvIlegkfce3IfEWPXJmf\n" +
                "lseaQU/NLFBwHFKkZGlIBpgTIIGNVrnGgcMQ7bpXa0AFtB/S6uGGLQDi7PWefL/f\n" +
                "auDGNetKYKRJJsE7V9L4xqqnRSaHgyBHgwr84fp+h0UQOpu0B/OxKDfWXho=\n" +
                "-----END RSA PRIVATE KEY-----\n";

        CertFileHelper fileHelper = new CertFileHelper();
        AuthorityKeyIdentifier aki = fileHelper.getAuthorityKeyIdentifier(certPem);
        System.out.println(Arrays.toString(aki.getKeyIdentifier()));
        System.out.println(DatatypeConverter.printHexBinary(aki.getKeyIdentifier()));
        SubjectKeyIdentifier ski = fileHelper.getSubjectKeyIdentifier(certPem);
        System.out.println(Arrays.toString(ski.getKeyIdentifier()));
        System.out.println(DatatypeConverter.printHexBinary(ski.getKeyIdentifier()));
        String serialNumberString = fileHelper.getSerialNumberString(certPem);
        System.out.println("serial:"+serialNumberString);



    }

    @Test
    public void test2() throws CertificateException, NoSuchAlgorithmException {
        String certPem = "-----BEGIN CERTIFICATE-----\n" +
                "MIICAjCCAamgAwIBAgIUHBxbT/czlYvkKHyZEOZr02ZzaKEwCgYIKoZIzj0EAwIw\n" +
                "XjELMAkGA1UEBhMCQ0gxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\n" +
                "EwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMQ8wDQYDVQQDEwZjbm5hbWUw\n" +
                "HhcNMTkwMTEwMDg1MzAwWhcNMzQwMTA2MDg1MzAwWjBeMQswCQYDVQQGEwJDSDEX\n" +
                "MBUGA1UECBMOTm9ydGggQ2Fyb2xpbmExFDASBgNVBAoTC0h5cGVybGVkZ2VyMQ8w\n" +
                "DQYDVQQLEwZGYWJyaWMxDzANBgNVBAMTBmNubmFtZTBZMBMGByqGSM49AgEGCCqG\n" +
                "SM49AwEHA0IABCygIUilREuQHDe/uJVsV2iG536ZveD2vg3HoaAJXfWbVhA1yRlN\n" +
                "iU5ORPfzyfAZr8sIqqM7wSx2nkp+/aVP8gSjRTBDMA4GA1UdDwEB/wQEAwIBBjAS\n" +
                "BgNVHRMBAf8ECDAGAQH/AgEBMB0GA1UdDgQWBBRxFBcKuMswyv1xgCexMct84+P+\n" +
                "bTAKBggqhkjOPQQDAgNHADBEAiA1zPKOyOrcjAfVDFOey6tW28FyTx/Gd0uY3UOW\n" +
                "+DQaEAIgJeySgabbX5ubou5btEQyiqi1XQVmM9sLFBuYOn9bL7c=\n" +
                "-----END CERTIFICATE-----\n";

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        ECPublicKeyImpl publicKey = (ECPublicKeyImpl) x509Cert.getPublicKey();
        byte[] encodedPublicValue = publicKey.getEncodedPublicValue();

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha256.digest(encodedPublicValue);
        System.out.println(DatatypeConverter.printHexBinary(digest).toLowerCase());

//        EllipticCurve curve = new EllipticCurve()
        System.out.println();
    }
}