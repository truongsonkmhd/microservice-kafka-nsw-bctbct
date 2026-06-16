package com.vn2bs.common.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(name = "minio.orphan-cleanup.enabled", havingValue = "true")
@Slf4j
public class MinioOrphanCleanupJob {

    @Autowired
    private MinioOrphanCleanupService minioOrphanCleanupService;

    @Scheduled(cron = "${minio.orphan-cleanup.cron:0 30 3 * * *}")
    public void cleanupOrphanBuckets() {
        int removed = minioOrphanCleanupService.cleanupOrphanBuckets();
        log.info("MinIO orphan cleanup finished removedBuckets={}", removed);
    }
}
