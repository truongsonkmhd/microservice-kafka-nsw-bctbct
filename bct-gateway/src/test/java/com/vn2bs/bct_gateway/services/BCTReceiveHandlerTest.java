package com.vn2bs.bct_gateway.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.vn2bs.bct_gateway.mapper.ThuTuc1.GuiHoSoMapper;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

import io.minio.MinioClient;

@ExtendWith(MockitoExtension.class)
class BCTReceiveHandlerTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private GuiHoSoMapper guiHoSoMapper;

    @Mock
    private KafkaTemplate<String, ThuTuc1_GuiHoSo> kafkaTemplate;

    @InjectMocks
    private BCTReceiveHandler bctReceiveHandler;

    @Test
    void receiveSoap_savesEntityAndReturnsSuccessAck() throws Exception {
        GuiHoSoRequest request = new GuiHoSoRequest();
        request.setMaSoHoSo("NSW-2026-001");
        request.setTenNguoiGui("Cong ty ABC");

        ThuTuc1_GuiHoSo entity = new ThuTuc1_GuiHoSo();
        entity.setMaSoHoSo("NSW-2026-001");
        entity.setTenNguoiGui("Cong ty ABC");

        ThuTuc1_GuiHoSo saved = new ThuTuc1_GuiHoSo();
        saved.setId(1L);
        saved.setMaSoHoSo("NSW-2026-001");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001")).thenReturn(Optional.empty());
        when(guiHoSoMapper.fromSoapRequestToEntity(request)).thenReturn(entity);
        when(minioClient.bucketExists(any())).thenReturn(false);
        when(guiHoSoRepository.save(entity)).thenReturn(saved);

        GuiHoSoResponse response = bctReceiveHandler.receiveSoap(request);

        assertEquals("NSW-2026-001", response.getMaSoHoSo());
        assertEquals("success", response.getKetQua());
        assertEquals(Status.CREATED, entity.getStatus());
        assertEquals(BusinessStatus.KHOI_TAO, entity.getBusinessStatus());
        verify(kafkaTemplate).send(eq(GlobalConfig.Kafka.Topic.BCT.ThuTuc1.GUI_HO_SO), eq(saved));
    }

    @Test
    void receiveSoap_duplicateMaSoHoSo_returnsIdempotentAck() {
        GuiHoSoRequest request = new GuiHoSoRequest();
        request.setMaSoHoSo("NSW-2026-001");
        request.setTenNguoiGui("Cong ty ABC");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-001"))
                .thenReturn(Optional.of(new ThuTuc1_GuiHoSo()));

        GuiHoSoResponse response = bctReceiveHandler.receiveSoap(request);

        assertEquals("NSW-2026-001", response.getMaSoHoSo());
        assertEquals("success", response.getKetQua());
        verify(guiHoSoRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void receiveSoap_missingFields_throwsIllegalArgumentException() {
        GuiHoSoRequest request = new GuiHoSoRequest();
        request.setMaSoHoSo("NSW-2026-001");

        assertThrows(IllegalArgumentException.class, () -> bctReceiveHandler.receiveSoap(request));
    }
}
