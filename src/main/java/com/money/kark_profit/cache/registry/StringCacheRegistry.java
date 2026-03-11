package com.money.kark_profit.cache.registry;

import com.money.kark_profit.cache.StringCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringCacheRegistry {

    public void loadCache() {

        log.info("Loading string cache...");

        String telegramResponse = "some telegram json response";
        String exchangeRateResponse = "some exchange rate json";

        StringCache.put("TELEGRAM_RESPONSE", telegramResponse);
        StringCache.put("EXCHANGE_RATE_RESPONSE", exchangeRateResponse);

        log.info("String cache loaded: {}", StringCache.size());
    }
}