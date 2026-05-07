package com.money.kark_profit.http;

import com.money.kark_profit.config.RetryExecutor;
import com.money.kark_profit.config.RetryableExceptionMatcher;
import com.money.kark_profit.config.properties.RestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class RestClientHttpProvider {
    
    private static final Logger log = LoggerFactory.getLogger(RestClientHttpProvider.class);
    
    private final RestClient restClient;
    private final RetryExecutor retryExecutor;
    private final String clientName;
    private final String baseUrl;

    public RestClientHttpProvider(RestClientProperties globalProperties,
                                  RestClientProperties.ClientConfig clientConfig) {
        this.clientName = clientConfig != null ? "custom" : "default";
        this.baseUrl = clientConfig != null ? clientConfig.getBaseUrl() : null;

        RestClientProperties.RetryConfig effectiveRetry = clientConfig != null ?
            clientConfig.getEffectiveRetry(globalProperties.getRetry()) :
            globalProperties.getRetry();

        RestClientProperties.ConnectionConfig effectiveConnection = clientConfig != null ?
            clientConfig.getEffectiveConnection(globalProperties.getConnection()) :
            globalProperties.getConnection();

        this.restClient = buildRestClient(effectiveConnection);

        if (effectiveRetry.isEnabled()) {
            RetryableExceptionMatcher matcher = new RetryableExceptionMatcher(
                effectiveRetry.getRetryableExceptions()
            );
            this.retryExecutor = new RetryExecutor(
                effectiveRetry.getMaxAttempts(),
                effectiveRetry.getInitialDelayMillis(),
                matcher
            );
            log.info("RestClient [{}] initialized with MaxAttempts: {}, InitialDelay: {}ms, Timeouts - Connect: {}ms, Read: {}ms",
                clientName, effectiveRetry.getMaxAttempts(), effectiveRetry.getInitialDelayMillis(),
                effectiveConnection.getConnectTimeout(), effectiveConnection.getReadTimeout());
        } else {
            this.retryExecutor = null;
            log.info("RestClient [{}] initialized with retry disabled, Timeouts - Connect: {}ms, Read: {}ms",
                clientName, effectiveConnection.getConnectTimeout(), effectiveConnection.getReadTimeout());
        }
    }
    
    private String resolveUrl(String url) {
        if (baseUrl != null && !url.startsWith("http"))
            return baseUrl + url;

        return url;
    }
    
    public <T> T get(String url, Class<T> responseType) {
        String fullUrl = resolveUrl(url);
        return execute(() -> {
            log.info("[GET][{}] request: {}", clientName, fullUrl);
            
            try {
                return restClient.get()
                        .uri(fullUrl)
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, response) -> {
                                log.error("[GET][{}] {} failed with status: {}", 
                                    clientName, request.getURI(), response.getStatusCode());
                                if (response.getStatusCode().is5xxServerError()) {
                                    throw new HttpServerErrorException(response.getStatusCode());
                                }
                                throw new HttpClientErrorException(response.getStatusCode());
                            }
                        )
                        .body(responseType);
            } catch (ResourceAccessException e) {
                log.error("[GET][{}] Timeout/connection error for {}: {}", clientName, fullUrl, e.getMessage());
                throw e;
            }
        });
    }
    
    public <T, R> T post(String url, R requestBody, Class<T> responseType) {
        String fullUrl = resolveUrl(url);
        return execute(() -> {
            log.info("[POST][{}] request: {} payload: {}", clientName, fullUrl, requestBody);
            
            try {
                var request = restClient.post().uri(fullUrl);
                if (requestBody != null) {
                    request = request.body(requestBody);
                }
                
                return request
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (requestHttp, responseHttp) -> {
                                log.error("[POST][{}] {} response status: {}", clientName, requestHttp.getURI(), responseHttp.getStatusCode());
                                if (responseHttp.getStatusCode().is5xxServerError()) {
                                    throw new HttpServerErrorException(responseHttp.getStatusCode());
                                }
                                throw new HttpClientErrorException(responseHttp.getStatusCode());
                            }
                        )
                        .body(responseType);
            } catch (ResourceAccessException e) {
                log.error("[POST][{}] Timeout/connection error for {}: {}", clientName, fullUrl, e.getMessage());
                throw e;
            }
        });
    }
    
    public <T, R> T post(String url, R requestBody, HttpHeaders headers, Class<T> responseType) {
        String fullUrl = resolveUrl(url);
        return execute(() -> {
            log.info("[POST][{}] request with headers to: {} payload: {}", clientName, fullUrl, requestBody);
            
            try {
                var requestBuilder = restClient.post()
                        .uri(fullUrl)
                        .headers(h -> h.addAll(headers));
                
                if (requestBody != null) {
                    requestBuilder = requestBuilder.body(requestBody);
                }
                
                return requestBuilder
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, response) -> {
                                log.error("[POST][{}] {} response status: {}", clientName, request.getURI(), response.getStatusCode());
                                throw new RestClientException(
                                    String.format("Request failed with status: %s", response.getStatusCode())
                                );
                            }
                        )
                        .body(responseType);
            } catch (ResourceAccessException e) {
                log.error("[POST][{}] Timeout/connection error for {}: {}", clientName, fullUrl, e.getMessage());
                throw e;
            }
        });
    }
    
    private <T> T execute(RetryExecutor.RetryableOperation<T> operation) {
        if (retryExecutor == null) {
            return executeDirect(operation);
        }
        
        try {
            return retryExecutor.execute(operation);
        } catch (Exception e) {
            throw new RestClientException("Request failed after retries", e);
        }
    }
    
    private <T> T executeDirect(RetryExecutor.RetryableOperation<T> operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            throw new RestClientException("Request failed", e);
        }
    }
    
    private RestClient buildRestClient(RestClientProperties.ConnectionConfig connectionConfig) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionConfig.getConnectTimeout());
        factory.setReadTimeout(connectionConfig.getReadTimeout());
        
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}