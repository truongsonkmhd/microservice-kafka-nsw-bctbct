package com.vn2bs.bct_adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiRequest;
import com.vn2bs.bct_adapter.xsd.bct.thutuc1.TraLoiResponse;

@Configuration
public class SoapClientConfiguration {

    @Bean
    public Jaxb2Marshaller traLoiMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(TraLoiRequest.class, TraLoiResponse.class);
        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller traLoiMarshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(traLoiMarshaller);
        template.setUnmarshaller(traLoiMarshaller);
        return template;
    }
}
