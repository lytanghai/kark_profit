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
        // Try to get from cache
        String cached = StringCache.get(CACHE_KEY);
        if (cached != null) {
            log.info("Returning economic events from cache");
            return cached;
        }

        // Cache miss: fetch from API
        log.info("Cache miss. Fetching economic events from API: {}", apiUrl);
        String jsonResponse = restHttp.get(apiUrl, null, null, String.class);

        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            // Store in cache
            StringCache.put(CACHE_KEY, jsonResponse);
            log.info("Economic events cached for future requests");
        }

        return jsonResponse;
    }

    /**
     * Clear the cached economic events manually.
     */
    public static void clearCache() {
        StringCache.clear(CACHE_KEY);
        log.info("Economic events cache cleared");
    }
}