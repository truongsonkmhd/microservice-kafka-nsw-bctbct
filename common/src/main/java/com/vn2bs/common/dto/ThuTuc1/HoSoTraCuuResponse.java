package com.vn2bs.common.dto.ThuTuc1;

import java.sql.Timestamp;

import com.vn2bs.common.domains.BusinessStatus;

import lombok.Data;

@Data
public class HoSoTraCuuResponse {

    private String maSoHoSo;
    private BusinessStatus businessStatus;
    private String ketQua;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;
}
