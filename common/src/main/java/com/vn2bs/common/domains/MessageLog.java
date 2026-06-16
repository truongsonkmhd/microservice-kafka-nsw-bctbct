package com.vn2bs.common.domains;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "message_log")
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true, length = 36)
    private String messageId;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    private MessageParty sender;

    @Enumerated(EnumType.STRING)
    private MessageParty receiver;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Lob
    @Column(name = "payload_xml", columnDefinition = "TEXT")
    private String payloadXml;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", nullable = false)
    private MessageLogStatus logStatus;

    @Column(name = "error_detail", length = 2000)
    private String errorDetail;

    @Column(name = "ma_so_ho_so")
    private String maSoHoSo;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @PrePersist
    void prePersist() {
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
