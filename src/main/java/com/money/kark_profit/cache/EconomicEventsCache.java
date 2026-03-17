package com.money.kark_profit.cache;

import com.money.kark_profit.http.RestTemplateHttpClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EconomicEventsCache {

    private static final String CACHE_KEY = "ECONOMIC_EVENTS";

    private EconomicEventsCache() {}

    /**
     * Fetch economic events from API with caching.
     * Returns cached data if not expired; otherwise fetches from API and caches it.
     *
     * @param restHttp RestTemplateHttpClient instance
     * @param apiUrl   API URL to fetch economic events
     * @return JSON string of economic events
     */
    public static String getEconomicEvents(RestTemplateHttpClient restHttp, String apiUrl) {
        return restHttp.get(apiUrl, null, null, String.class);
    }

    /**
     * Clear the cached economic events manually.
     */
    public static void clearCache() {
        StringCache.clear(CACHE_KEY);
        log.info("Economic events cache cleared");
    }
}