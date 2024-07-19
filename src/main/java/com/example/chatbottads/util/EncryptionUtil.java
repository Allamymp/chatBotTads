package com.example.chatbottads.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final SecretKeySpec SECRET_KEY;

    static {
       // String key = System.getenv("ENCRYPTION_KEY");
        String key = "your_16_char_key";
        if (key == null || key.length() != 16) {
            throw new IllegalArgumentException("Invalid encryption key. The key must be 16 characters long.");
        }
        SECRET_KEY = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    public static String encrypt(Long id) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            byte[] encryptedBytes = cipher.doFinal(String.valueOf(id).getBytes());
            return Base64.getUrlEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting the ID", e);
        }
    }

    public static Long decrypt(String encryptedId) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encryptedId); // Utilizando URL-safe decoder
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return Long.parseLong(new String(decryptedBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting the ID", e);
        }
    }
}
