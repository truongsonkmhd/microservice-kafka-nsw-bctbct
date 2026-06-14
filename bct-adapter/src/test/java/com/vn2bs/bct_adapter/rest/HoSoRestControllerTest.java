package com.vn2bs.bct_adapter.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vn2bs.bct_adapter.dto.DuyetRequest;
import com.vn2bs.bct_adapter.services.HoSoService;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

@ExtendWith(MockitoExtension.class)
class HoSoRestControllerTest {

    @Mock
    private HoSoService hoSoService;

    @Mock
    private ResponseFactory responseFactory;

    @InjectMocks
    private HoSoRestController hoSoRestController;

    @Test
    void list_returnsChoXuLyHoSo() {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setMaSoHoSo("NSW-2026-001");

        when(hoSoService.listByStatus(BusinessStatus.CHO_XU_LY)).thenReturn(List.of(dto));
        when(responseFactory.success(List.of(dto))).thenReturn(ResponseEntity.ok(new IResponse<>()));

        ResponseEntity<IResponse<List<GuiHoSoDto>>> response = hoSoRestController.list(BusinessStatus.CHO_XU_LY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void duyet_delegatesToService() {
        DuyetRequest request = new DuyetRequest();
        request.setTenNguoiXuLy("Can bo A");
        request.setKetQua("Phe duyet");

        when(responseFactory.success("Duyet thanh cong")).thenReturn(ResponseEntity.ok(new IResponse<>()));

        hoSoRestController.duyet("NSW-2026-001", request);

        verify(hoSoService).duyet("NSW-2026-001", request);
    }
}
