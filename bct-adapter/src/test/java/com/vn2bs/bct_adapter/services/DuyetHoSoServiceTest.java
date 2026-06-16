package com.vn2bs.bct_adapter.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.bct_adapter.client.TraLoiSender;
import com.vn2bs.bct_adapter.dto.DuyetRequest;
import com.vn2bs.bct_adapter.dto.TuChoiRequest;
import com.vn2bs.bct_adapter.exception.BusinessException;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;

@ExtendWith(MockitoExtension.class)
class DuyetHoSoServiceTest {

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @Mock
    private TraLoiSender traLoiSender;

    @InjectMocks
    private DuyetHoSoService duyetHoSoService;

    @Test
    void duyet_updatesStatusAndSavesTraLoi() {
        ThuTuc1_GuiHoSo hoSo = new ThuTuc1_GuiHoSo();
        hoSo.setMaSoHoSo("NSW-2026-001");
        hoSo.setBusinessStatus(BusinessStatus.CHO_XU_LY);

        DuyetRequest request = new DuyetRequest();
        request.setTenNguoiXuLy("Can bo A");
        request.setKetQua("Phe duyet ho so");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(hoSo));
        when(traLoiRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.empty());
        when(traLoiSender.send("NSW-2026-001", "Phe duyet ho so")).thenReturn(true);

        duyetHoSoService.duyet("NSW-2026-001", request);

        assertEquals(BusinessStatus.DA_GUI_KET_QUA, hoSo.getBusinessStatus());
        verify(traLoiRepository).save(any(ThuTuc1_TraLoi.class));
        verify(traLoiSender).send("NSW-2026-001", "Phe duyet ho so");
    }

    @Test
    void duyet_wrongStatus_throwsConflict() {
        ThuTuc1_GuiHoSo hoSo = new ThuTuc1_GuiHoSo();
        hoSo.setMaSoHoSo("NSW-2026-001");
        hoSo.setBusinessStatus(BusinessStatus.KHOI_TAO);

        DuyetRequest request = new DuyetRequest();
        request.setTenNguoiXuLy("Can bo A");
        request.setKetQua("Phe duyet");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(hoSo));

        assertThrows(BusinessException.class, () -> duyetHoSoService.duyet("NSW-2026-001", request));
    }

    @Test
    void tuChoi_missingLyDo_throwsBadRequest() {
        TuChoiRequest request = new TuChoiRequest();
        request.setTenNguoiXuLy("Can bo A");

        assertThrows(IllegalArgumentException.class,
                () -> duyetHoSoService.tuChoi("NSW-2026-001", request));
    }

    @Test
    void tuChoi_savesTraLoiWithLyDo() {
        ThuTuc1_GuiHoSo hoSo = new ThuTuc1_GuiHoSo();
        hoSo.setMaSoHoSo("NSW-2026-001");
        hoSo.setBusinessStatus(BusinessStatus.CHO_XU_LY);

        TuChoiRequest request = new TuChoiRequest();
        request.setTenNguoiXuLy("Can bo A");
        request.setLyDo("Thieu giay to");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.of(hoSo));
        when(traLoiRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.empty());
        when(traLoiSender.send("NSW-2026-001", "Tu choi: Thieu giay to")).thenReturn(true);

        duyetHoSoService.tuChoi("NSW-2026-001", request);

        assertEquals(BusinessStatus.DA_GUI_KET_QUA, hoSo.getBusinessStatus());
        verify(traLoiSender).send("NSW-2026-001", "Tu choi: Thieu giay to");
    }
}
