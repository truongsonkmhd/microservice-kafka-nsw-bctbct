package com.vn2bs.bct_adapter.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class TraLoiSenderStubTest {

    @Test
    void send_logsWithoutError() {
        TraLoiSenderStub stub = new TraLoiSenderStub();
        assertDoesNotThrow(() -> stub.send("NSW-2026-001", "Phe duyet"));
    }
}
