package com.money.kark_profit.cache;

import com.money.kark_profit.cache.registry.ConfigurationCacheRegistry;
import com.money.kark_profit.cache.registry.StringCacheRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationInit {
    private final ConfigurationCacheRegistry configurationCacheRegistry;
    private final StringCacheRegistry stringCacheRegistry;

    public ApplicationInit(ConfigurationCacheRegistry configurationCacheRegistry,
                           StringCacheRegistry stringCacheRegistry) {
        this.configurationCacheRegistry = configurationCacheRegistry;
        this.stringCacheRegistry = stringCacheRegistry;
    }

    private void cacheInit() {
        configurationCacheRegistry.loadingComponent();
        stringCacheRegistry.loadCache();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void serverCacheInitiation() {
        log.info("Application cache initiating ....");
        cacheInit();
    }
}