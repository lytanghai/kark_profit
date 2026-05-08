package com.money.kark_profit.service.feature;

import com.money.kark_profit.cache.StringCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.constants.ApplicationUrl;
import com.money.kark_profit.http.RestClientHttpProvider;
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

    private final RestClientHttpProvider  restClientHttpProvider;

    public static String CACHE_GOLD_OPENING_PRICE = "CACHE_GOLD_OPENING_PRICE";

    public void markMarketOpeningHour() {
        StringCache.put(CACHE_GOLD_OPENING_PRICE, restClientHttpProvider.get(ApplicationUrl.goldUrl, GoldResponse.class).getPrice().toString());
    }

    public ResponseBuilderUtils<GoldResponse> dailyAnalysis() {
        GoldResponse response = new GoldResponse();

        String mockOpening = "4750";
        if(mockOpening != null){
            double openingPrice = NumberUtil.round(Double.parseDouble(Objects.requireNonNull(mockOpening)));
            double currentPrice = NumberUtil.round(restClientHttpProvider.get(ApplicationUrl.goldUrl, GoldResponse.class).getPrice());

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
