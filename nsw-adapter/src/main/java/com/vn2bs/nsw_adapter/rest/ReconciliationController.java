package com.vn2bs.nsw_adapter.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;
import com.vn2bs.nsw_adapter.dto.ReconciliationReport;
import com.vn2bs.nsw_adapter.services.ReconciliationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("nsw/admin/reconciliation")
@Slf4j
@Tag(name = "NSW Admin — Reconciliation")
public class ReconciliationController {

    @Autowired
    private ReconciliationService reconciliationService;

    @Autowired
    private ResponseFactory responseFactory;

    @Operation(summary = "Trigger reconciliation thủ công (G4-T18)")
    @PostMapping("run")
    public ResponseEntity<IResponse<ReconciliationReport>> run() {
        log.info("Manual reconciliation triggered");
        ReconciliationReport report = reconciliationService.reconcile();
        return responseFactory.success(report);
    }
}
