package com.vn2bs.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.OutboxEvent;
import com.vn2bs.common.domains.OutboxStatus;
import com.vn2bs.common.repositories.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void enqueue_persistsPendingEvent() {
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> {
            OutboxEvent event = inv.getArgument(0);
            event.setId(1L);
            return event;
        });

        OutboxEvent saved = outboxService.enqueue(
                "nsw-thutuc1-guihoso",
                "nsw-thutuc1-guihoso.dlq",
                "{\"maSoHoSo\":\"NSW-001\"}",
                "ThuTuc1_GuiHoSo",
                "NSW-001");

        assertNotNull(saved.getId());
        assertEquals(OutboxStatus.PENDING, saved.getStatus());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals("nsw-thutuc1-guihoso", captor.getValue().getTopic());
    }
}
