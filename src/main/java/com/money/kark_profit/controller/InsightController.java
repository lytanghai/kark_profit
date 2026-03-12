package com.money.kark_profit.controller;

import com.money.kark_profit.service.provider.NewsAggregatorProvider;
import com.money.kark_profit.service.provider.PublicNewsProvider;
import com.money.kark_profit.transform.request.InsightRequest;
import com.money.kark_profit.transform.response.EventCalendarResponse;
import com.money.kark_profit.transform.response.InsightResponse;
import com.money.kark_profit.transform.response.NewsArticleResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/insight")
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
        return new ResponseEntity<>(
                newsAggregatorProvider.getFilteredNews(category, market, impact, keyword),
                HttpStatus.OK);
    }

    @PostMapping("/news")
    public ResponseEntity<ResponseBuilderUtils<InsightResponse>> googleNews(@RequestBody InsightRequest insightRequest) {
        return new ResponseEntity<>(provider.fetchGoogleNews(insightRequest), HttpStatus.OK);
    }

    @GetMapping("/event")
    public ResponseEntity<ResponseBuilderUtils<List<EventCalendarResponse>>> forexFactory() {
        return new ResponseEntity<>(provider.fetchForexFactory(), HttpStatus.OK);
    }
}
