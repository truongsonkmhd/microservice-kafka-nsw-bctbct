package com.vn2bs.bct_adapter.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn2bs.bct_adapter.services.DuyetHoSoService;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("bct/admin/reconciliation")
@Slf4j
@Tag(name = "BCT Admin — Reconciliation")
public class ReconciliationAdminController {

    @Autowired
    private DuyetHoSoService duyetHoSoService;

    @Autowired
    private ResponseFactory responseFactory;

    @Operation(summary = "Auto-replay TraLoi khi phát hiện lệch trạng thái (G4-T17)")
    @PostMapping("replay/{maSoHoSo}")
    public ResponseEntity<IResponse<Boolean>> replayTraLoi(@PathVariable String maSoHoSo) {
        log.info("Replay TraLoi requested maSoHoSo={}", maSoHoSo);
        boolean sent = duyetHoSoService.replayTraLoi(maSoHoSo);
        return responseFactory.success(sent);
    }
}
