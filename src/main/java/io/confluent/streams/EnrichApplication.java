package io.confluent.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class EnrichApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnrichApplication.class, args);
    }
}
