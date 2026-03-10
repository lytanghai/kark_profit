package com.money.kark_profit.cache.registry;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.repository.ConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class ConfigurationCacheRegistry {

    private final ConfigRepository configRepository;

    public ConfigurationCacheRegistry(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public void loadingComponent() {
        log.info("loading service transaction mapping component to cache ...");

        ConfigurationModel telegramConfig = configRepository.findByName(ApplicationCache.TELEGRAM_CHAT_ID).get();

        if(ObjectUtils.isEmpty(telegramConfig)) {
            log.info("failed to initialize sys application feature cache due {}", telegramConfig);
            return;
        }

        log.info("result: {}", telegramConfig);
        ConfigurationCache.initCache(telegramConfig);
    }
}