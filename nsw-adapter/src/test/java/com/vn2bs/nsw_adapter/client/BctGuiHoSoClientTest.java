package com.vn2bs.nsw_adapter.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.client.core.WebServiceTemplate;

@ExtendWith(MockitoExtension.class)
class BctGuiHoSoClientTest {

    @Mock
    private WebServiceTemplate webServiceTemplate;

    private BctGuiHoSoClient client;

    @BeforeEach
    void setUp() {
        client = new BctGuiHoSoClient(webServiceTemplate);
        ReflectionTestUtils.setField(client, "bctSoapUrl",
                "http://localhost:8082/web-services/bct-thu-tuc-1-gui-ho-so");
    }

    @Test
    void sendGuiHoSo_stubReturnsSuccess() {
        String result = client.sendGuiHoSo("NSW-2026-0001", "Cong ty ABC");

        assertEquals("success", result);
    }
}
