package com.vn2bs.nsw_gateway.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.vn2bs.nsw_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.nsw_gateway.xsd.bct.guihoso.GuiHoSoResponse;
import com.vn2bs.nsw_gateway.xsd.bct.guihoso.ObjectFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

class SoapContractTest {

    private static final String GUI_HO_SO_XSD = "xsd/bct/guihoso.xsd";
    private static final String THU_TUC1_XSD = "xsd/bct/thutuc1.xsd";

    @Test
    void guihosoXsd_isWellFormedAndValidatesSampleRequest() throws Exception {
        Schema schema = loadSchema(GUI_HO_SO_XSD);
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GuiHoSoRequest xmlns="guihoso.bct.xsd.nsw_gateway.vn2bs.com">
                  <maSoHoSo>NSW-2026-0001</maSoHoSo>
                  <tenNguoiGui>Cong ty ABC</tenNguoiGui>
                </GuiHoSoRequest>
                """;
        assertThatCode(() -> validate(schema, xml)).doesNotThrowAnyException();
    }

    @Test
    void guihosoXsd_validatesSampleResponse() throws Exception {
        Schema schema = loadSchema(GUI_HO_SO_XSD);
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <GuiHoSoResponse xmlns="guihoso.bct.xsd.nsw_gateway.vn2bs.com">
                  <maSoHoSo>NSW-2026-0001</maSoHoSo>
                  <ketQua>OK</ketQua>
                </GuiHoSoResponse>
                """;
        assertThatCode(() -> validate(schema, xml)).doesNotThrowAnyException();
    }

    @Test
    void thutuc1Xsd_isWellFormed() throws Exception {
        assertThatCode(() -> loadSchema(THU_TUC1_XSD)).doesNotThrowAnyException();
    }

    @Test
    void guihosoJaxb_roundTripMatchesXsd() throws Exception {
        ObjectFactory factory = new ObjectFactory();
        GuiHoSoRequest request = factory.createGuiHoSoRequest();
        request.setMaSoHoSo("NSW-2026-0099");
        request.setTenNguoiGui("Contract Test");

        JAXBContext context = JAXBContext.newInstance(GuiHoSoRequest.class, GuiHoSoResponse.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        java.io.StringWriter writer = new java.io.StringWriter();
        marshaller.marshal(request, writer);

        Schema schema = loadSchema(GUI_HO_SO_XSD);
        assertThatCode(() -> validate(schema, writer.toString())).doesNotThrowAnyException();

        Unmarshaller unmarshaller = context.createUnmarshaller();
        GuiHoSoRequest parsed = (GuiHoSoRequest) unmarshaller.unmarshal(new StringReader(writer.toString()));
        assertThat(parsed.getMaSoHoSo()).isEqualTo("NSW-2026-0099");
        assertThat(parsed.getTenNguoiGui()).isEqualTo("Contract Test");
    }

    private Schema loadSchema(String classpathLocation) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return factory.newSchema(new ClassPathResource(classpathLocation).getURL());
    }

    private void validate(Schema schema, String xml) throws Exception {
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xml)));
    }
}
