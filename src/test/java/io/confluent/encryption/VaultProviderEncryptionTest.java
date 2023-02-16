package io.confluent.encryption;

import io.confluent.encryption.serializers.common.SecuredStringSerializer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Testcontainers
public class VaultProviderEncryptionTest {

    private static final String VAULT_TOKEN = "confluent";

    @Container
    @ClassRule
    public static final VaultContainer<?> vaultContainer;

    @Test
    void vaultSerializerWhenPublicAndPrivate() {
        final var config = new HashMap<String, String>();
        config.put("value.serializer.key", "SymmetricDataKey");
        config.put("value.serializer.wrapping.key", "PublicMasterKey");
        config.put("value.serializer.wrapping.key.properties", "store.id=false"); // required here
        config.put("encryption.provider.name", "vault");
        config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
        config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
        config.put("vault.provider.token", VAULT_TOKEN);
        config.put("vault.provider.path", "secret/data/master");
        config.put("vault.provider.keys", "SymmetricDataKey,PrivateMasterKey,PublicMasterKey");
        config.put("vault.provider.SymmetricDataKey.key.type", "SymmetricKey");
        config.put("vault.provider.PrivateMasterKey.key.type", "PrivateKey");
        config.put("vault.provider.PublicMasterKey.key.type", "PublicKey");

        try (var secured = new SecuredStringSerializer()) {
            secured.configure(config, false);
            final var headers = new RecordHeaders();
            var bytes = secured.serialize("ducks", headers, "quack");
            var encrypted = new String(bytes, StandardCharsets.UTF_8);
            System.out.println(headers);
            Assertions.assertThat(headers.toString()).contains("SymmetricDataKey");
            System.out.println(encrypted);
        }
    }

    @Test
    void testWhenMultipleThreads() throws Exception {
        try (var secured = new SecuredStringSerializer()) {
            secured.configure(producerConfig(), false);

            new Thread(() -> {
                final var headers = new RecordHeaders();
                var bytes = secured.serialize("ducks", headers, "quack");
                var encrypted = new String(bytes, StandardCharsets.UTF_8);
                Assertions.assertThat(headers.toString()).contains("WrappingMasterKey");
                System.out.println(">>> " + encrypted);
            }).start();

            Thread.sleep(100);

            new Thread(() -> {
                final var headers = new RecordHeaders();
                var bytes = secured.serialize("ducks", headers, "quack");
                var encrypted = new String(bytes, StandardCharsets.UTF_8);
                Assertions.assertThat(headers.toString()).contains("WrappingMasterKey");
                System.out.println(">>> " + encrypted);
            }).start();

            Thread.sleep(100);
        }
    }

    private Map<String, Object> producerConfig() {
        final var config = new HashMap<String, Object>();
        // encryption
        config.put("value.serializer.key", "GeneratedDataCipher");
        config.put("value.serializer.key.provider.name", "cachedlocal");
        config.put("value.serializer.wrapping.key", "WrappingMasterKey");
        config.put("value.serializer.wrapping.key.provider.name", "cachedvault");
        // default cipher
        config.put("encryption.provider.name", "vault");
        // data cipher
        config.put("cachedlocal.provider.class", "CacheCipherProvider");
        config.put("cachedlocal.provider.name", "generator");
        config.put("generator.provider.class", "GeneratorCipherProvider");
        config.put("generator.provider.name", "local");
        config.put("local.provider.class", "LocalCipherProvider");
        // master cipher
        config.put("cachedvault.provider.class", "CacheCipherProvider");
        config.put("cachedvault.provider.expiry", "600"); // seconds
        config.put("cachedvault.provider.name", "vault");
        config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
        config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
        config.put("vault.provider.token", VAULT_TOKEN);
        // public key from vault
        config.put("vault.provider.path", "secret/data/master-public");
        config.put("vault.provider.keys", "WrappingMasterKey");
        config.put("vault.provider.WrappingMasterKey.key.type", "PublicKey");
        return config;
    }

