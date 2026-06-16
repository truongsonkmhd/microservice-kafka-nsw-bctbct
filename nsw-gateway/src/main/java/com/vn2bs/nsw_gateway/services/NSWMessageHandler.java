package com.vn2bs.nsw_gateway.services;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.MessageParty;
import com.vn2bs.common.domains.MessageType;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoSubmitResponse;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.metrics.GatewayMetrics;
import com.vn2bs.common.services.MessageLogService;
import com.vn2bs.common.services.OutboxService;
import com.vn2bs.common.utils.CorrelationIdSupport;
import com.vn2bs.common.utils.NameUtil;
import com.vn2bs.nsw_gateway.mapper.ThuTuc1.GuiHoSoMapper;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NSWMessageHandler {

    private static final String BUCKET_PREFIX = "nsw-thutuc1-guihoso";

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private GuiHoSoMapper guiHoSoMapper;

    @Autowired
    private MaSoHoSoGenerator maSoHoSoGenerator;

    @Autowired
    private MessageLogService messageLogService;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private GatewayMetrics gatewayMetrics;

    @Transactional
    public GuiHoSoSubmitResponse ThuTuc1_GuiHoSo(GuiHoSoDto message, List<MultipartFile> tepDinhKem)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
            IllegalArgumentException, IOException {

        String maSoHoSo = maSoHoSoGenerator.resolve(message.getMaSoHoSo());
        message.setMaSoHoSo(maSoHoSo);
        log.info("NSW GuiHoSo maSoHoSo={} tenNguoiGui={}", maSoHoSo, message.getTenNguoiGui());

        ThuTuc1_GuiHoSo entity = guiHoSoMapper.toEntity(message);
        entity.setStatus(Status.CREATED);
        entity.setBusinessStatus(BusinessStatus.KHOI_TAO);
        entity.setCorrelationId(CorrelationIdSupport.generate());

        final String bucketName = NameUtil.toBucketNameSafe(BUCKET_PREFIX, maSoHoSo);
        entity.setBucketName(bucketName);
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        if (tepDinhKem != null) {
            for (MultipartFile file : tepDinhKem) {
                if (file != null && !file.isEmpty()) {
                    final String fileName = NameUtil.toFileNameSafe("tepDinhKem_", file.getOriginalFilename());
                    minioClient.putObject(io.minio.PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
                    log.info("Uploaded tepDinhKem: {}", file.getOriginalFilename());
                    if (entity.getTaiLieuDinhKem() == null) {
                        entity.setTaiLieuDinhKem(new java.util.ArrayList<>());
                    }
                    entity.getTaiLieuDinhKem().add(fileName);
                }
            }
        }

        entity = guiHoSoRepository.save(entity);
        log.info("Saved GuiHoSo entity: id={} maSoHoSo={} correlationId={}",
                entity.getId(), entity.getMaSoHoSo(), entity.getCorrelationId());

        messageLogService.logSent(
                entity.getCorrelationId(),
                MessageParty.NSW,
                MessageParty.NSW,
                MessageType.GUI_HO_SO,
                "<GuiHoSo maSoHoSo=\"" + entity.getMaSoHoSo() + "\"/>",
                entity.getMaSoHoSo());

        outboxService.enqueueObject(
                GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO,
                GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO_DLQ,
                entity,
                "ThuTuc1_GuiHoSo",
                entity.getMaSoHoSo());

        gatewayMetrics.recordMessage("nsw-gateway", MessageType.GUI_HO_SO.name(), "outbound");

        GuiHoSoSubmitResponse response = new GuiHoSoSubmitResponse();
        response.setMaSoHoSo(entity.getMaSoHoSo());
        response.setStatus(Status.CREATED.name());
        return response;
    }
}
