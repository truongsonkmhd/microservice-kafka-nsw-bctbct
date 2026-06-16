package com.vn2bs.nsw_adapter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.nsw_adapter.client.BctGuiHoSoClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GuiHoSoService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private BctGuiHoSoClient bctGuiHoSoClient;

    public void process(ThuTuc1_GuiHoSo entity) {
        ThuTuc1_GuiHoSo record = guiHoSoRepository.findById(entity.getId()).orElse(entity);

        record.setStatus(Status.PROCESSING);
        guiHoSoRepository.save(record);
        log.info("Processing GuiHoSo maSoHoSo={}", record.getMaSoHoSo());

        try {
            String ketQua = bctGuiHoSoClient.sendGuiHoSo(record.getMaSoHoSo(), record.getTenNguoiGui());
            log.info("BCT GuiHoSo response maSoHoSo={} ketQua={}", record.getMaSoHoSo(), ketQua);

            record.setBusinessStatus(BusinessStatus.CHO_PHE_DUYET);
            record.setStatus(Status.COMPLETED);
        } catch (Exception ex) {
            log.error("Failed to send GuiHoSo to BCT maSoHoSo={} error={}",
                    record.getMaSoHoSo(), ex.getMessage());
            record.setStatus(Status.FAILED);
        }

        guiHoSoRepository.save(record);
        log.info("Finished GuiHoSo maSoHoSo={} status={} businessStatus={}",
                record.getMaSoHoSo(), record.getStatus(), record.getBusinessStatus());
    }
}
