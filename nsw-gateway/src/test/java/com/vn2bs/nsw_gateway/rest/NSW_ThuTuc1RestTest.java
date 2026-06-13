package com.vn2bs.nsw_gateway.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoSubmitResponse;
import com.vn2bs.nsw_gateway.services.NSWMessageHandler;

@ExtendWith(MockitoExtension.class)
class NSW_ThuTuc1RestTest {

    @Mock
    private NSWMessageHandler nswMessageHandler;

    @InjectMocks
    private NSW_ThuTuc1Rest nswThuTuc1Rest;

    @Test
    void guiHoSo_returnsMaSoHoSoAndCreatedStatus() throws Exception {
        GuiHoSoDto thongTin = new GuiHoSoDto();
        thongTin.setTenNguoiGui("Cong ty ABC");

        GuiHoSoSubmitResponse handlerResult = new GuiHoSoSubmitResponse();
        handlerResult.setMaSoHoSo("NSW-2026-0001");
        handlerResult.setStatus("CREATED");

        when(nswMessageHandler.ThuTuc1_GuiHoSo(any(), isNull())).thenReturn(handlerResult);

        ResponseEntity<IResponse<GuiHoSoSubmitResponse>> response =
                nswThuTuc1Rest.guiHoSo(thongTin, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ok", response.getBody().getMessage());
        assertEquals("NSW-2026-0001", response.getBody().getData().getMaSoHoSo());
        assertEquals("CREATED", response.getBody().getData().getStatus());
    }

    @Test
    void guiHoSo_withAttachments_delegatesToHandler() throws Exception {
        GuiHoSoDto thongTin = new GuiHoSoDto();
        GuiHoSoSubmitResponse handlerResult = new GuiHoSoSubmitResponse();
        handlerResult.setMaSoHoSo("NSW-2026-0002");
        handlerResult.setStatus("CREATED");

        when(nswMessageHandler.ThuTuc1_GuiHoSo(any(), any())).thenReturn(handlerResult);

        ResponseEntity<IResponse<GuiHoSoSubmitResponse>> response =
                nswThuTuc1Rest.guiHoSo(thongTin, List.of());

        assertEquals("NSW-2026-0002", response.getBody().getData().getMaSoHoSo());
    }
}
