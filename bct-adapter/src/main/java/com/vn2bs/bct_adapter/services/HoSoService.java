package com.vn2bs.bct_adapter.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn2bs.bct_adapter.dto.GuiHoSoDetailDto;
import com.vn2bs.bct_adapter.exception.BusinessException;
import com.vn2bs.bct_adapter.mapper.GuiHoSoMapper;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.services.BusinessStatusValidator;
import com.vn2bs.common.services.MessageLogService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HoSoService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private GuiHoSoMapper guiHoSoMapper;

    @Autowired
    private DuyetHoSoService duyetHoSoService;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private BusinessStatusValidator businessStatusValidator;

    public void markChoXuLy(ThuTuc1_GuiHoSo message) {
        if (messageLogService.isAlreadyProcessed(message.getCorrelationId())) {
            log.warn("Duplicate BCT GuiHoSo skipped correlationId={} maSoHoSo={}",
                    message.getCorrelationId(), message.getMaSoHoSo());
            return;
        }

        ThuTuc1_GuiHoSo record = guiHoSoRepository.findByMaSoHoSo(message.getMaSoHoSo())
                .orElse(message);

        businessStatusValidator.validateTransition(record.getBusinessStatus(), BusinessStatus.CHO_XU_LY);
        record.setBusinessStatus(BusinessStatus.CHO_XU_LY);
        record.setStatus(Status.PROCESSING);
        if (record.getCorrelationId() == null) {
            record.setCorrelationId(message.getCorrelationId());
        }
        guiHoSoRepository.save(record);

        messageLogService.logProcessedSuccess(
                record.getCorrelationId(),
                MessageParty.NSW,
                MessageParty.BCT,
                MessageType.GUI_HO_SO,
                record.getMaSoHoSo());

        log.info("GuiHoSo marked CHO_XU_LY maSoHoSo={}", record.getMaSoHoSo());
    }

    public List<GuiHoSoDto> listByStatus(BusinessStatus status) {
        return guiHoSoRepository.findByBusinessStatus(status).stream()
                .map(guiHoSoMapper::toDto)
                .toList();
    }

    public GuiHoSoDetailDto getDetail(String maSoHoSo) {
        ThuTuc1_GuiHoSo entity = findHoSoOrThrow(maSoHoSo);
        return guiHoSoMapper.toDetailDto(entity);
    }

    public void duyet(String maSoHoSo, com.vn2bs.bct_adapter.dto.DuyetRequest request) {
        duyetHoSoService.duyet(maSoHoSo, request);
    }

    public void tuChoi(String maSoHoSo, com.vn2bs.bct_adapter.dto.TuChoiRequest request) {
        duyetHoSoService.tuChoi(maSoHoSo, request);
    }

    ThuTuc1_GuiHoSo findHoSoOrThrow(String maSoHoSo) {
        return guiHoSoRepository.findByMaSoHoSo(maSoHoSo)
                .orElseThrow(() -> new BusinessException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "HO_SO_NOT_FOUND",
                        "Ho so not found: " + maSoHoSo));
    }
}
