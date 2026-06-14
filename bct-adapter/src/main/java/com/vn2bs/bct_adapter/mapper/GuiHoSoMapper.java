package com.vn2bs.bct_adapter.mapper;

import org.springframework.stereotype.Component;

import com.vn2bs.bct_adapter.dto.GuiHoSoDetailDto;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

@Component
public class GuiHoSoMapper {

    public GuiHoSoDto toDto(ThuTuc1_GuiHoSo entity) {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setMaSoHoSo(entity.getMaSoHoSo());
        dto.setTenNguoiGui(entity.getTenNguoiGui());
        return dto;
    }

    public GuiHoSoDetailDto toDetailDto(ThuTuc1_GuiHoSo entity) {
        GuiHoSoDetailDto dto = new GuiHoSoDetailDto();
        dto.setMaSoHoSo(entity.getMaSoHoSo());
        dto.setTenNguoiGui(entity.getTenNguoiGui());
        dto.setBusinessStatus(entity.getBusinessStatus());
        dto.setTaiLieuDinhKem(entity.getTaiLieuDinhKem());
        dto.setBucketName(entity.getBucketName());
        return dto;
    }
}
