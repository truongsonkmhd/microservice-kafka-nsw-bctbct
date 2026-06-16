package com.vn2bs.nsw_adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.nsw_adapter.xsd.bct.guihoso.GuiHoSoResponse;

@Configuration
public class SoapClientConfiguration {

    @Bean
    public Jaxb2Marshaller guiHoSoMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(GuiHoSoRequest.class, GuiHoSoResponse.class);
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller guiHoSoMarshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(guiHoSoMarshaller);
        template.setUnmarshaller(guiHoSoMarshaller);
        return template;
    }
}
