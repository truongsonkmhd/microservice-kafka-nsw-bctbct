package com.vn2bs.bct_adapter.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiResponse;

@ExtendWith(MockitoExtension.class)
class NswTraLoiClientTest {

    @Mock
    private WebServiceTemplate webServiceTemplate;

    private NswTraLoiClient client;

    @BeforeEach
    void setUp() {
        client = new NswTraLoiClient(webServiceTemplate);
        ReflectionTestUtils.setField(client, "nswSoapUrl", "http://localhost:8084/web-services");
    }

    @Test
    void send_returnsTrueWhenNswAcks() {
        TraLoiResponse response = new TraLoiResponse();
        response.setMaSoHoSo("NSW-2026-001");
        response.setKetQua("success");

        when(webServiceTemplate.marshalSendAndReceive(anyString(), any(TraLoiRequest.class)))
                .thenReturn(response);

        assertTrue(client.send("NSW-2026-001", "Phe duyet"));
    }

    @Test
    void send_returnsFalseWhenUnexpectedResponse() {
        when(webServiceTemplate.marshalSendAndReceive(anyString(), any(TraLoiRequest.class)))
                .thenReturn("unexpected");

        assertFalse(client.send("NSW-2026-001", "Phe duyet"));
    }
}
