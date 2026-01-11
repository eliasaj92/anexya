package com.anexya.app.cloud;

public interface KeyManagementService {
    byte[] encrypt(byte[] plaintext, String keyId);

    byte[] decrypt(byte[] ciphertext, String keyId);
}
