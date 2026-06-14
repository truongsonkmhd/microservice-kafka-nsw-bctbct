package com.vn2bs.bct_adapter.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

@ExtendWith(MockitoExtension.class)
class HoSoServiceTest {

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private com.vn2bs.bct_adapter.mapper.GuiHoSoMapper guiHoSoMapper;

    @Mock
    private DuyetHoSoService duyetHoSoService;

    @InjectMocks
    private HoSoService hoSoService;

    @Test
    void markChoXuLy_updatesBusinessStatus() {
        ThuTuc1_GuiHoSo message = new ThuTuc1_GuiHoSo();
        message.setMaSoHoSo("NSW-2026-001");

        ThuTuc1_GuiHoSo existing = new ThuTuc1_GuiHoSo();
        existing.setMaSoHoSo("NSW-2026-001");
        existing.setBusinessStatus(BusinessStatus.KHOI_TAO);

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(existing));
        when(guiHoSoRepository.save(existing)).thenReturn(existing);

        hoSoService.markChoXuLy(message);

        assertEquals(BusinessStatus.CHO_XU_LY, existing.getBusinessStatus());
        assertEquals(Status.PROCESSING, existing.getStatus());
        verify(guiHoSoRepository).save(existing);
    }
}
