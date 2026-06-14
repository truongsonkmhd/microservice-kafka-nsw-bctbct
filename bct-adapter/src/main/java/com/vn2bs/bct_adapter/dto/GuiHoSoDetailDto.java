package com.vn2bs.bct_adapter.dto;

import java.util.List;

import com.vn2bs.common.domains.BusinessStatus;

import lombok.Data;

@Data
public class GuiHoSoDetailDto {

    private String maSoHoSo;
    private String tenNguoiGui;
    private BusinessStatus businessStatus;
    private List<String> taiLieuDinhKem;
    private String bucketName;
}
