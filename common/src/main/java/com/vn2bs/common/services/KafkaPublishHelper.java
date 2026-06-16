package com.vn2bs.common.services;

import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class KafkaPublishHelper {

    private KafkaPublishHelper() {
    }

    public static <T> void sendOrDlq(KafkaTemplate<String, T> kafkaTemplate, String topic, String dlqTopic, T payload) {
        try {
            kafkaTemplate.send(topic, payload);
            log.debug("Kafka publish ok topic={}", topic);
        } catch (Exception ex) {
            log.error("Kafka publish failed topic={} error={}", topic, ex.getMessage());
            try {
                kafkaTemplate.send(dlqTopic, payload);
                log.warn("Published to DLQ topic={}", dlqTopic);
            } catch (Exception dlqEx) {
                log.error("DLQ publish also failed topic={} error={}", dlqTopic, dlqEx.getMessage());
            }
        }
    }

    public static void sendStringOrDlq(KafkaTemplate<String, String> kafkaTemplate, String topic, String dlqTopic,
            String payload) {
        sendOrDlq(kafkaTemplate, topic, dlqTopic, payload);
    }
}
