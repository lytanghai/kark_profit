package com.money.kark_profit.config;

import com.money.kark_profit.config.properties.RestClientProperties;
import com.money.kark_profit.http.RestClientHttpProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RestClientFactory {
    
    private final RestClientProperties properties;
    private final ConcurrentHashMap<String, RestClientHttpProvider> clientCache = new ConcurrentHashMap<>();
    
    public RestClientHttpProvider getClient(String clientName) {
        return clientCache.computeIfAbsent(clientName, name -> {
            RestClientProperties.ClientConfig clientConfig = properties.getClientConfig(name);
            return new RestClientHttpProvider(properties, clientConfig);
        });
    }
    
    public RestClientHttpProvider getDefaultClient() {
        return getClient("default");
    }
}