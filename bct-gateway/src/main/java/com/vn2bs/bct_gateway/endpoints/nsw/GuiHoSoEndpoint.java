package com.vn2bs.bct_gateway.endpoints.nsw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.vn2bs.bct_gateway.services.BCTReceiveHandler;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;

import lombok.extern.slf4j.Slf4j;

@Endpoint
@Slf4j
public class GuiHoSoEndpoint {

    private static final String NAMESPACE = "guihoso.bct.xsd.nsw_gateway.vn2bs.com";

    @Autowired
    private BCTReceiveHandler bctReceiveHandler;

    @PayloadRoot(namespace = NAMESPACE, localPart = "GuiHoSoRequest")
    @ResponsePayload
    public GuiHoSoResponse receiveGuiHoSo(@RequestPayload GuiHoSoRequest request) {
        log.info("GuiHoSoEndpoint receive maSoHoSo={}", request.getMaSoHoSo());
        return bctReceiveHandler.receiveSoap(request);
    }
}
