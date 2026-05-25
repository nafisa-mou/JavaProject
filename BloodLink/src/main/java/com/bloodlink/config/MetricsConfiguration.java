package com.bloodlink.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * MetricsConfiguration - Configure application metrics and monitoring
 * 
 * Features:
 * - Request/response metrics
 * - Business logic metrics (@Timed)
 * - Custom application metrics
 * - Micrometer integration with Actuator
 * 
 * Exposed endpoints:
 * - GET /actuator/metrics - List available metrics
 * - GET /actuator/health - Application health
 * - GET /actuator/prometheus - Prometheus-compatible metrics
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfiguration {

    /**
     * Customize meter registry with application tags
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "bloodlink",
            "environment", "development",
            "service", "blood-donor-management"
        );
    }

    /**
     * Enable @Timed annotation on methods
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
