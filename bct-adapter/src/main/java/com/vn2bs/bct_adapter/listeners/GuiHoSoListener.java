package com.vn2bs.bct_adapter.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.vn2bs.bct_adapter.services.HoSoService;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GuiHoSoListener {

    @Autowired
    private HoSoService hoSoService;

    @KafkaListener(
            topics = GlobalConfig.Kafka.Topic.BCT.ThuTuc1.GUI_HO_SO,
            groupId = "bct-adapter-guihoso",
            containerFactory = "ThuTuc1_GuiHoSo_kafkaListenerContainerFactory")
    public void onGuiHoSo(ThuTuc1_GuiHoSo message) {
        log.info("Received BCT GuiHoSo Kafka message maSoHoSo={}", message.getMaSoHoSo());
        hoSoService.markChoXuLy(message);
    }
}
