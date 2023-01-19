package io.confluent.streams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("spring.kafka.properties.schema.registry")
public class SchemaRegistryConfig {

    // schema.registry.url={{ SR_URL }}
    private String url;

    public Map<String, String> asMap() {
        final var config = new HashMap<String, String>();
        config.put("schema.registry.url", url);
        return config;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
