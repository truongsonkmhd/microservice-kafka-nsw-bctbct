package com.vn2bs.nsw_adapter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.services.AdapterRetryHelper;
import com.vn2bs.common.services.BusinessStatusValidator;
import com.vn2bs.common.services.MessageLogService;
import com.vn2bs.nsw_adapter.client.BctGuiHoSoClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GuiHoSoService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private BctGuiHoSoClient bctGuiHoSoClient;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private BusinessStatusValidator businessStatusValidator;

    @Autowired
    private KafkaTemplate<String, ThuTuc1_GuiHoSo> kafkaTemplate;

    public void process(ThuTuc1_GuiHoSo entity) {
        String correlationId = entity.getCorrelationId();
        if (messageLogService.isAlreadyProcessed(correlationId)) {
            log.warn("Duplicate GuiHoSo skipped correlationId={} maSoHoSo={}", correlationId, entity.getMaSoHoSo());
            return;
        }

        ThuTuc1_GuiHoSo record = guiHoSoRepository.findById(entity.getId()).orElse(entity);
        record.setStatus(Status.PROCESSING);
        guiHoSoRepository.save(record);
        log.info("Processing GuiHoSo maSoHoSo={} correlationId={}", record.getMaSoHoSo(), correlationId);

        AdapterRetryHelper.runWithRetry(
                () -> sendToBct(record),
                () -> onSuccess(record, correlationId),
                () -> onFinalFailure(record, correlationId, entity));
    }

    private Void sendToBct(ThuTuc1_GuiHoSo record) {
        String ketQua = bctGuiHoSoClient.sendGuiHoSo(
                record.getMaSoHoSo(),
                record.getTenNguoiGui(),
                record.getCorrelationId());
        log.info("BCT GuiHoSo response maSoHoSo={} ketQua={}", record.getMaSoHoSo(), ketQua);
        return null;
    }

    private void onSuccess(ThuTuc1_GuiHoSo record, String correlationId) {
        businessStatusValidator.validateTransition(record.getBusinessStatus(), BusinessStatus.CHO_PHE_DUYET);
        record.setBusinessStatus(BusinessStatus.CHO_PHE_DUYET);
        record.setStatus(Status.COMPLETED);
        guiHoSoRepository.save(record);
        messageLogService.logProcessedSuccess(
                correlationId,
                MessageParty.NSW,
                MessageParty.BCT,
                MessageType.GUI_HO_SO,
                record.getMaSoHoSo());
        log.info("Finished GuiHoSo maSoHoSo={} status={} businessStatus={}",
                record.getMaSoHoSo(), record.getStatus(), record.getBusinessStatus());
    }

    private void onFinalFailure(ThuTuc1_GuiHoSo record, String correlationId, ThuTuc1_GuiHoSo original) {
        record.setStatus(Status.DEAD_LETTER);
        guiHoSoRepository.save(record);
        messageLogService.logProcessedFailed(
                correlationId,
                MessageParty.NSW,
                MessageParty.BCT,
                MessageType.GUI_HO_SO,
                record.getMaSoHoSo(),
                "Failed after retries");
        AdapterRetryHelper.publishToDlq(
                kafkaTemplate,
                GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO_DLQ,
                original);
    }
}
