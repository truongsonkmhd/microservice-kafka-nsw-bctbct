package com.vn2bs.common.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.vn2bs.common.domains.OutboxEvent;
import com.vn2bs.common.domains.OutboxStatus;
import com.vn2bs.common.repositories.OutboxEventRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OutboxService {

    private final Gson gson = new Gson();

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Transactional
    public OutboxEvent enqueue(String topic, String dlqTopic, String payload, String payloadType, String aggregateKey) {
        OutboxEvent event = new OutboxEvent();
        event.setTopic(topic);
        event.setDlqTopic(dlqTopic);
        event.setPayload(payload);
        event.setPayloadType(payloadType);
        event.setAggregateKey(aggregateKey);
        event.setStatus(OutboxStatus.PENDING);
        OutboxEvent saved = outboxEventRepository.save(event);
        log.debug("Outbox enqueued id={} topic={} aggregateKey={}", saved.getId(), topic, aggregateKey);
        return saved;
    }

    @Transactional
    public OutboxEvent enqueueObject(String topic, String dlqTopic, Object payload, String payloadType,
            String aggregateKey) {
        return enqueue(topic, dlqTopic, gson.toJson(payload), payloadType, aggregateKey);
    }
}
