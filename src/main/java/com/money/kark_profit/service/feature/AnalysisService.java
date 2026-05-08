package com.money.kark_profit.service.feature;

import com.money.kark_profit.cache.StringCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.constants.ApplicationUrl;
import com.money.kark_profit.http.RestTemplateHttpClient;
import com.money.kark_profit.transform.response.GoldResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.NumberUtil;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final RestTemplateHttpClient restTemplate;

    public static String CACHE_GOLD_OPENING_PRICE = "CACHE_GOLD_OPENING_PRICE";

    public void markMarketOpeningHour() {
        StringCache.put(CACHE_GOLD_OPENING_PRICE, restTemplate.get(ApplicationUrl.goldUrl, null, null, GoldResponse.class).getPrice().toString());
    }

    public ResponseBuilderUtils<GoldResponse> dailyAnalysis() {
        GoldResponse response = new GoldResponse();

        String mockOpen = "4750";
        if(mockOpen != null){
            double openingPrice = NumberUtil.round(Double.parseDouble(Objects.requireNonNull(mockOpen)));
            double currentPrice = NumberUtil.round(restTemplate.get(ApplicationUrl.goldUrl,null, null, GoldResponse.class).getPrice());

            double gap;
            String trend;
            if(openingPrice > currentPrice) {
                gap = openingPrice - currentPrice;
                if(gap > 0.1 && gap < 50) {
                    trend = "BEARISH";
                } else {
                    trend = "STRONG BEARISH";
                }
            } else {
                gap = currentPrice - openingPrice ;
                if(gap > 0.1 && gap < 50) {
                    trend = "BULLISH";
                } else {
                    trend = "STRONG BULLISH";
                }
            }

            response.setDate(DateUtils.formatPhnomPenhTime(new Date()));
            response.setOpeningPrice(openingPrice);
            response.setCurrentPrice(currentPrice);
            response.setGap(NumberUtil.round(gap));
            response.setTrend(trend);
            response.setResult("SUCCESS");
        }

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                response
        );
    }

}
