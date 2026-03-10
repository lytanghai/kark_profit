package com.money.kark_profit.cache;

import com.money.kark_profit.model.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigurationCache {

    private static final Map<String, Configuration> mappingCache = new ConcurrentHashMap<>();
    private static volatile long lastUpdatedTime = 0L;

    private ConfigurationCache() { }

    public static Configuration getByKeyName(String keyName) {
        return mappingCache.get(keyName);
    }

    public static void initCache(Configuration configuration) {
        if(configuration.getName() != null) {
            mappingCache.put(configuration.getName(), configuration);
        }
        lastUpdatedTime = System.currentTimeMillis();
    }

    public static int size() {
        return mappingCache.size();
    }

    public static boolean isExpired(long duration) {
        return (System.currentTimeMillis() - lastUpdatedTime) > duration;
    }
}