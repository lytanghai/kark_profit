package com.money.kark_profit.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "rest.client")
public class RestClientProperties {

    private RetryConfig retry = new RetryConfig();
    private ConnectionConfig connection = new ConnectionConfig();

    private Map<String, ClientConfig> clients = new HashMap<>();
    private ClientConfig defaultConfig = new ClientConfig();


    public ClientConfig getClientConfig(String clientName) {
        return clients.getOrDefault(clientName, defaultConfig);
    }

    @Setter
    @Getter
    public static class RetryConfig {
        private boolean enabled;
        private int maxAttempts;
        private long initialDelayMillis;
        private List<String> retryableExceptions;

    }

    @Setter
    @Getter
    public static class ConnectionConfig {
        private int connectTimeout;
        private int readTimeout;
    }

    // Client-specific configuration (can override global settings)
    @Setter
    @Getter
    @Component
    public static class ClientConfig {
        private RetryConfig retry;
        private ConnectionConfig connection;
        private String baseUrl;

        public RetryConfig getEffectiveRetry(RetryConfig defaultRetry) {
            if (retry != null) {
                RetryConfig merged = new RetryConfig();
                merged.setEnabled(retry.isEnabled());
                merged.setMaxAttempts(retry.getMaxAttempts());
                merged.setInitialDelayMillis(retry.getInitialDelayMillis());
                merged.setRetryableExceptions(retry.getRetryableExceptions());
                return merged;
            }
            return defaultRetry;
        }

        public ConnectionConfig getEffectiveConnection(ConnectionConfig defaultConnection) {
            if (connection != null) {
                ConnectionConfig merged = new ConnectionConfig();
                merged.setConnectTimeout(connection.getConnectTimeout());
                merged.setReadTimeout(connection.getReadTimeout());
                return merged;
            }
            return defaultConnection;
        }
    }
}