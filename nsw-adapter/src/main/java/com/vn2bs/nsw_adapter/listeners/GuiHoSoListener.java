package com.vn2bs.nsw_adapter.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.nsw_adapter.services.GuiHoSoService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GuiHoSoListener {

    @Autowired
    private GuiHoSoService guiHoSoService;

    @KafkaListener(
            topics = GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO,
            groupId = "nsw-guihoso",
            containerFactory = "ThuTuc1_GuiHoSo_kafkaListenerContainerFactory")
    public void onGuiHoSo(ThuTuc1_GuiHoSo message) {
        log.info("Received GuiHoSo Kafka message maSoHoSo={}", message.getMaSoHoSo());
        guiHoSoService.process(message);
    }
}
