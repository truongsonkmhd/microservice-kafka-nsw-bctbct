package com.vn2bs.nsw_adapter.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BctGuiHoSoClient {

    private final WebServiceTemplate webServiceTemplate;

    @Value("${bct.soap.url}")
    private String bctSoapUrl;

    public BctGuiHoSoClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    /**
     * Stub — Sprint 1: chỉ log. Sprint 3 sẽ marshal {@code GuiHoSoRequest} và gọi BCT gateway.
     */
    public String sendGuiHoSo(String maSoHoSo, String tenNguoiGui) {
        log.info("STUB send GuiHoSo to BCT url={} maSoHoSo={} tenNguoiGui={}",
                bctSoapUrl, maSoHoSo, tenNguoiGui);
        // TODO G3-T01: webServiceTemplate.marshalSendAndReceive(bctSoapUrl, request)
        return "success";
    }
}
