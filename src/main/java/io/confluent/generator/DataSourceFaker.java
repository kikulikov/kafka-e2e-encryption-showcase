package io.confluent.generator;

import com.github.javafaker.Faker;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.OnlineOrder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
public class DataSourceFaker implements DataSource {

    @Override
    public Book retrieveBook() {
        final var title = FAKER.harryPotter().book();
        final var id = UUID.nameUUIDFromBytes(title.getBytes()).toString();
        return new Book(id, title);
    }

    @Override
    public OnlineOrder retrieveOnlineOrder() {
        return new OnlineOrder(FAKER.internet().uuid(),
                retrieveBook().getBookId(), FAKER.random().nextInt(1, 10));
    }

    private static final Faker FAKER = Faker.instance();

    private byte[] md5(String str) {
        try {
            return MessageDigest.getInstance("MD5").digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("failure", e);
        }
    }

    private static String toHex(byte[] bytes) {
        final var sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }
}