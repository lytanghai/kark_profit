package com.money.kark_profit.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class StringCache {

    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    private static volatile long lastUpdatedTime = 0L;

    private StringCache() {}

    public static void put(String key, String value) {
        log.info("Saving cache key {}", key);
        cache.put(key, value);
        lastUpdatedTime = System.currentTimeMillis();
    }

    public static String get(String key) {
        log.info("Getting cache key {}", key);
        return cache.get(key);
    }

    public static int size() {
        return cache.size();
    }

    public static boolean isExpired(long duration) {
        return (System.currentTimeMillis() - lastUpdatedTime) > duration;
    }

    public static void clear() {
        cache.clear();
    }
}