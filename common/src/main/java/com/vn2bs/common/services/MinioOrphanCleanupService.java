package com.vn2bs.common.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MinioOrphanCleanupService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @Value("${minio.orphan-cleanup.bucket-prefixes:nsw-thutuc1-guihoso,bct-thutuc1-guihoso,bct-thutuc1-traloi}")
    private String bucketPrefixes;

    public int cleanupOrphanBuckets() {
        int removed = 0;
        try {
            List<String> prefixes = Arrays.stream(bucketPrefixes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (Bucket bucket : minioClient.listBuckets()) {
                String bucketName = bucket.name();
                if (!matchesManagedPrefix(bucketName, prefixes)) {
                    continue;
                }
                if (isOrphanBucket(bucketName)) {
                    deleteBucket(bucketName);
                    removed++;
                    log.warn("Removed orphan MinIO bucket={}", bucketName);
                }
            }
        } catch (Exception ex) {
            log.error("MinIO orphan cleanup failed error={}", ex.getMessage());
        }
        return removed;
    }

    private boolean matchesManagedPrefix(String bucketName, List<String> prefixes) {
        return prefixes.stream().anyMatch(bucketName::startsWith);
    }

    private boolean isOrphanBucket(String bucketName) {
        if (bucketName.startsWith("bct-thutuc1-traloi")) {
            return traLoiRepository.findByBucketName(bucketName).isEmpty();
        }
        return guiHoSoRepository.findByBucketName(bucketName).isEmpty();
    }

    private void deleteBucket(String bucketName) throws Exception {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());
        for (Result<Item> result : objects) {
            Item item = result.get();
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(item.objectName())
                    .build());
        }
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
}
