package io.confluent.generator;

import com.github.javafaker.Faker;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.Customer;
import io.confluent.model.avro.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DataSourceFaker implements DataSource {

    @Override
    public Order retrieveOrder() {
        return new Order(FAKER.internet().uuid(), retrieveBook().getBookId(),
                retrieveCustomer().getCustomerId(), FAKER.random().nextInt(1, 10)
        );
    }

    @Override
    public Book retrieveBook() {
        final var title = FAKER.harryPotter().book();
        return new Book(toHex(md5(title)), title);
    }

    @Override
    public Customer retrieveCustomer() {
        final var name = FAKER.harryPotter().character();
        return new Customer(toHex(md5(name)), name);
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