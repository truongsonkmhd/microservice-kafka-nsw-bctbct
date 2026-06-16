package com.vn2bs.common.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class GatewayMetrics {

    private final MeterRegistry registry;

    public GatewayMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordMessage(String module, String messageType, String direction) {
        registry.counter("gateway.messages.total",
                "module", module,
                "type", messageType,
                "direction", direction)
                .increment();
    }

    public void recordError(String module, String errorType) {
        registry.counter("gateway.errors.total",
                "module", module,
                "error", errorType)
                .increment();
    }
}
