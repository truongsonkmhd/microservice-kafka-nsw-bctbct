package com.vn2bs.common.utils;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

public final class CorrelationIdSupport {

    public static final String HEADER_LOCAL_NAME = "CorrelationId";
    public static final String HEADER_NAMESPACE = "http://vn2bs.com/correlation";

    private CorrelationIdSupport() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static void setHeader(SoapMessage soapMessage, String correlationId) {
        SoapHeaderElement header = soapMessage.getSoapHeader()
                .addHeaderElement(new QName(HEADER_NAMESPACE, HEADER_LOCAL_NAME));
        header.setText(correlationId);
    }

    public static String readHeader(SoapMessage soapMessage) {
        Iterator<SoapHeaderElement> headers = soapMessage.getSoapHeader().examineAllHeaderElements();
        while (headers.hasNext()) {
            SoapHeaderElement header = headers.next();
            if (HEADER_LOCAL_NAME.equals(header.getName().getLocalPart())) {
                return header.getText();
            }
        }
        return null;
    }

    public static String extractPayloadXml(WebServiceMessage message) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(message.getPayloadSource(), new StreamResult(writer));
            return writer.toString();
        } catch (Exception ex) {
            return "<payload-unavailable>";
        }
    }
}
