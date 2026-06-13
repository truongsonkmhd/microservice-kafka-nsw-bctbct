package com.vn2bs.nsw_gateway.services;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vn2bs.common.config.GlobalConfig;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoSubmitResponse;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
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
    private KafkaTemplate<String, ThuTuc1_GuiHoSo> kafkaTemplate;

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
        log.info("Saved GuiHoSo entity: id={} maSoHoSo={}", entity.getId(), entity.getMaSoHoSo());

        try {
            kafkaTemplate.send(GlobalConfig.Kafka.Topic.NSW.ThuTuc1.GUI_HO_SO, entity);
        } catch (Exception ex) {
            log.error("Error sending GuiHoSo to Kafka id={} error={}", entity.getId(), ex.getMessage());
        }

        GuiHoSoSubmitResponse response = new GuiHoSoSubmitResponse();
        response.setMaSoHoSo(entity.getMaSoHoSo());
        response.setStatus(Status.CREATED.name());
        return response;
    }
}
