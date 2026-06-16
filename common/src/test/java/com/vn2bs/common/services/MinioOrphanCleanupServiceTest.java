package com.vn2bs.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_TraLoiRepository;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Bucket;

@ExtendWith(MockitoExtension.class)
class MinioOrphanCleanupServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private ThuTuc1_TraLoiRepository traLoiRepository;

    @InjectMocks
    private MinioOrphanCleanupService minioOrphanCleanupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioOrphanCleanupService, "bucketPrefixes",
                "nsw-thutuc1-guihoso,bct-thutuc1-traloi");
    }

    @Test
    void cleanupOrphanBuckets_removesBucketWithoutDbRecord() throws Exception {
        Bucket orphanBucket = org.mockito.Mockito.mock(Bucket.class);
        when(orphanBucket.name()).thenReturn("nsw-thutuc1-guihoso-orphan-001");
        when(minioClient.listBuckets()).thenReturn(List.of(orphanBucket));
        when(guiHoSoRepository.findByBucketName("nsw-thutuc1-guihoso-orphan-001")).thenReturn(Optional.empty());
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(List.of());

        int removed = minioOrphanCleanupService.cleanupOrphanBuckets();

        assertEquals(1, removed);
        verify(minioClient).removeBucket(any(RemoveBucketArgs.class));
    }

    @Test
    void cleanupOrphanBuckets_skipsBucketWithDbRecord() throws Exception {
        Bucket existingBucket = org.mockito.Mockito.mock(Bucket.class);
        when(existingBucket.name()).thenReturn("nsw-thutuc1-guihoso-nsw-2026-001");
        when(minioClient.listBuckets()).thenReturn(List.of(existingBucket));
        when(guiHoSoRepository.findByBucketName("nsw-thutuc1-guihoso-nsw-2026-001"))
                .thenReturn(Optional.of(new com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo()));

        int removed = minioOrphanCleanupService.cleanupOrphanBuckets();

        assertEquals(0, removed);
        verify(minioClient, never()).removeBucket(any(RemoveBucketArgs.class));
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }
}
