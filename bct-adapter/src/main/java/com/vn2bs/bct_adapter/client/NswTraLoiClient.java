package com.vn2bs.bct_adapter.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;

import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiResponse;
import com.vn2bs.common.utils.CorrelationIdSupport;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NswTraLoiClient implements TraLoiSender {

    private static final String ACK_SUCCESS = "success";

    private final WebServiceTemplate webServiceTemplate;

    @Value("${nsw.soap.url}")
    private String nswSoapUrl;

    public NswTraLoiClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    @Override
    public boolean send(String maSoHoSo, String ketQua, String correlationId) {
        TraLoiRequest request = new TraLoiRequest();
        request.setMaSoHoSo(maSoHoSo);
        request.setKetQua(ketQua);

        WebServiceMessageCallback headerCallback = message -> {
            if (message instanceof SoapMessage soapMessage) {
                CorrelationIdSupport.setHeader(soapMessage, correlationId);
            }
        };

        log.info("Sending TraLoi SOAP to NSW url={} maSoHoSo={} correlationId={}",
                nswSoapUrl, maSoHoSo, correlationId);

        Object response = webServiceTemplate.marshalSendAndReceive(nswSoapUrl, request, headerCallback);
        if (!(response instanceof TraLoiResponse soapResponse)) {
            log.error("Unexpected TraLoi SOAP response type: {}", response);
            return false;
        }

        log.info("NSW TraLoi SOAP response maSoHoSo={} ketQua={}",
                soapResponse.getMaSoHoSo(), soapResponse.getKetQua());

        return ACK_SUCCESS.equalsIgnoreCase(soapResponse.getKetQua());
    }
}