    private static final String privateMasterKey = """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDTC8epcy/VG/5I
            0TEOOJb/1TiL2ZEeQmRk9AgNpMdpqEnWQiR6hn7mpbSGiZEjkfcuIegSMJyZrfGl
            RxHd2Cpau5HHxnjDYDRoq3qEhgz5QEi2Yes9TW+tr3xHn9SHmo6jEiKZJmvp78m7
            MrlcVTJN3UssgjK7nWJWrkF5S0mSXZDZPWhOyRBFyeH1j9pwzeoCbmNGYDNGe1CN
            imPFC4GpFmKi0rVVF8R3AQZQVWhKLfmXNbhuWz4KvbxBGydPk75YAO9VQOHen3wp
            1VCoGix82aVZrgX99qZr5F4h07cxbTgb3HzefKC0aDtO92sTjOwjYAS/GfdITQar
            NIMtDjazAgMBAAECggEAdQpOYznfKAeLQOTKmMFndF/SKTSyVx6owJfZM3/Tm1Pa
            BvnNcynNjKoO8KGwHRLhCvOA2T2eS1f88BT24pnz+zeUhD8cT/W1eurGWmJhJYWy
            DXtGU0vziFYOzUXlPoLHYcY368k0BZuWu7tIst0Itcfo3bki24tPsxSWeOb42TMK
            Mhe/vUlNtpFBiLh/HClbR4YlweOW34/s8KyjyfCvil/oDBSxe6O3RKCKWDEWRIPs
            sj0c4zkKL3eU1rHarbDOxVU/XjfL5I76GtZDKIKcbjWcXGqu1E9mCXxQ7X7f5onW
            JqrDiG+ikhUqR+o9Am17b5Pj7msXFVVtN5bKx1lMsQKBgQD4kEGLlFlNhKpKK/ab
            INX+uDbuoVTKXkKPDmmaEqt/YeLsZm5G/e4acWpaTV6SddVpojQC6PSWwAHeqhz+
            nBjo9rLbrwpKemwHKLLiREDEWOfz5O7axz3aVbCacyTRwrCvRAdmc1mSlHqyRGpF
            lmHVvoRL4I0yUfGhkdLzdsrpSwKBgQDZXC2VkQF4z702z61SoqoHGyCIpCljmip0
            Kukcpv8LDdWv84rFv9RTDU17YmbuW642DSAWVxb692jav9HaIDOZJjHj7O/dnukQ
            l4/x/pPt1iBC8trwpWy3fCftnw/VqoKoza7hshfT3ndnhSYVukoPJxj9rRqEvFed
            nCN7w3KvOQKBgQCl7IYUir7iRED4qkhAXbb8BjSFLyTOwgVkQZa0xum8ard0XxwW
            P8QF2tfH2AXddtMzZJebqa48Q4f7/0rDm6f1O1lB1KLl/LrIPJ7M9ArYBxEveg8H
            9CCR8smlEF/vtisDlEHsXevZMUyGW60lLrG4YSknDYqzoIfP9uv+obnp6QKBgF3E
            ViXfmDL16zpp/OtZIOBP44kOyIwfRZE3sbiakgvCvBxbg9IrHv11D6fam2zYQB8x
            KlZ424EImkND3NOscJXw1DTvUcrZctGlkINkv4wqg2BeQ/TsaCn/dXoc1EfodtU7
            ZeYXXhAYOIp/9h4Jb0l6JF3K/1WjqmhjoysQ9biBAoGBANkAGLVLdz/I1wIfWz87
            LFAdIsbf9I4ZuRANn6MOIIg4W2szIurzuUi4oCNrrkjCqPUs7uqLbmgHdZ/IPd1Z
            eansBFIAW0dBpu4BOmYmWvODAzK0AOG4Ofbyg22cmbITmHkQLO1jYyGX6mIOu7Ij
            jC2cGeUSECRuvUkbCJJYhH4H
            -----END PRIVATE KEY-----
            """;

    private static final String publicMasterKey = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0wvHqXMv1Rv+SNExDjiW
            /9U4i9mRHkJkZPQIDaTHaahJ1kIkeoZ+5qW0homRI5H3LiHoEjCcma3xpUcR3dgq
            WruRx8Z4w2A0aKt6hIYM+UBItmHrPU1vra98R5/Uh5qOoxIimSZr6e/JuzK5XFUy
            Td1LLIIyu51iVq5BeUtJkl2Q2T1oTskQRcnh9Y/acM3qAm5jRmAzRntQjYpjxQuB
            qRZiotK1VRfEdwEGUFVoSi35lzW4bls+Cr28QRsnT5O+WADvVUDh3p98KdVQqBos
            fNmlWa4F/fama+ReIdO3MW04G9x83nygtGg7TvdrE4zsI2AEvxn3SE0GqzSDLQ42
            swIDAQAB
            -----END PUBLIC KEY-----
            """;

    public static final String symmetricDataKey = "qJNVAHSuyNVtxc9FzQYKwpEv5jgXIliQDSOEJuCGWv4=";

    static {
        try {
            vaultContainer = new VaultContainer<>("vault:latest")
                    .withSecretInVault("secret/master",
                            "SymmetricDataKey=" + URLEncoder.encode(symmetricDataKey, StandardCharsets.US_ASCII.toString()),
                            "RSAWrappingKey=" + URLEncoder.encode(privateMasterKey, StandardCharsets.US_ASCII.toString()),
                            "PrivateMasterKey=" + URLEncoder.encode(privateMasterKey, StandardCharsets.US_ASCII.toString()),
                            "PublicMasterKey=" + URLEncoder.encode(publicMasterKey, StandardCharsets.US_ASCII.toString()))
                    .withSecretInVault("secret/master-public",
                            "WrappingMasterKey=" + URLEncoder.encode(publicMasterKey, StandardCharsets.US_ASCII))
                    .withSecretInVault("secret/master-private",
                            "WrappingMasterKey=" + URLEncoder.encode(privateMasterKey, StandardCharsets.US_ASCII))
                    .withVaultToken(VAULT_TOKEN);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
