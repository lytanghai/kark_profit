package com.money.kark_profit.controller;

import com.sun.management.OperatingSystemMXBean;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Data
    @Builder
    public static class SystemHealth{
        private String cpuUsage;
        private String totalMemory;
        private String freeMemory;
        private String usedMemory;
    }

    @GetMapping("/system-health")
    public SystemHealth checkSystemHealth() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuUsage = osBean.getSystemCpuLoad() * 100;

        return SystemHealth.builder()
                .cpuUsage(String.format("%.2f %%", cpuUsage))
                .totalMemory((totalMemory / 1024 / 1024) + " MB")
                .usedMemory((usedMemory / 1024 / 1024) + " MB")
                .freeMemory((freeMemory / 1024 / 1024) + " MB")
                .build();
    }
}
