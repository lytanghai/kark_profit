package com.money.kark_profit.controller;

import com.money.kark_profit.http.RestTemplateHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/polymarket")
@RequiredArgsConstructor
public class PolyMarketController {

    private final RestTemplateHttpClient restHttp;

    @PostMapping("/demo")
    public ResponseEntity<?> result(@RequestBody Map<String, Object> request) {

        String method = (String) request.get("method");
        String url = (String) request.get("url");
        Object body = request.get("body");

        // Basic validation
        if (method == null || url == null) {
            return ResponseEntity.badRequest().body("method and url are required");
        }

        try {
            if ("POST".equalsIgnoreCase(method)) {
                String response = restHttp.post(
                        url,
                        body,
                        null,
                        String.class
                );
                return ResponseEntity.ok(response);

            } else if ("GET".equalsIgnoreCase(method)) {
                String response = restHttp.get(
                        url,
                        null,
                        null,
                        String.class
                );
                return ResponseEntity.ok(response);

            } else {
                return ResponseEntity.badRequest().body("Unsupported method: " + method);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling external API: " + e.getMessage());
        }
    }
}
