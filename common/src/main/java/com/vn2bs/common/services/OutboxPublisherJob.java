package com.vn2bs.common.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vn2bs.common.domains.OutboxEvent;
import com.vn2bs.common.domains.OutboxStatus;
import com.vn2bs.common.repositories.OutboxEventRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(name = "outbox.publisher.enabled", havingValue = "true")
@Slf4j
public class OutboxPublisherJob {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${outbox.publish.batch-size:50}")
    private int batchSize;

    @Value("${outbox.publish.max-retries:3}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${outbox.publish.interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, batchSize));

        for (OutboxEvent event : pending) {
            publishOne(event);
        }
    }

    private void publishOne(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(new Timestamp(System.currentTimeMillis()));
            event.setErrorDetail(null);
            log.info("Outbox published id={} topic={} aggregateKey={}", event.getId(), event.getTopic(),
                    event.getAggregateKey());
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorDetail(ex.getMessage());
            log.warn("Outbox publish failed id={} attempt={}/{} error={}",
                    event.getId(), event.getRetryCount(), maxRetries, ex.getMessage());

            if (event.getRetryCount() >= maxRetries) {
                moveToDlq(event);
            }
        }
        outboxEventRepository.save(event);
    }

    private void moveToDlq(OutboxEvent event) {
        try {
            kafkaTemplate.send(event.getDlqTopic(), event.getPayload()).get();
            event.setStatus(OutboxStatus.FAILED);
            log.error("Outbox moved to DLQ id={} dlqTopic={} aggregateKey={}",
                    event.getId(), event.getDlqTopic(), event.getAggregateKey());
        } catch (Exception dlqEx) {
            event.setStatus(OutboxStatus.FAILED);
            event.setErrorDetail("DLQ failed: " + dlqEx.getMessage());
            log.error("Outbox DLQ publish failed id={} error={}", event.getId(), dlqEx.getMessage());
        }
    }
}
