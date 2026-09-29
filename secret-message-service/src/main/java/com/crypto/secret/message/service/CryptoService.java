package com.crypto.secret.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class CryptoService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${crypto.password.length}")
    private int passwordLength;

    public String generatePassword() {
        byte[] bytes = new byte[passwordLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String encrypt(String content, String password) {
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            SecretKey key = deriveKey(password);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encryptedData = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encryptedData.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed");
        }
    }

    public String decrypt(String encryptedContent, String password) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedContent);

            byte[] iv = new byte[12];
            byte[] cipherText = new byte[decoded.length - 12];

            System.arraycopy(decoded, 0, iv, 0, 12);
            System.arraycopy(decoded, 12, cipherText, 0, cipherText.length);

            SecretKey key = deriveKey(password);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed");
        }
    }

    private SecretKey deriveKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
