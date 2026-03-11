package com.money.kark_profit.controller;

import com.money.kark_profit.service.provider.PublicNewsProvider;
import com.money.kark_profit.transform.request.MarketNewsRequest;
import com.money.kark_profit.transform.response.EventCalendarResponse;
import com.money.kark_profit.transform.response.MarketNewsResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final PublicNewsProvider provider;

    @PostMapping("/news")
    public ResponseEntity<ResponseBuilderUtils<MarketNewsResponse>> googleNews(@RequestBody MarketNewsRequest marketNewsRequest) {
        return new ResponseEntity<>(provider.fetchGoogleNews(marketNewsRequest), HttpStatus.OK);
    }

    @GetMapping("/event")
    public ResponseEntity<ResponseBuilderUtils<List<EventCalendarResponse>>> forexFactory() {
        return new ResponseEntity<>(provider.fetchForexFactory(), HttpStatus.OK);
    }
}
