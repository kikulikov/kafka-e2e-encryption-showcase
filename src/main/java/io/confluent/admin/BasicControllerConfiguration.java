package io.confluent.admin;

import io.confluent.model.avro.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class BasicControllerConfiguration {

    private Map<String, Object> producerConfig() {
        final var config = new HashMap<String, Object>();
        config.put("client.id", "encrypted-producer");
        config.put("bootstrap.servers", "localhost:9092");
        config.put("schema.registry.url", "http://localhost:8081");
        // config.put("key.serializer", "io.confluent.encryption.serializers.common.SecuredStringSerializer");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // config.put("value.serializer", "io.confluent.encryption.serializers.avro.SecuredSpecificAvroSerializer");
        // config.put("value.serializer", "io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer");
        config.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        // TODO
        // config.put("key.serializer.key", "GeneratedDataCipher");
        // config.put("key.serializer.key.provider.name", "keycached");
        // config.put("key.serializer.wrapping.key", "WrappingMasterKey");
        // config.put("key.serializer.wrapping.key.provider.name", "keycachedvault");
        // encryption
        config.put("value.serializer.key", "GeneratedDataCipher");
        config.put("value.serializer.key.provider.name", "cached");
        config.put("value.serializer.wrapping.key", "WrappingMasterKey");
        config.put("value.serializer.wrapping.key.provider.name", "cachedvault");
        // default cipher
        config.put("encryption.provider.name", "vault");
        // TODO
        // config.put("keycached.provider.class", "SharedCachedCipherProvider");
        // config.put("keycached.provider.name", "generator");
        // data cipher
        config.put("cached.provider.class", "SharedCachedCipherProvider");
        config.put("cached.provider.name", "generator");
        config.put("generator.provider.class", "GeneratorCipherProvider");
        config.put("generator.provider.name", "local");
        config.put("local.provider.class", "LocalCipherProvider");
        // TODO
        // config.put("keycachedvault.provider.class", "SharedCachedCipherProvider");
        // config.put("keycachedvault.provider.expiry", "60"); // seconds
        // config.put("keycachedvault.provider.name", "vault");
        // master cipher
        config.put("cachedvault.provider.class", "SharedCachedCipherProvider");
        config.put("cachedvault.provider.expiry", "60"); // seconds
        config.put("cachedvault.provider.name", "vault");
        config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
        config.put("vault.provider.url", "http://localhost:8200");
        config.put("vault.provider.token", "confluent");
        // public key from vault
        config.put("vault.provider.path", "secret/data/master-public");
        config.put("vault.provider.keys", "WrappingMasterKey");
        config.put("vault.provider.WrappingMasterKey.key.type", "PublicKey");
        return config;
    }
}
