package com.vn2bs.bct_adapter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vn2bs.bct_adapter.client.TraLoiSender;
import com.vn2bs.bct_adapter.dto.DuyetRequest;
import com.vn2bs.bct_adapter.dto.TuChoiRequest;
import com.vn2bs.bct_adapter.exception.BusinessException;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DuyetHoSoService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @Autowired
    private TraLoiSender traLoiSender;

    public void duyet(String maSoHoSo, DuyetRequest request) {
        validateDuyetRequest(request);
        ThuTuc1_GuiHoSo hoSo = findHoSoOrThrow(maSoHoSo);
        assertChoXuLy(hoSo);

        hoSo.setBusinessStatus(BusinessStatus.DA_XU_LY);
        hoSo.setStatus(Status.COMPLETED);
        guiHoSoRepository.save(hoSo);

        saveTraLoi(maSoHoSo, request.getKetQua(), null, request.getTenNguoiXuLy());
        traLoiSender.send(maSoHoSo, request.getKetQua());

        log.info("Duyet ho so maSoHoSo={} by {}", maSoHoSo, request.getTenNguoiXuLy());
    }

    public void tuChoi(String maSoHoSo, TuChoiRequest request) {
        validateTuChoiRequest(request);
        ThuTuc1_GuiHoSo hoSo = findHoSoOrThrow(maSoHoSo);
        assertChoXuLy(hoSo);

        hoSo.setBusinessStatus(BusinessStatus.DA_XU_LY);
        hoSo.setStatus(Status.COMPLETED);
        guiHoSoRepository.save(hoSo);

        String ketQua = "Tu choi: " + request.getLyDo();
        saveTraLoi(maSoHoSo, ketQua, request.getLyDo(), request.getTenNguoiXuLy());
        traLoiSender.send(maSoHoSo, ketQua);

        log.info("Tu choi ho so maSoHoSo={} by {} lyDo={}", maSoHoSo, request.getTenNguoiXuLy(), request.getLyDo());
    }

    private void saveTraLoi(String maSoHoSo, String ketQua, String lyDo, String tenNguoiXuLy) {
        ThuTuc1_TraLoi traLoi = traLoiRepository.findByMaSoHoSo(maSoHoSo).orElse(new ThuTuc1_TraLoi());
        traLoi.setMaSoHoSo(maSoHoSo);
        traLoi.setKetQua(ketQua);
        traLoi.setLyDo(lyDo);
        traLoi.setTenNguoiXuLy(tenNguoiXuLy);
        traLoi.setStatus(Status.COMPLETED);
        traLoiRepository.save(traLoi);
    }

    private void assertChoXuLy(ThuTuc1_GuiHoSo hoSo) {
        if (hoSo.getBusinessStatus() != BusinessStatus.CHO_XU_LY) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "INVALID_STATUS",
                    "businessStatus must be CHO_XU_LY, current: " + hoSo.getBusinessStatus());
        }
    }

    private ThuTuc1_GuiHoSo findHoSoOrThrow(String maSoHoSo) {
        return guiHoSoRepository.findByMaSoHoSo(maSoHoSo)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "HO_SO_NOT_FOUND",
                        "Ho so not found: " + maSoHoSo));
    }

    private void validateDuyetRequest(DuyetRequest request) {
        if (request == null || isBlank(request.getTenNguoiXuLy()) || isBlank(request.getKetQua())) {
            throw new IllegalArgumentException("tenNguoiXuLy and ketQua are required");
        }
    }

    private void validateTuChoiRequest(TuChoiRequest request) {
        if (request == null || isBlank(request.getTenNguoiXuLy()) || isBlank(request.getLyDo())) {
            throw new IllegalArgumentException("tenNguoiXuLy and lyDo are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
