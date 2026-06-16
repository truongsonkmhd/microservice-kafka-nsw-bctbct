package com.vn2bs.nsw_gateway.services.bct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.dto.ThuTuc1.TraLoiDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;
import com.vn2bs.nsw_gateway.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.nsw_gateway.xsd.bct.thutuc1.TraLoiResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ThuTuc1Service {

    private final Gson gson = new Gson();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ThuTuc1_TraLoiRepository traLoiRepository;

    public TraLoiResponse traLoi(TraLoiRequest request) {
        log.info("Nhận dữ liệu trả lời thủ tục 1: (mã hồ sơ={}, kết quả={})", request.getMaSoHoSo(),
                request.getKetQua());

        ThuTuc1_TraLoi entity = new ThuTuc1_TraLoi();
        entity.setMaSoHoSo(request.getMaSoHoSo());
        entity.setKetQua(request.getKetQua());
        entity.setStatus(Status.CREATED);
        traLoiRepository.save(entity);
        log.info("Saved TraLoi entity id={} maSoHoSo={}", entity.getId(), entity.getMaSoHoSo());

        TraLoiDto dto = new TraLoiDto();
        dto.setMaSoHoSo(request.getMaSoHoSo());
        dto.setKetQua(request.getKetQua());
        kafkaTemplate.send(GlobalConfig.Kafka.Topic.BCT.ThuTuc1.TRA_LOI_WS, gson.toJson(dto));

        TraLoiResponse response = new TraLoiResponse();
        response.setMaSoHoSo(dto.getMaSoHoSo());
        response.setKetQua("success");
        return response;
    }
}
