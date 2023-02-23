package io.confluent.encryption;

import io.confluent.encryption.serializers.common.SecuredStringDeserializer;
import io.confluent.encryption.serializers.common.SecuredStringSerializer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class VaultProviderComplexTest {

    private static final String VAULT_TOKEN = "confluent";

    @Container
    @ClassRule
    public static final VaultContainer<?> vaultContainer;

    /**
     * Testing Scenario:
     * 1. Encrypt the message with key version 1
     * 2. Rotate the key so the vault contains version 2
     * 3. Encrypt the message with key version 2
     * 4. Decrypt both messages v1 & v2
     */
    @Test
    void vaultSerializerWhenKeysUpdate() throws Exception {

        final List<Pair<String, RecordHeaders>> events = new ArrayList<>();

        // Using the master key version 1
        vaultContainer.execInContainer("vault", "kv", "put", "-mount=secret", "master",
                "MasterCipher=" + URLEncoder.encode(symmetricDataKey, StandardCharsets.US_ASCII));

        try (var secured = new SecuredStringSerializer()) {
            secured.configure(producerConfig(), false);
            final var headers = new RecordHeaders();
            var bytes = secured.serialize("ducks", headers, "quack");
            var encrypted = new String(bytes, StandardCharsets.UTF_8);

            printHeaders(headers);
            assertThat(headers.toString()).contains("DataCipher");
            assertThat(headers.toString()).contains("MasterCipher");
            assertThat(encrypted).isNotEqualTo("quack");

            events.add(new ImmutablePair<>(encrypted, headers));
        }

        // Using the master key version 2
        vaultContainer.execInContainer("vault", "kv", "put", "-mount=secret", "master",
                "MasterCipher=" + URLEncoder.encode(anotherSymmetricDataKey, StandardCharsets.US_ASCII));

        try (var secured = new SecuredStringSerializer()) {
            secured.configure(producerConfig(), false);
            final var headers = new RecordHeaders();
            var bytes = secured.serialize("ducks", headers, "quack quack");
            var encrypted = new String(bytes, StandardCharsets.UTF_8);

            printHeaders(headers);
            assertThat(headers.toString()).contains("DataCipher");
            assertThat(headers.toString()).contains("MasterCipher");
            assertThat(encrypted).isNotEqualTo("quack quack");

            events.add(new ImmutablePair<>(encrypted, headers));
        }

        try (var secured = new SecuredStringDeserializer()) {
            secured.configure(consumerConfig(), false);

            final var firstPair = events.get(0);
            final var first = secured.deserialize("ducks", firstPair.getRight(), firstPair.getLeft().getBytes());
            assertThat(first).isEqualTo("quack");

            final var secondPair = events.get(1);
            final var second = secured.deserialize("ducks", secondPair.getRight(), secondPair.getLeft().getBytes());
            assertThat(second).isEqualTo("quack quack");
        }
    }

    private static void printHeaders(RecordHeaders headers) {
        for (var iter = headers.iterator(); iter.hasNext(); ) {
            final var header = iter.next();
            System.out.println("Header: " + header.key() + ", " + new String(header.value()));
        }
    }

    private Map<String, Object> producerConfig() {
        final var config = new HashMap<String, Object>();
        config.put("value.serializer.key", "DataCipher");
        config.put("encryption.provider.name", "generator");
        config.put("value.serializer.wrapping.key", "MasterCipher");
        config.put("value.serializer.wrapping.key.provider.name", "vault");
        // Data cipher AES generator provider
        config.put("generator.provider.class", "AESGeneratorCipherProvider");
        config.put("generator.provider.cache.expiry", "60");
        config.put("generator.provider.symmetric.key.size", "256");
        config.put("generator.provider.dynamic.iv", "true");
        // Wrapping cipher provider configuration
        config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
        config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
        config.put("vault.provider.token", VAULT_TOKEN);
        config.put("vault.provider.path", "secret/data/master");
        config.put("vault.provider.keys", "MasterCipher");
        config.put("vault.provider.MasterCipher.key.type", "SymmetricKey");
        return config;
    }

    private Map<String, Object> consumerConfig() {
        final var config = new HashMap<String, Object>();
        config.put("value.deserializer.key", "DataCipher");
        config.put("encryption.provider.name", "generator");
        config.put("value.deserializer.wrapping.key", "MasterCipher");
        config.put("value.deserializer.wrapping.key.provider.name", "vault");
        // Data cipher AES generator provider
        config.put("generator.provider.class", "AESGeneratorCipherProvider");
        config.put("generator.provider.cache.expiry", "60");
        config.put("generator.provider.symmetric.key.size", "256");
        config.put("generator.provider.dynamic.iv", "true");
        // Wrapping cipher provider configuration
        config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
        config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
        config.put("vault.provider.token", VAULT_TOKEN);
        config.put("vault.provider.path", "secret/data/master");
        config.put("vault.provider.keys", "MasterCipher");
        config.put("vault.provider.MasterCipher.key.type", "SymmetricKey");
        return config;
    }

    public static final String symmetricDataKey = "qJNVAHSuyNVtxc9FzQYKwpEv5jgXIliQDSOEJuCGWv4=";
    public static final String anotherSymmetricDataKey = "LkPCmoUYgoTjvvTmEqxNgSFmAP9Z1XKD6Y7KrVTnDOM=";

    static {
        vaultContainer = new VaultContainer<>("vault:latest").withVaultToken(VAULT_TOKEN);
    }
}
