package io.confluent.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({"io.confluent.generator", "io.confluent.producer"})
@SpringBootApplication
@EnableScheduling
public class BasicProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicProducerApplication.class, args);
    }
}
