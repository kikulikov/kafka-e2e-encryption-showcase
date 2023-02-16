package io.confluent.encryption;

import io.confluent.encryption.serializers.common.SecuredStringDeserializer;
import io.confluent.encryption.serializers.common.SecuredStringSerializer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

class KeyPairEncryptionTest {

    @Test
    void keyPairSerializerWithGeneratedKey() {
        final var config = new HashMap<String, String>();
        // serializer configuration
        config.put("value.serializer.key", "DataCipher");
        config.put("value.serializer.wrapping.key", "MasterCipher");
        // deserializer configuration
        config.put("value.deserializer.key", "DataCipher");
        config.put("value.deserializer.wrapping.key", "MasterCipher");
        // encryption
        config.put("encryption.provider.name", "generator");
        config.put("value.serializer.wrapping.key.provider.name", "local");
        config.put("value.deserializer.wrapping.key.provider.name", "local");
        config.put("generator.provider.class", "AESGeneratorCipherProvider");
        config.put("generator.provider.symmetric.key.size", "256");
        config.put("local.provider.class", "LocalCipherProvider");
        config.put("local.provider.keys", "MasterCipher");
        config.put("local.provider.MasterCipher.key.type", "KeyPair");
        config.put("local.provider.MasterCipher.key.private", privateMasterKey);
        config.put("local.provider.MasterCipher.key", publicMasterKey);

        try (var stringSerializer = new SecuredStringSerializer()) {
            stringSerializer.configure(config, false);
            final var headers = new RecordHeaders();
            final var encryptedBytes = stringSerializer.serialize("test", headers, "hello");
            final var encryptedString = new String(encryptedBytes, StandardCharsets.UTF_8);
            Assertions.assertThat(headers.toString()).contains("DataCipher").contains("MasterCipher");
            Assertions.assertThat(encryptedString).isNotEqualTo("hello");

            try (var stringDeserializer = new SecuredStringDeserializer()) {
                stringDeserializer.configure(config, false);
                final var plaintext = stringDeserializer.deserialize("test", headers, encryptedBytes);
                Assertions.assertThat(plaintext).isEqualTo("hello");
            }
        }
    }

    private static final String publicMasterKey = """
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA52ksO3lnKaagltHTCfUNWgvmlL4OytMU2HxErcjDspvWvhCo3K/+oVNC5CFJLJkpixgYGFQHstjj1xZjtQd0G78ZWvRD7XUP6opSZbgexx9s9dKHgbku7ZHaPswP7uABwzgn2vfspOOyFjSzwY5FbOMTKN3C+q1cjOYbuYVat5UVWG1VdgE+6GHTNwupxMOZHRNNbkoI5HKXe0YOfGYizWHa/da8BG5ccaBeNLXRwbOaEOA1jMsdYWdZ6cnpBLFWLKvwArBGiDBkFzWbC2e115gFdeLGV70lbB4lBpL/+yyoLFBGdJVB/T0arOIyvup0kDsYi6jOsgkQM6+TEojpVwIDAQAB
            """;

    private static final String privateMasterKey = """
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDnaSw7eWcppqCW0dMJ9Q1aC+aUvg7K0xTYfEStyMOym9a+EKjcr/6hU0LkIUksmSmLGBgYVAey2OPXFmO1B3Qbvxla9EPtdQ/qilJluB7HH2z10oeBuS7tkdo+zA/u4AHDOCfa9+yk47IWNLPBjkVs4xMo3cL6rVyM5hu5hVq3lRVYbVV2AT7oYdM3C6nEw5kdE01uSgjkcpd7Rg58ZiLNYdr91rwEblxxoF40tdHBs5oQ4DWMyx1hZ1npyekEsVYsq/ACsEaIMGQXNZsLZ7XXmAV14sZXvSVsHiUGkv/7LKgsUEZ0lUH9PRqs4jK+6nSQOxiLqM6yCRAzr5MSiOlXAgMBAAECggEAKC3fqzfqDJZM3cLyxJDSz6avU3YodVjvDqOM/SuVpwZd48RavW6lZHdjbt7EqMSzLN5zGI5Gg+waqzbM+xqfM04b5enxfWJM8CkyI00zstkm/wud0Y1Is6EWZr7hqVUlmTK/4MoZQYvzWN2vtFSygzRuGDNg7kt5fVFa0PxxgtxAPwOXFWDMT7bU7csjrsOtxm7bgwm+rxSCSPU9J834TTzQlPID8tP9Ajvj4TrllZN55oKdDNO5aVaMkYRKMBbg/mzHpQE9wWeKX1iGYR0x8N3ctKJvAC6SYcnOiAKKPg0C5Qi6RFg88GdaYq1tUtDbjcP5CUWMZ2X9M5XEqO2WAQKBgQD3iRy7Fm91EFy2duJxNULe0qU5hbRekCmEX7/5HNk+BjWDFLLVFkgtWop7+p+G/TwmmPdpEtRYCbT1k2qHrPgohReo2VzF+K7Q3yppnFu2dPy5T2AL02J9J4BCzxB1uwzpnGd2PqwUX79WsF2ol18F04sMeXUzuKggOm7M2nl8VwKBgQDvUugqw3Yfl9TF5VkVyA26/FOfMYQZ2ovzpTg9Qq7il1F37h8Lg83zvI63NXUlY2khweo0FyMbtZpG0a9dpQBE0f4D2n9JsmpuGbgjYYNNEWUQvuI3bqdQ/ecPpMGvYXWvtnTPG0jr6GbSobL/RXwgOp4H8tOpFNEejaARjDfbAQKBgQCmpYdTKMqGnCpeqPDP2FSZoGSdsjb5BsL8nF2ov1Q93n4+LjwrGuIirnbW+qZVgbzyGz9NXODaGEbcoY8xojA7T0bbZOKBYWeHtQZfrWVNE7tkolx9+aSvr105HR/usqwBxksdHxpIaSFuojObobTWPlG5ZzeRR3rgn5Yikd8B5wKBgQC2+GLJ9wBDbTheSFdQoM9miu1/w0Kk5YKkN0gFBgtg76F4mJQhoJZ/50QRbAxxFkzVY0UkqB/OWoxl4oA5jyHie83BsnYoqQBXxtASNMZG0Kq9H8Mh8DZ5ZHUYb7Uo2dE5ErzBbrHUsqySEtAf/EbG+SnDF/KczW6H9m1PnS1DAQKBgCANQP+nXYE2/81+wmlPqOZqpXdZaTc8tNB2vHfpamw/OCZqoRmZB9iUPOVfeTH6JmlL3K1BRaDnYJci/rWP/QjGLcterlihnZvOzVOfBgsS8MXw1jUjThNI6YAzhEOKBantke/ngwC+3X0s7GoEUIP+D3zdGAk34RhvEryeF+dY
            """;
}
