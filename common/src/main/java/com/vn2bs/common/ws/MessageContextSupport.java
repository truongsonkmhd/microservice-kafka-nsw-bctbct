package com.vn2bs.common.ws;

import com.vn2bs.common.utils.CorrelationIdSupport;

public final class MessageContextSupport {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PAYLOAD_XML = new ThreadLocal<>();

    private MessageContextSupport() {
    }

    public static void setCorrelationId(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static void setPayloadXml(String payloadXml) {
        PAYLOAD_XML.set(payloadXml);
    }

    public static String correlationId() {
        String value = CORRELATION_ID.get();
        return value != null ? value : CorrelationIdSupport.generate();
    }

    public static String payloadXml() {
        return PAYLOAD_XML.get();
    }

    public static void clear() {
        CORRELATION_ID.remove();
        PAYLOAD_XML.remove();
    }
}
