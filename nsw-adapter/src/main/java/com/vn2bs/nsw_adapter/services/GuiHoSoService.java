package com.vn2bs.nsw_adapter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GuiHoSoService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    public void process(ThuTuc1_GuiHoSo entity) {
        ThuTuc1_GuiHoSo record = guiHoSoRepository.findById(entity.getId()).orElse(entity);

        record.setStatus(Status.PROCESSING);
        guiHoSoRepository.save(record);
        log.info("Processing GuiHoSo maSoHoSo={}", record.getMaSoHoSo());

        // G1: log only — SOAP client to BCT deferred to Sprint 3

        record.setStatus(Status.COMPLETED);
        guiHoSoRepository.save(record);
        log.info("Completed GuiHoSo maSoHoSo={}", record.getMaSoHoSo());
    }
}
