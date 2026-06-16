package com.vn2bs.common.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn2bs.common.domains.MessageLog;
import com.vn2bs.common.domains.MessageLogStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.repositories.MessageLogRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageLogService {

    @Autowired
    private MessageLogRepository messageLogRepository;

    @Transactional
    public MessageLog logSent(String correlationId, MessageParty sender, MessageParty receiver,
            MessageType messageType, String payloadXml, String maSoHoSo) {
        return save(correlationId, sender, receiver, messageType, payloadXml, maSoHoSo, MessageLogStatus.SENT, null);
    }

    @Transactional
    public MessageLog logReceived(String correlationId, MessageParty sender, MessageParty receiver,
            MessageType messageType, String payloadXml, String maSoHoSo) {
        return save(correlationId, sender, receiver, messageType, payloadXml, maSoHoSo, MessageLogStatus.RECEIVED,
                null);
    }

    @Transactional
    public MessageLog logProcessedSuccess(String correlationId, MessageParty sender, MessageParty receiver,
            MessageType messageType, String maSoHoSo) {
        return save(correlationId, sender, receiver, messageType, null, maSoHoSo, MessageLogStatus.PROCESSED_SUCCESS,
                null);
    }

    @Transactional
    public MessageLog logProcessedFailed(String correlationId, MessageParty sender, MessageParty receiver,
            MessageType messageType, String maSoHoSo, String errorDetail) {
        return save(correlationId, sender, receiver, messageType, null, maSoHoSo, MessageLogStatus.PROCESSED_FAILED,
                errorDetail);
    }

    public boolean isAlreadyProcessed(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return false;
        }
        return messageLogRepository.existsByCorrelationIdAndLogStatus(correlationId, MessageLogStatus.PROCESSED_SUCCESS);
    }

    private MessageLog save(String correlationId, MessageParty sender, MessageParty receiver, MessageType messageType,
            String payloadXml, String maSoHoSo, MessageLogStatus logStatus, String errorDetail) {
        MessageLog entry = new MessageLog();
        entry.setCorrelationId(correlationId);
        entry.setSender(sender);
        entry.setReceiver(receiver);
        entry.setMessageType(messageType);
        entry.setPayloadXml(payloadXml);
        entry.setMaSoHoSo(maSoHoSo);
        entry.setLogStatus(logStatus);
        entry.setErrorDetail(errorDetail);
        MessageLog saved = messageLogRepository.save(entry);
        log.debug("message_log {} correlationId={} type={} maSoHoSo={}", logStatus, correlationId, messageType,
                maSoHoSo);
        return saved;
    }
}
