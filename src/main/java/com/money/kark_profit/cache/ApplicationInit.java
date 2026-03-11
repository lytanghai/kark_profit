package com.money.kark_profit.cache;

import com.money.kark_profit.cache.registry.ConfigurationCacheRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationInit {
    private final ConfigurationCacheRegistry configurationCacheRegistry;

    public ApplicationInit(ConfigurationCacheRegistry configurationCacheRegistry) {
        this.configurationCacheRegistry = configurationCacheRegistry;
    }

    private void cacheInit() {
        configurationCacheRegistry.loadingComponent();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void serverCacheInitiation() {
        log.info("Application cache initiating ....");
        cacheInit();
    }
}