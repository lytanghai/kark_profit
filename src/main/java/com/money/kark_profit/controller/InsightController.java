package com.money.kark_profit.controller;

import com.money.kark_profit.service.provider.NewsAggregatorProvider;
import com.money.kark_profit.service.provider.PublicNewsProvider;
import com.money.kark_profit.transform.request.InsightRequest;
import com.money.kark_profit.transform.response.EventCalendarResponse;
import com.money.kark_profit.transform.response.InsightResponse;
import com.money.kark_profit.transform.response.NewsArticleResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/insight")
@Slf4j
@RequiredArgsConstructor
public class InsightController {

    private final PublicNewsProvider provider;
    private final NewsAggregatorProvider newsAggregatorProvider;

    @GetMapping
    public ResponseEntity<ResponseBuilderUtils<List<NewsArticleResponse>>> getNews(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String impact,
            @RequestParam(required = false) String keyword
    ) {
        log.info("incoming request to check news globally");
        return new ResponseEntity<>(
                newsAggregatorProvider.getFilteredNews(category, market, impact, keyword),
                HttpStatus.OK);
    }

    @PostMapping("/news")
    public ResponseEntity<ResponseBuilderUtils<InsightResponse>> googleNews(@RequestBody InsightRequest insightRequest) {
        log.info("incoming request to check news");
        return new ResponseEntity<>(provider.fetchGoogleNews(insightRequest), HttpStatus.OK);
    }

    @GetMapping("/events")
    public ResponseEntity<ResponseBuilderUtils<List<EventCalendarResponse>>> forexFactory() {
        log.info("incoming request to check weekly event");
        return new ResponseEntity<>(provider.fetchForexFactory(), HttpStatus.OK);
    }
}
