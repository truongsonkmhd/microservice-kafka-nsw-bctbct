package com.vn2bs.bct_adapter.listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DlqAlertListener {

    @KafkaListener(
            topics = GlobalConfig.Kafka.Topic.BCT.ThuTuc1.GUI_HO_SO_DLQ,
            groupId = "bct-dlq-alert",
            containerFactory = "ThuTuc1_GuiHoSo_kafkaListenerContainerFactory")
    public void onBctGuiHoSoDlq(ThuTuc1_GuiHoSo message) {
        log.error("[DLQ ALERT] BCT GuiHoSo failed maSoHoSo={} correlationId={} — manual replay required",
                message.getMaSoHoSo(), message.getCorrelationId());
    }
}
