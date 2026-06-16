package com.vn2bs.nsw_adapter.nghiepVu.bct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.TraLoiDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ThuTuc1Handler {

    private final Gson gson = new Gson();

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @KafkaListener(topics = GlobalConfig.Kafka.Topic.BCT.ThuTuc1.TRA_LOI_WS, groupId = "bct", containerFactory = "kafkaListenerContainerFactory")
    public void traLoi(String request) {
        TraLoiDto dto = gson.fromJson(request, TraLoiDto.class);
        log.info("Received TraLoi at NSW adapter maSoHoSo={} ketQua={}", dto.getMaSoHoSo(), dto.getKetQua());

        ThuTuc1_GuiHoSo hoSo = guiHoSoRepository.findByMaSoHoSo(dto.getMaSoHoSo()).orElse(null);
        if (hoSo == null) {
            log.warn("GuiHoSo not found for TraLoi maSoHoSo={}", dto.getMaSoHoSo());
            return;
        }

        hoSo.setBusinessStatus(resolveBusinessStatus(dto.getKetQua()));
        hoSo.setStatus(Status.COMPLETED);
        guiHoSoRepository.save(hoSo);

        log.info("Updated GuiHoSo maSoHoSo={} businessStatus={}", hoSo.getMaSoHoSo(), hoSo.getBusinessStatus());
    }

    static BusinessStatus resolveBusinessStatus(String ketQua) {
        if (ketQua != null && ketQua.toLowerCase().contains("tu choi")) {
            return BusinessStatus.TU_CHOI;
        }
        return BusinessStatus.DA_PHE_DUYET;
    }
}
