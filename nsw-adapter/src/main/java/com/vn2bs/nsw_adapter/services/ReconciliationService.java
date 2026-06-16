package com.vn2bs.nsw_adapter.services;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.nsw_adapter.dto.ReconciliationReport;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReconciliationService {

    private static final Set<BusinessStatus> BCT_PROCESSED = EnumSet.of(
            BusinessStatus.DA_XU_LY,
            BusinessStatus.DA_GUI_KET_QUA);

    @Autowired
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Value("${bct.adapter.url:http://localhost:8085}")
    private String bctAdapterUrl;

    @Value("${reconciliation.auto-replay:true}")
    private boolean autoReplay;

    private final RestClient restClient = RestClient.create();

    @Scheduled(cron = "${reconciliation.cron:0 0 2 * * *}")
    public void scheduledReconcile() {
        log.info("Starting scheduled reconciliation");
        ReconciliationReport report = reconcile();
        log.info("Scheduled reconciliation done checked={} discrepancies={}",
                report.getCheckedCount(), report.getDiscrepancyCount());
    }

    public ReconciliationReport reconcile() {
        ReconciliationReport report = new ReconciliationReport();
        List<ThuTuc1_GuiHoSo> pending = guiHoSoRepository.findByBusinessStatus(BusinessStatus.CHO_PHE_DUYET);

        for (ThuTuc1_GuiHoSo nswHoSo : pending) {
            report.setCheckedCount(report.getCheckedCount() + 1);
            BusinessStatus bctStatus = fetchBctStatus(nswHoSo.getMaSoHoSo());
            if (bctStatus == null) {
                continue;
            }

            if (BCT_PROCESSED.contains(bctStatus)) {
                String message = String.format(
                        "DISCREPANCY maSoHoSo=%s NSW=%s BCT=%s",
                        nswHoSo.getMaSoHoSo(), nswHoSo.getBusinessStatus(), bctStatus);
                report.getDiscrepancies().add(message);
                report.setDiscrepancyCount(report.getDiscrepancyCount() + 1);
                log.error("[RECONCILIATION ALERT] {}", message);

                if (autoReplay) {
                    triggerReplay(nswHoSo.getMaSoHoSo(), report);
                }
            }
        }
        return report;
    }

    private BusinessStatus fetchBctStatus(String maSoHoSo) {
        try {
            IResponse<BctStatusData> response = restClient.get()
                    .uri(bctAdapterUrl + "/bct/thu-tuc-1/ho-so/{maSoHoSo}", maSoHoSo)
                    .retrieve()
                    .body(new ParameterizedTypeReference<IResponse<BctStatusData>>() {
                    });
            if (response != null && response.getData() != null) {
                return response.getData().getBusinessStatus();
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch BCT status maSoHoSo={} error={}", maSoHoSo, ex.getMessage());
        }
        return null;
    }

    private void triggerReplay(String maSoHoSo, ReconciliationReport report) {
        try {
            restClient.post()
                    .uri(bctAdapterUrl + "/bct/admin/reconciliation/replay/{maSoHoSo}", maSoHoSo)
                    .retrieve()
                    .toBodilessEntity();
            report.getReplayTriggered().add(maSoHoSo);
            log.info("Auto-replay TraLoi triggered maSoHoSo={}", maSoHoSo);
        } catch (Exception ex) {
            log.error("Auto-replay failed maSoHoSo={} error={}", maSoHoSo, ex.getMessage());
        }
    }

    @lombok.Data
    static class BctStatusData {
        private BusinessStatus businessStatus;
    }
}
