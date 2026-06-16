package com.vn2bs.nsw_adapter.listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DlqAlertListener {

    @KafkaListener(
            topics = GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO_DLQ,
            groupId = "nsw-dlq-alert",
            containerFactory = "ThuTuc1_GuiHoSo_kafkaListenerContainerFactory")
    public void onGuiHoSoDlq(ThuTuc1_GuiHoSo message) {
        log.error("[DLQ ALERT] NSW GuiHoSo failed maSoHoSo={} correlationId={} — manual replay required",
                message.getMaSoHoSo(), message.getCorrelationId());
    }

    @KafkaListener(
            topics = GlobalConfig.Kafka.Topic.BCT.ThuTuc1.TRA_LOI_WS_DLQ,
            groupId = "nsw-traloi-dlq-alert",
            containerFactory = "kafkaListenerContainerFactory")
    public void onTraLoiDlq(String message) {
        log.error("[DLQ ALERT] TraLoi WS failed payload={} — manual replay required", message);
    }
}
