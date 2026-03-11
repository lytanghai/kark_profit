package com.money.kark_profit.controller;

import com.money.kark_profit.service.provider.PublicNewsProvider;
import com.money.kark_profit.transform.request.MarketNewsRequest;
import com.money.kark_profit.transform.response.MarketNewsResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final PublicNewsProvider provider;

    @PostMapping("/news")
    public ResponseEntity<ResponseBuilderUtils<MarketNewsResponse>> register(@RequestBody MarketNewsRequest marketNewsRequest) {
        return new ResponseEntity<>(provider.fetchGoogleNews(marketNewsRequest), HttpStatus.OK);
    }
}
