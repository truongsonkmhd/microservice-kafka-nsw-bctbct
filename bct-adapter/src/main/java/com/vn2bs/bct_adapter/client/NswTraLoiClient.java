package com.vn2bs.bct_adapter.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiResponse;

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
    public boolean send(String maSoHoSo, String ketQua) {
        TraLoiRequest request = new TraLoiRequest();
        request.setMaSoHoSo(maSoHoSo);
        request.setKetQua(ketQua);

        log.info("Sending TraLoi SOAP to NSW url={} maSoHoSo={}", nswSoapUrl, maSoHoSo);

        Object response = webServiceTemplate.marshalSendAndReceive(nswSoapUrl, request);
        if (!(response instanceof TraLoiResponse soapResponse)) {
            log.error("Unexpected TraLoi SOAP response type: {}", response);
            return false;
        }

        log.info("NSW TraLoi SOAP response maSoHoSo={} ketQua={}",
                soapResponse.getMaSoHoSo(), soapResponse.getKetQua());

        return ACK_SUCCESS.equalsIgnoreCase(soapResponse.getKetQua());
    }
}
