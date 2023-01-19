package io.confluent.streams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("spring.kafka.properties.basic.auth")
public class BasicAuthConfig {

    //    basic.auth.credentials.source=USER_INFO
    private String credentials_source;

    //    basic.auth.user.info={{ SR_API_KEY }}:{{ SR_API_SECRET }}
    private String user_info;

    public Map<String, String> asMap() {
        final var config = new HashMap<String, String>();

        if (credentials_source != null && !credentials_source.isBlank()) {
            config.put("basic.auth.credentials.source", credentials_source);
        }

        if (user_info != null && !user_info.isBlank()) {
            config.put("basic.auth.user.info", user_info);
        }

        return config;
    }

    public void setCredentials_source(String credentials_source) {
        this.credentials_source = credentials_source;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }
}
