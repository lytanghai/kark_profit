package com.money.kark_profit.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class RestTemplateHttpClient {

    private final RestTemplate restTemplate;

    public RestTemplateHttpClient() {
        this.restTemplate = new RestTemplate();
    }

    /* ---------------- GET REQUEST ---------------- */

    public <T> T get(String url, MultiValueMap<String, String> queryParams, Class<T> responseType) {
        try {
            String fullUrl = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParams(queryParams != null ? queryParams : new LinkedMultiValueMap<>())
                    .toUriString();

            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<T> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    responseType
            );

            return response.getBody();

        } catch (RestClientException ex) {
            log.error("GET request failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    /* ---------------- POST REQUEST ---------------- */

    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );

            return response.getBody();

        } catch (RestClientException ex) {
            log.error("POST request failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}