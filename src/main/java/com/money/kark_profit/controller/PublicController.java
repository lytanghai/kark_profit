package com.money.kark_profit.controller;
import com.sun.management.OperatingSystemMXBean;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;

@RestController
@Slf4j
@RequestMapping("/public")
public class PublicController {

    @Data
    @Builder
    public static class SystemHealth {
        private String cpuUsage;
        private String totalMemory;
        private String freeMemory;
        private String usedMemory;
        private String systemLoad;        // Add this field
        private int availableProcessors;   // Add this field
    }

    @GetMapping("/system-health")
    public SystemHealth getSystemHealth() {
        return checkSystemHealth();
    }

    @Scheduled(cron = "*/20 * * * * *")
    public SystemHealth checkSystemHealth() {
        Runtime runtime = Runtime.getRuntime();

        // Force garbage collection for more accurate memory readings
        System.gc();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuUsage = osBean.getSystemCpuLoad() * 100;
        double systemLoadAverage = osBean.getSystemLoadAverage();

        SystemHealth systemHealth = SystemHealth.builder()
                .cpuUsage(String.format("%.2f %%", cpuUsage))
                .totalMemory(formatMemory(totalMemory))
                .usedMemory(formatMemory(usedMemory))
                .freeMemory(formatMemory(freeMemory))
                .systemLoad(String.format("%.2f", systemLoadAverage))
                .availableProcessors(runtime.availableProcessors())
                .build();

        log.info("System Health Check - CPU: {}, Memory Used: {}/{}",
                String.format("%.2f%%", cpuUsage),
                formatMemory(usedMemory),
                formatMemory(totalMemory));

        return systemHealth;
    }

    private String formatMemory(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}