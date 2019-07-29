package org.atoko.call4code.entrado.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.UUID;

public class Encryption {
    static final byte[] encryptionKey = UUID.randomUUID().toString().getBytes();
    static Cipher encryptCipher;
    static Cipher decryptCypher;
    static private ObjectMapper objectMapper = new ObjectMapper();

    static {
        try {
            SecureRandom sr = new SecureRandom(encryptionKey);
            KeyGenerator kg = KeyGenerator.getInstance("RC4");
            kg.init(sr);
            SecretKey sk = kg.generateKey();
            // create an instance of encryptCipher
            encryptCipher = Cipher.getInstance("RC4");
            encryptCipher.init(Cipher.ENCRYPT_MODE, sk);


            decryptCypher = Cipher.getInstance("RC4");
            decryptCypher.init(Cipher.DECRYPT_MODE, sk);
        } catch (Exception e) {

        }
    }

    public static String encrypt(byte[] key) {
        try {
            return objectMapper.writeValueAsString(encryptCipher.doFinal(key));
        } catch (Throwable t) {
            LoggerFactory.getLogger("ACTIVITY_VIEW").error("Error creating crypted ID", t);
            return "";
        }
    }

    public static String decrypt(String encrypted) {
        try {
            byte[] bytes = objectMapper.readTree(encrypted).binaryValue();
            return new String(decryptCypher.doFinal(bytes));
        } catch (Throwable t) {
            LoggerFactory.getLogger("ACTIVITY_VIEW").error("Error reading crypted ID", t);
            return "";
        }
    }
}
