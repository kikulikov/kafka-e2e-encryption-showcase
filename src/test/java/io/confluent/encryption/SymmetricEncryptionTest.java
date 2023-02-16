package io.confluent.encryption;

import io.confluent.encryption.serializers.common.SecuredStringDeserializer;
import io.confluent.encryption.serializers.common.SecuredStringSerializer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

class SymmetricEncryptionTest {

    // config.put("value.serializer.wrapping.key.properties", "store.id=false,cache=false,ignore.errors=false");

    @Test
    void symmetricKeySerializerWithAnotherKey() {
        final var config = new HashMap<String, String>();
        // serializer configuration
        config.put("value.serializer.key", "SymmetricDataKey");
        config.put("value.serializer.wrapping.key", "SymmetricMasterKey");
        // deserializer configuration
        config.put("value.deserializer.key", "SymmetricDataKey");
        config.put("value.deserializer.wrapping.key", "SymmetricMasterKey");
        // shared configuration
        config.put("encryption.provider.name", "local");
        config.put("value.serializer.wrapping.key.provider.name", "local");
        config.put("value.serializer.wrapping.key.properties", "store.id=false"); // store.id=false
        config.put("local.provider.class", "LocalCipherProvider");
        config.put("local.provider.keys", "SymmetricDataKey,SymmetricMasterKey");
        config.put("local.provider.SymmetricDataKey.key.type", "SymmetricKey");
        config.put("local.provider.SymmetricDataKey.key", "qJNVAHSuyNVtxc9FzQYKwpEv5jgXIliQDSOEJuCGWv4=");
        config.put("local.provider.SymmetricMasterKey.key.type", "SymmetricKey");
        config.put("local.provider.SymmetricMasterKey.key", "ClAX5Qad/qaSsWFZDdASf5rCgE5fY+90n5/Z55nCZr8=");

        try (var stringSerializer = new SecuredStringSerializer()) {
            stringSerializer.configure(config, false);
            final var headers = new RecordHeaders();
            final var encryptedBytes = stringSerializer.serialize("test", headers, "hello");
            final var encryptedString = new String(encryptedBytes, StandardCharsets.UTF_8);
            Assertions.assertThat(headers.toString()).contains("SymmetricDataKey").contains("SymmetricMasterKey");
            Assertions.assertThat(encryptedString).isNotEqualTo("hello");

            try (var stringDeserializer = new SecuredStringDeserializer()) {
                stringDeserializer.configure(config, false);
                final var plaintext = stringDeserializer.deserialize("test", headers, encryptedBytes);
                Assertions.assertThat(plaintext).isEqualTo("hello");
            }
        }
    }

    @Test
    void symmetricKeySerializerWithGeneratedKey() {
        final var config = new HashMap<String, String>();
        // serializer configuration
        config.put("value.serializer.key", "GeneratedDataKey");
        config.put("value.serializer.wrapping.key", "SymmetricMasterKey");
        // deserializer configuration
        config.put("value.deserializer.key", "GeneratedDataKey");
        config.put("value.deserializer.wrapping.key", "SymmetricMasterKey");
        // shared configuration
        config.put("encryption.provider.name", "generator");
        config.put("value.serializer.wrapping.key.provider.name", "local");
        config.put("generator.provider.class", "AESGeneratorCipherProvider");
        config.put("generator.provider.symmetric.key.size",  "256");
        config.put("local.provider.class", "LocalCipherProvider");
        config.put("local.provider.keys", "SymmetricMasterKey");
        config.put("local.provider.SymmetricMasterKey.key.type", "SymmetricKey");
        config.put("local.provider.SymmetricMasterKey.key", "ClAX5Qad/qaSsWFZDdASf5rCgE5fY+90n5/Z55nCZr8=");

        try (var stringSerializer = new SecuredStringSerializer()) {
            stringSerializer.configure(config, false);
            final var headers = new RecordHeaders();
            final var encryptedBytes = stringSerializer.serialize("test", headers, "hello");
            final var encryptedString = new String(encryptedBytes, StandardCharsets.UTF_8);
            Assertions.assertThat(headers.toString()).contains("GeneratedDataKey").contains("SymmetricMasterKey");
            Assertions.assertThat(encryptedString).isNotEqualTo("hello");

            try (var stringDeserializer = new SecuredStringDeserializer()) {
                stringDeserializer.configure(config, false);
                final var plaintext = stringDeserializer.deserialize("test", headers, encryptedBytes);
                Assertions.assertThat(plaintext).isEqualTo("hello");
            }
        }
    }
}
