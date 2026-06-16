package com.vn2bs.bct_gateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn2bs.bct_gateway.mapper.ThuTuc1.GuiHoSoMapper;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoRequest;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;
import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.services.MessageLogService;
import com.vn2bs.common.services.OutboxService;
import com.vn2bs.common.utils.NameUtil;
import com.vn2bs.common.ws.MessageContextSupport;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BCTReceiveHandler {

    private static final String BUCKET_PREFIX = "bct-thutuc1-guihoso";
    private static final String ACK_SUCCESS = "success";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private GuiHoSoMapper guiHoSoMapper;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private OutboxService outboxService;

    @Transactional
    public GuiHoSoResponse receiveSoap(GuiHoSoRequest request) {
        validateSoapRequest(request);
        String correlationId = MessageContextSupport.correlationId();
        String payloadXml = MessageContextSupport.payloadXml();
        log.info("BCT receive GuiHoSo SOAP maSoHoSo={} correlationId={}",
                request.getMaSoHoSo(), correlationId);

        messageLogService.logReceived(
                correlationId,
                MessageParty.NSW,
                MessageParty.BCT,
                MessageType.GUI_HO_SO,
                payloadXml,
                request.getMaSoHoSo());

        if (guiHoSoRepository.findByMaSoHoSo(request.getMaSoHoSo()).isPresent()) {
            log.warn("GuiHoSo already exists maSoHoSo={} — idempotent ack", request.getMaSoHoSo());
            return buildAck(request.getMaSoHoSo());
        }

        ThuTuc1_GuiHoSo entity = guiHoSoMapper.fromSoapRequestToEntity(request);
        entity.setStatus(Status.CREATED);
        entity.setBusinessStatus(BusinessStatus.KHOI_TAO);
        entity.setCorrelationId(correlationId);

        final String bucketName = NameUtil.toBucketNameSafe(BUCKET_PREFIX, request.getMaSoHoSo());
        entity.setBucketName(bucketName);
        ensureBucket(bucketName);

        entity = guiHoSoRepository.save(entity);
        log.info("Saved BCT GuiHoSo entity: id={} maSoHoSo={}", entity.getId(), entity.getMaSoHoSo());

        enqueueGuiHoSo(entity);

        return buildAck(entity.getMaSoHoSo());
    }

    @Transactional
    public GuiHoSoResponse receiveRest(GuiHoSoDto dto) {
        validateDto(dto);
        log.info("BCT receive GuiHoSo REST maSoHoSo={} tenNguoiGui={}",
                dto.getMaSoHoSo(), dto.getTenNguoiGui());

        if (guiHoSoRepository.findByMaSoHoSo(dto.getMaSoHoSo()).isPresent()) {
            log.warn("GuiHoSo already exists maSoHoSo={} — idempotent ack", dto.getMaSoHoSo());
            return buildAck(dto.getMaSoHoSo());
        }

        ThuTuc1_GuiHoSo entity = guiHoSoMapper.toEntity(dto);
        entity.setStatus(Status.CREATED);
        entity.setBusinessStatus(BusinessStatus.KHOI_TAO);

        final String bucketName = NameUtil.toBucketNameSafe(BUCKET_PREFIX, dto.getMaSoHoSo());
        entity.setBucketName(bucketName);
        ensureBucket(bucketName);

        entity = guiHoSoRepository.save(entity);
        log.info("Saved BCT GuiHoSo entity: id={} maSoHoSo={}", entity.getId(), entity.getMaSoHoSo());

        enqueueGuiHoSo(entity);

        return buildAck(entity.getMaSoHoSo());
    }

    private void enqueueGuiHoSo(ThuTuc1_GuiHoSo entity) {
        outboxService.enqueueObject(
                GlobalConfig.Kafka.Topic.BCT.ThuTuc1.GUI_HO_SO,
                GlobalConfig.Kafka.Topic.BCT.ThuTuc1.GUI_HO_SO_DLQ,
                entity,
                "ThuTuc1_GuiHoSo",
                entity.getMaSoHoSo());
        messageLogService.logSent(
                entity.getCorrelationId(),
                MessageParty.BCT,
                MessageParty.BCT,
                MessageType.GUI_HO_SO,
                null,
                entity.getMaSoHoSo());
    }

    private void ensureBucket(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception ex) {
            log.error("MinIO bucket error bucket={} error={}", bucketName, ex.getMessage());
        }
    }

    private GuiHoSoResponse buildAck(String maSoHoSo) {
        GuiHoSoResponse response = new GuiHoSoResponse();
        response.setMaSoHoSo(maSoHoSo);
        response.setKetQua(ACK_SUCCESS);
        return response;
    }

    private void validateSoapRequest(GuiHoSoRequest request) {
        if (request == null || isBlank(request.getMaSoHoSo()) || isBlank(request.getTenNguoiGui())) {
            throw new IllegalArgumentException("maSoHoSo and tenNguoiGui are required");
        }
    }

    private void validateDto(GuiHoSoDto dto) {
        if (dto == null || isBlank(dto.getMaSoHoSo()) || isBlank(dto.getTenNguoiGui())) {
            throw new IllegalArgumentException("maSoHoSo and tenNguoiGui are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
