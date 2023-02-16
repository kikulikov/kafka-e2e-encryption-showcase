package io.confluent.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"io.confluent.controller", "io.confluent.datasource"})
public class BasicWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicWebApplication.class, args);
    }
}
