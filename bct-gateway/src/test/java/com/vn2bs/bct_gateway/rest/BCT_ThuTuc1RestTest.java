package com.vn2bs.bct_gateway.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vn2bs.bct_gateway.services.BCTReceiveHandler;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

@ExtendWith(MockitoExtension.class)
class BCT_ThuTuc1RestTest {

    @Mock
    private BCTReceiveHandler bctReceiveHandler;

    @InjectMocks
    private BCT_ThuTuc1Rest bctThuTuc1Rest;

    @Test
    void guiHoSo_returnsAckResponse() {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setMaSoHoSo("NSW-2026-001");
        dto.setTenNguoiGui("Cong ty ABC");

        GuiHoSoResponse handlerResult = new GuiHoSoResponse();
        handlerResult.setMaSoHoSo("NSW-2026-001");
        handlerResult.setKetQua("success");

        when(bctReceiveHandler.receiveRest(dto)).thenReturn(handlerResult);

        ResponseEntity<IResponse<GuiHoSoResponse>> response = bctThuTuc1Rest.guiHoSo(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getData().getKetQua());
    }
}
