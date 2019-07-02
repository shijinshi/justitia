package cn.shijinshi.fabricmanager.service.utils;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class StringConverter {
    private static final Logger log = Logger.getLogger(StringConverter.class);

    private static final byte[] KEY_BYTES = {-106,-67,79,-104,-41,-86,-12,-75,96,-43,43,-47,46,-46,116,80};
    private static final String ENCRYPT_TYPE = "AES";

    public String getMD5(String clearText) {
        //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.warn(e);
            throw new RuntimeException("Get MD5 Failed.");
        }
        md5.update(clearText.getBytes());
        //转换并返回结果，也是字节数组，包含16个元素
        byte[] digest = md5.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    public String encrypt(String clearText) {
        try {
            Key key = new SecretKeySpec(KEY_BYTES, ENCRYPT_TYPE);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(clearText.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.warn(e);
            throw new RuntimeException("Encrypt string failed.");
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decode = Base64.getDecoder().decode(cipherText);
            Key key = new SecretKeySpec(KEY_BYTES, ENCRYPT_TYPE);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(decode);
            return new String(bytes);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.warn(e);
            throw new RuntimeException("Decrypt string failed.");
        }
    }

    /**
     * 字符串转16进制
     * @return
     */
    public String strToHex(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<str.length();i++) {
            int ch = (int) str.charAt(i);
            sb.append(Integer.toHexString(ch));
        }
        return sb.toString();
    }
}
