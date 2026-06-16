package com.vn2bs.nsw_adapter.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BctGuiHoSoClient {

    private static final String ACK_SUCCESS = "success";

    private final WebServiceTemplate webServiceTemplate;

    @Value("${bct.soap.url}")
    private String bctSoapUrl;

    public BctGuiHoSoClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String sendGuiHoSo(String maSoHoSo, String tenNguoiGui) {
        GuiHoSoRequest request = new GuiHoSoRequest();
        request.setMaSoHoSo(maSoHoSo);
        request.setTenNguoiGui(tenNguoiGui);

        log.info("Sending GuiHoSo SOAP to BCT url={} maSoHoSo={}", bctSoapUrl, maSoHoSo);

        Object response = webServiceTemplate.marshalSendAndReceive(bctSoapUrl, request);
        if (!(response instanceof GuiHoSoResponse soapResponse)) {
            throw new IllegalStateException("Unexpected SOAP response type: " + response);
        }

        log.info("BCT GuiHoSo SOAP response maSoHoSo={} ketQua={}",
                soapResponse.getMaSoHoSo(), soapResponse.getKetQua());

        if (!ACK_SUCCESS.equalsIgnoreCase(soapResponse.getKetQua())) {
            throw new IllegalStateException("BCT rejected GuiHoSo: " + soapResponse.getKetQua());
        }
        return ACK_SUCCESS;
    }
}
