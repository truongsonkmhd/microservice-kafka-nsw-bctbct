package com.vn2bs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn2bs.common.domains.MessageLog;
import com.vn2bs.common.domains.MessageLogStatus;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    boolean existsByCorrelationIdAndLogStatus(String correlationId, MessageLogStatus logStatus);
}
