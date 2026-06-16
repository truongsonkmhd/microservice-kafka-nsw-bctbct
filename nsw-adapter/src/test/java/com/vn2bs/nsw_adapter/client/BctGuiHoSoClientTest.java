package com.vn2bs.nsw_adapter.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoResponse;

@ExtendWith(MockitoExtension.class)
class BctGuiHoSoClientTest {

    @Mock
    private WebServiceTemplate webServiceTemplate;

    private BctGuiHoSoClient client;

    @BeforeEach
    void setUp() {
        client = new BctGuiHoSoClient(webServiceTemplate);
        ReflectionTestUtils.setField(client, "bctSoapUrl", "http://localhost:8082/web-services");
    }

    @Test
    void sendGuiHoSo_returnsSuccessWhenBctAcks() {
        GuiHoSoResponse response = new GuiHoSoResponse();
        response.setMaSoHoSo("NSW-2026-0001");
        response.setKetQua("success");

        when(webServiceTemplate.marshalSendAndReceive(anyString(), any(GuiHoSoRequest.class)))
                .thenReturn(response);

        String result = client.sendGuiHoSo("NSW-2026-0001", "Cong ty ABC");

        assertEquals("success", result);
    }

    @Test
    void sendGuiHoSo_throwsWhenBctRejects() {
        GuiHoSoResponse response = new GuiHoSoResponse();
        response.setMaSoHoSo("NSW-2026-0001");
        response.setKetQua("error");

        when(webServiceTemplate.marshalSendAndReceive(anyString(), any(GuiHoSoRequest.class)))
                .thenReturn(response);

        assertThrows(IllegalStateException.class,
                () -> client.sendGuiHoSo("NSW-2026-0001", "Cong ty ABC"));
    }
}
