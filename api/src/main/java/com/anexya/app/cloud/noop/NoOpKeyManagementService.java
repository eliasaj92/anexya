package com.anexya.app.cloud.noop;

import com.anexya.app.cloud.KeyManagementService;

public class NoOpKeyManagementService implements KeyManagementService
{
    @Override
    public byte[] encrypt(byte[] plaintext, String keyId)
    {
        return plaintext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, String keyId)
    {
        return ciphertext;
    }
}
