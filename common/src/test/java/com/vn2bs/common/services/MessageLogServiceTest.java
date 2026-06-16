package com.vn2bs.common.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.MessageLogStatus;
import com.vn2bs.common.repositories.MessageLogRepository;

@ExtendWith(MockitoExtension.class)
class MessageLogServiceTest {

    @Mock
    private MessageLogRepository messageLogRepository;

    @InjectMocks
    private MessageLogService messageLogService;

    @Test
    void isAlreadyProcessed_returnsFalseWhenCorrelationIdNull() {
        assertFalse(messageLogService.isAlreadyProcessed(null));
    }

    @Test
    void isAlreadyProcessed_returnsTrueWhenProcessedSuccessExists() {
        when(messageLogRepository.existsByCorrelationIdAndLogStatus("cid-1", MessageLogStatus.PROCESSED_SUCCESS))
                .thenReturn(true);
        assertTrue(messageLogService.isAlreadyProcessed("cid-1"));
    }
}
