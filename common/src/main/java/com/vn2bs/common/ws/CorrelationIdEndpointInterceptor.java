package com.vn2bs.common.ws;

import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;

import com.vn2bs.common.utils.CorrelationIdSupport;

@Component
public class CorrelationIdEndpointInterceptor implements EndpointInterceptor {

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage soapMessage) {
            String correlationId = CorrelationIdSupport.readHeader(soapMessage);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = CorrelationIdSupport.generate();
            }
            MessageContextSupport.setCorrelationId(correlationId);
            MessageContextSupport.setPayloadXml(CorrelationIdSupport.extractPayloadXml(soapMessage));
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
        MessageContextSupport.clear();
    }
}
