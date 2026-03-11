package com.money.kark_profit.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class StringCache {

    private static final class CacheEntry {
        private final String value;
        private final long timestamp;

        CacheEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes

    private StringCache() {}

    public static void put(String key, String value) {
        log.info("Saving cache key {}", key);
        cache.put(key, new CacheEntry(value));
    }

    public static String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;

        // Check if expired
        if (System.currentTimeMillis() - entry.timestamp > EXPIRATION_MS) {
            log.info("Cache key {} expired, removing", key);
            cache.remove(key);
            return null;
        }

        log.info("Returning cached key {}", key);
        return entry.value;
    }

    public static void clear() {
        cache.clear();
    }

    public static int size() {
        return cache.size();
    }
}