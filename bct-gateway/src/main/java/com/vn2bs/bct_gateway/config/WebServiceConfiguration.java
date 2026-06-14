package com.vn2bs.bct_gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfiguration {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/web-services/*");
    }

    @Bean(name = "bct-guihoso-schema")
    @Qualifier("bct-guihoso-schema")
    public XsdSchema BCT_GuiHoSo_schema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/bct/guihoso.xsd"));
    }

    @Bean(name = "bct-thu-tuc-1-gui-ho-so")
    public DefaultWsdl11Definition BCT_GuiHoSo_defaultWsdl11Definition(
            @Qualifier("bct-guihoso-schema") XsdSchema schema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("GuiHoSo");
        wsdl11Definition.setLocationUri("/web-services");
        wsdl11Definition.setTargetNamespace("com.vn2bs.webservices.bct.guihoso");
        wsdl11Definition.setSchema(schema);
        return wsdl11Definition;
    }
}
