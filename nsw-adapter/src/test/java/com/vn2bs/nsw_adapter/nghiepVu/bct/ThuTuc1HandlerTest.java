package com.vn2bs.nsw_adapter.nghiepVu.bct;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vn2bs.common.domains.BusinessStatus;

class ThuTuc1HandlerTest {

    @Test
    void resolveBusinessStatus_tuChoiKeyword() {
        assertEquals(BusinessStatus.TU_CHOI,
                ThuTuc1Handler.resolveBusinessStatus("Tu choi: Thieu giay to"));
    }

    @Test
    void resolveBusinessStatus_duyet() {
        assertEquals(BusinessStatus.DA_PHE_DUYET,
                ThuTuc1Handler.resolveBusinessStatus("Phe duyet ho so thanh cong"));
    }
}
