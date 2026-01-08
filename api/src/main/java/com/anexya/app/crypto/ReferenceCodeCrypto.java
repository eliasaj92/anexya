package com.anexya.app.crypto;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.anexya.app.cloud.CloudServiceFactory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReferenceCodeCrypto
{

    private final CloudServiceFactory cloudFactory;

    @Value("${app.kms.key-id:}")
    private String keyId;

    public String encrypt(String plaintext)
    {
        if (plaintext == null)
            return null;
        if (keyId == null || keyId.isBlank())
        {
            return plaintext; // no key configured; store as-is
        }
        byte[] ciphertext = cloudFactory.kms().encrypt(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                keyId);
        return Base64.getEncoder().encodeToString(ciphertext);
    }

    public String decrypt(String ciphertextB64)
    {
        if (ciphertextB64 == null)
            return null;
        if (keyId == null || keyId.isBlank())
        {
            return ciphertextB64; // stored in plaintext when no key configured
        }
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextB64);
        byte[] plaintext = cloudFactory.kms().decrypt(ciphertext, keyId);
        return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
    }
}
