package com.vn2bs.nsw_gateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.dto.ThuTuc1.HoSoTraCuuResponse;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;
import com.vn2bs.nsw_gateway.exception.BusinessException;

@Service
public class HoSoTraCuuService {

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private ThuTuc1_TraLoiRepository traLoiRepository;

    public HoSoTraCuuResponse traCuu(String maSoHoSo) {
        ThuTuc1_GuiHoSo hoSo = guiHoSoRepository.findByMaSoHoSo(maSoHoSo)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "HO_SO_NOT_FOUND",
                        "Ho so not found: " + maSoHoSo));

        HoSoTraCuuResponse response = new HoSoTraCuuResponse();
        response.setMaSoHoSo(hoSo.getMaSoHoSo());
        response.setBusinessStatus(hoSo.getBusinessStatus());
        response.setCreatedDate(hoSo.getCreatedDate());
        response.setLastModifiedDate(hoSo.getLastModifiedDate());

        traLoiRepository.findByMaSoHoSo(maSoHoSo)
                .map(ThuTuc1_TraLoi::getKetQua)
                .ifPresent(response::setKetQua);

        return response;
    }
}
