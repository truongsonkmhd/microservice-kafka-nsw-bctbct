package com.vn2bs.bct_gateway.mapper.ThuTuc1;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

@Mapper(componentModel = "spring")
public interface GuiHoSoMapper {

    GuiHoSoDto toDto(ThuTuc1_GuiHoSo entity);

    ThuTuc1_GuiHoSo toEntity(GuiHoSoDto dto);

    GuiHoSoDto fromSoapRequest(GuiHoSoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "businessStatus", ignore = true)
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "taiLieuDinhKem", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    ThuTuc1_GuiHoSo fromSoapRequestToEntity(GuiHoSoRequest request);
}
