package com.anexya.app.cloud.aws;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.anexya.app.cloud.KeyManagementService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.KmsException;

@Component
@Profile("aws")
@RequiredArgsConstructor
public class AwsKeyManagementService implements KeyManagementService {
    private final KmsClient kmsClient;

    @Override
    public byte[] encrypt(byte[] plaintext, String keyId) {
        try {
            final var response = kmsClient.encrypt(EncryptRequest.builder()
                                                                 .keyId(keyId)
                                                                 .plaintext(SdkBytes.fromByteArray(plaintext))
                                                                 .build());
            return response.ciphertextBlob()
                           .asByteArray();
        } catch (KmsException e) {
            throw new RuntimeException("KMS encrypt failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, String keyId) {
        try {
            final var response = kmsClient.decrypt(DecryptRequest.builder()
                                                                 .keyId(keyId)
                                                                 .ciphertextBlob(SdkBytes.fromByteArray(ciphertext))
                                                                 .build());
            return response.plaintext()
                           .asByteArray();
        } catch (KmsException e) {
            throw new RuntimeException("KMS decrypt failed", e);
        }
    }
}
