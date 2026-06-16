package com.vn2bs.nsw_gateway.services.bct;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.dto.ThuTuc1.TraLoiDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;
import com.vn2bs.common.services.MessageLogService;
import com.vn2bs.common.services.OutboxService;
import com.vn2bs.common.ws.MessageContextSupport;
import com.vn2bs.nsw_gateway.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.nsw_gateway.xsd.bct.thutuc1.TraLoiResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ThuTuc1Service {

    private final Gson gson = new Gson();

    @Autowired
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private OutboxService outboxService;

    @Transactional
    public TraLoiResponse traLoi(TraLoiRequest request) {
        String correlationId = MessageContextSupport.correlationId();
        String payloadXml = MessageContextSupport.payloadXml();
        log.info("Nhận TraLoi thủ tục 1: maSoHoSo={} ketQua={} correlationId={}",
                request.getMaSoHoSo(), request.getKetQua(), correlationId);

        messageLogService.logReceived(
                correlationId,
                MessageParty.BCT,
                MessageParty.NSW,
                MessageType.TRA_LOI,
                payloadXml,
                request.getMaSoHoSo());

        Optional<ThuTuc1_GuiHoSo> hoSo = guiHoSoRepository.findByMaSoHoSo(request.getMaSoHoSo());
        if (hoSo.isPresent() && hoSo.get().getBusinessStatus() == BusinessStatus.DA_HUY) {
            log.warn("Reject TraLoi for cancelled ho so maSoHoSo={}", request.getMaSoHoSo());
            messageLogService.logProcessedFailed(
                    correlationId,
                    MessageParty.BCT,
                    MessageParty.NSW,
                    MessageType.TRA_LOI,
                    request.getMaSoHoSo(),
                    "Ho so DA_HUY");
            TraLoiResponse rejected = new TraLoiResponse();
            rejected.setMaSoHoSo(request.getMaSoHoSo());
            rejected.setKetQua("rejected");
            return rejected;
        }

        if (messageLogService.isAlreadyProcessed(correlationId)) {
            log.warn("Duplicate TraLoi correlationId={} maSoHoSo={} — idempotent ack", correlationId,
                    request.getMaSoHoSo());
            TraLoiResponse duplicate = new TraLoiResponse();
            duplicate.setMaSoHoSo(request.getMaSoHoSo());
            duplicate.setKetQua("success");
            return duplicate;
        }

        ThuTuc1_TraLoi entity = new ThuTuc1_TraLoi();
        entity.setMaSoHoSo(request.getMaSoHoSo());
        entity.setKetQua(request.getKetQua());
        entity.setCorrelationId(correlationId);
        entity.setStatus(Status.CREATED);
        traLoiRepository.save(entity);
        log.info("Saved TraLoi entity id={} maSoHoSo={}", entity.getId(), entity.getMaSoHoSo());

        TraLoiDto dto = new TraLoiDto();
        dto.setMaSoHoSo(request.getMaSoHoSo());
        dto.setKetQua(request.getKetQua());
        dto.setCorrelationId(correlationId);

        outboxService.enqueue(
                GlobalConfig.Kafka.Topic.BCT.ThuTuc1.TRA_LOI_WS,
                GlobalConfig.Kafka.Topic.BCT.ThuTuc1.TRA_LOI_WS_DLQ,
                gson.toJson(dto),
                "TraLoiDto",
                request.getMaSoHoSo());

        messageLogService.logSent(
                correlationId,
                MessageParty.NSW,
                MessageParty.NSW,
                MessageType.TRA_LOI,
                payloadXml,
                request.getMaSoHoSo());

        messageLogService.logProcessedSuccess(
                correlationId,
                MessageParty.BCT,
                MessageParty.NSW,
                MessageType.TRA_LOI,
                request.getMaSoHoSo());

        TraLoiResponse response = new TraLoiResponse();
        response.setMaSoHoSo(dto.getMaSoHoSo());
        response.setKetQua("success");
        return response;
    }
}
