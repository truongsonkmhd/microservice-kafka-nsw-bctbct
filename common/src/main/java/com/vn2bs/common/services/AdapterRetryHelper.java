package com.vn2bs.common.services;

import java.util.function.Supplier;

import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AdapterRetryHelper {

    private static final int MAX_ATTEMPTS = 3;

    private AdapterRetryHelper() {
    }

    public static <T> void runWithRetry(Supplier<T> action, Runnable onSuccess, Runnable onFinalFailure) {
        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                action.get();
                onSuccess.run();
                return;
            } catch (Exception ex) {
                lastError = ex;
                log.warn("Adapter retry attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, ex.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    sleepBackoff(attempt);
                }
            }
        }
        log.error("Adapter processing failed after {} attempts: {}", MAX_ATTEMPTS,
                lastError != null ? lastError.getMessage() : "unknown");
        onFinalFailure.run();
    }

    public static <T> void publishToDlq(KafkaTemplate<String, T> kafkaTemplate, String dlqTopic, T payload) {
        try {
            kafkaTemplate.send(dlqTopic, payload);
            log.warn("Published failed message to adapter DLQ topic={}", dlqTopic);
        } catch (Exception ex) {
            log.error("Adapter DLQ publish failed topic={} error={}", dlqTopic, ex.getMessage());
        }
    }

    private static void sleepBackoff(int attempt) {
        try {
            Thread.sleep((long) Math.pow(2, attempt) * 500L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
