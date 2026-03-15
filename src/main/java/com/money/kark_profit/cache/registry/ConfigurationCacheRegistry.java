package com.money.kark_profit.cache.registry;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.repository.ConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigurationCacheRegistry {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationCacheRegistry(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public void loadingComponent() {
        log.info("loading service transaction mapping component to cache ...! fetching from db");

        ConfigurationCache.initCache(configurationRepository.findByName(ApplicationCache.TELEGRAM_CHAT_ID).get());
        ConfigurationCache.initCache(configurationRepository.findByName(ApplicationCache.MASTER_KEY).get());
        ConfigurationCache.initCache(configurationRepository.findByName(ApplicationCache.MASTER_ADMIN_USERNAME).get());
        ConfigurationCache.initCache(configurationRepository.findByName(ApplicationCache.TOKEN_LIFE_SPAN).get());
    }
}