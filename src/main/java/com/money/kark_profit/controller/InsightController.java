package com.money.kark_profit.controller;

import com.money.kark_profit.service.provider.PublicNewsProvider;
import com.money.kark_profit.transform.request.InsightRequest;
import com.money.kark_profit.transform.response.EventCalendarResponse;
import com.money.kark_profit.transform.response.InsightResponse;
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

    @PostMapping("/news")
    public ResponseEntity<ResponseBuilderUtils<InsightResponse>> googleNews(@RequestBody InsightRequest insightRequest) {
        return new ResponseEntity<>(provider.fetchGoogleNews(insightRequest), HttpStatus.OK);
    }

    @GetMapping("/event")
    public ResponseEntity<ResponseBuilderUtils<List<EventCalendarResponse>>> forexFactory() {
        return new ResponseEntity<>(provider.fetchForexFactory(), HttpStatus.OK);
    }
}
