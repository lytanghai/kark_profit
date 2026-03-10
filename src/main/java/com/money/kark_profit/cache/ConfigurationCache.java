package com.money.kark_profit.cache;

import com.money.kark_profit.model.ConfigurationModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigurationCache {

    private static final Map<String, ConfigurationModel> mappingCache = new ConcurrentHashMap<>();
    private static volatile long lastUpdatedTime = 0L;

    private ConfigurationCache() { }

    public static ConfigurationModel getByKeyName(String keyName) {
        return mappingCache.get(keyName);
    }

    public static void initCache(ConfigurationModel configurationModel) {
        if(configurationModel.getName() != null) {
            mappingCache.put(configurationModel.getName(), configurationModel);
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