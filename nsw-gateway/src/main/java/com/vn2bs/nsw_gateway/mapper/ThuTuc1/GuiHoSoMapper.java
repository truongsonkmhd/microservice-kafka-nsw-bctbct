package com.vn2bs.nsw_gateway.mapper.ThuTuc1;

import org.mapstruct.Mapper;

import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

@Mapper(componentModel = "spring")
public interface GuiHoSoMapper {

    GuiHoSoDto toDto(ThuTuc1_GuiHoSo entity);

    ThuTuc1_GuiHoSo toEntity(GuiHoSoDto dto);
}
