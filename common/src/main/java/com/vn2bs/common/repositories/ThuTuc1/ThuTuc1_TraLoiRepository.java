package com.vn2bs.common.repositories.ThuTuc1;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_TraLoi;

@Repository
public interface ThuTuc1_TraLoiRepository extends JpaRepository<ThuTuc1_TraLoi, Long> {

    Optional<ThuTuc1_TraLoi> findByMaSoHoSo(String maSoHoSo);

    Optional<ThuTuc1_TraLoi> findByBucketName(String bucketName);
}
