package com.money.kark_profit.cache.registry;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.model.ConfigurationModel;
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
        log.info("loading service transaction mapping component to cache ...");

        ConfigurationModel telegramConfig = configurationRepository.findByName(ApplicationCache.TELEGRAM_CHAT_ID).get();
        ConfigurationModel masterKey = configurationRepository.findByName(ApplicationCache.MASTER_KEY).get();

        log.info("result: {}", telegramConfig);
        ConfigurationCache.initCache(telegramConfig);
        ConfigurationCache.initCache(masterKey);
    }
}