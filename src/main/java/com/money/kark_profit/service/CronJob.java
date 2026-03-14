package com.money.kark_profit.service;

import com.money.kark_profit.http.RestTemplateHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CronJob {

    private final RestTemplateHttpClient restHttp;

    private final String url = "https://alert-engine-h1mv.onrender.com/health";

//    @Scheduled(fixedRate = 100000)
    public void sendMessage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0");

        // Fetch from API
        String xml = restHttp.get(url, null, headers, String.class);
    }
}
