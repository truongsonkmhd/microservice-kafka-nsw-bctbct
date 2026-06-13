package com.vn2bs.common.domains.ThuTuc1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.vn2bs.common.domains.BusinessStatus;

class ThuTuc1_GuiHoSoTest {

    @Test
    void newEntity_hasDefaultBusinessStatusKhoiTao() {
        ThuTuc1_GuiHoSo entity = new ThuTuc1_GuiHoSo();

        assertEquals(BusinessStatus.KHOI_TAO, entity.getBusinessStatus());
        assertNull(entity.getBucketName());
        assertNull(entity.getCorrelationId());
    }
}
