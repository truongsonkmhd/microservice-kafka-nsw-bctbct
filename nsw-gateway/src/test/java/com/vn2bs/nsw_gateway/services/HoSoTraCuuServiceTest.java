package com.vn2bs.nsw_gateway.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;
import com.vn2bs.nsw_gateway.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class HoSoTraCuuServiceTest {

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @InjectMocks
    private HoSoTraCuuService hoSoTraCuuService;

    @Test
    void traCuu_returnsHoSoWithKetQua() {
        ThuTuc1_GuiHoSo hoSo = new ThuTuc1_GuiHoSo();
        hoSo.setMaSoHoSo("NSW-2026-001");
        hoSo.setBusinessStatus(BusinessStatus.DA_PHE_DUYET);
        hoSo.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        hoSo.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

        ThuTuc1_TraLoi traLoi = new ThuTuc1_TraLoi();
        traLoi.setKetQua("Phe duyet ho so thanh cong");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(hoSo));
        when(traLoiRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(traLoi));

        var result = hoSoTraCuuService.traCuu("NSW-2026-001");

        assertEquals("NSW-2026-001", result.getMaSoHoSo());
        assertEquals(BusinessStatus.DA_PHE_DUYET, result.getBusinessStatus());
        assertEquals("Phe duyet ho so thanh cong", result.getKetQua());
    }

    @Test
    void traCuu_notFound_throwsBusinessException() {
        when(guiHoSoRepository.findByMaSoHoSo("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> hoSoTraCuuService.traCuu("UNKNOWN"));
    }
}
