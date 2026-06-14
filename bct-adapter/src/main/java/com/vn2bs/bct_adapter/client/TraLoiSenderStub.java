package com.vn2bs.bct_adapter.client;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TraLoiSenderStub implements TraLoiSender {

    @Override
    public void send(String maSoHoSo, String ketQua) {
        log.info("STUB TraLoiSender.send maSoHoSo={} ketQua={} — TODO G3 SOAP client", maSoHoSo, ketQua);
    }
}
