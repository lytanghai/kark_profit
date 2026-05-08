package com.money.kark_profit.service.provider;

import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.service.feature.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Configuration
@RequiredArgsConstructor
@Service
@Slf4j
public class AnalysisProvider implements SchedulingConfigurer {

    private final AnalysisService analysisService;

    private final ConfigurationRepository configurationRepository;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("******* [CRON] Fetch Gold Price");
        taskRegistrar.addTriggerTask(
            () -> analysisService.markMarketOpeningHour(),
            triggerContext -> {
                // second minute hour day month weekday
                // MON-FRI only at hh:00
                String cron = String.format(
                        "0 0 %d * * MON-FRI",
                        Integer.parseInt(
                                configurationRepository.findByName(ApplicationCache.MARKET_OPEN_HOUR).get().getValue())
                );
                CronTrigger cronTrigger = new CronTrigger(cron);

                return cronTrigger.nextExecution(triggerContext);
            }
        );
    }
}
