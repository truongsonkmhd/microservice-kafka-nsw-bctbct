package com.vn2bs.bct_gateway.endpoints.nsw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.bct_gateway.services.BCTReceiveHandler;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;

@ExtendWith(MockitoExtension.class)
class GuiHoSoEndpointTest {

    @Mock
    private BCTReceiveHandler bctReceiveHandler;

    @InjectMocks
    private GuiHoSoEndpoint guiHoSoEndpoint;

    @Test
    void receiveGuiHoSo_delegatesToHandler() {
        GuiHoSoRequest request = new GuiHoSoRequest();
        request.setMaSoHoSo("NSW-2026-001");
        request.setTenNguoiGui("Cong ty ABC");

        GuiHoSoResponse expected = new GuiHoSoResponse();
        expected.setMaSoHoSo("NSW-2026-001");
        expected.setKetQua("success");

        when(bctReceiveHandler.receiveSoap(request)).thenReturn(expected);

        GuiHoSoResponse response = guiHoSoEndpoint.receiveGuiHoSo(request);

        assertEquals("success", response.getKetQua());
        assertEquals("NSW-2026-001", response.getMaSoHoSo());
    }
}
