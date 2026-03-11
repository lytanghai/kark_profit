package com.money.kark_profit.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class StringCache {

    private record CacheEntry(String value, long timestamp) {}

    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long DEFAULT_EXPIRATION_MS = 10 * 60 * 1000; // 10 minutes

    private StringCache() {}

    /**
     * Save a value in cache with default expiration.
     */
    public static void put(String key, String value) {
        log.info("Saving cache key '{}'", key);
        cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
    }

    /**
     * Retrieve a value from cache if not expired.
     */
    public static String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            log.debug("Cache miss for key '{}'", key);
            return null;
        }

        if (isExpired(entry)) {
            log.info("Cache key '{}' expired, removing", key);
            cache.remove(key);
            return null;
        }

        log.debug("Cache hit for key '{}'", key);
        return entry.value();
    }

    /**
     * Clear a specific cache key.
     */
    public static void clear(String key) {
        cache.remove(key);
        log.info("Cache key '{}' cleared", key);
    }

    /**
     * Clear all cache entries.
     */
    public static void clearAll() {
        cache.clear();
        log.info("All cache cleared");
    }

    /**
     * Returns number of cached entries.
     */
    public static int size() {
        return cache.size();
    }

    /**
     * Check if a cache entry has expired.
     */
    public static boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.timestamp() > DEFAULT_EXPIRATION_MS;
    }
}