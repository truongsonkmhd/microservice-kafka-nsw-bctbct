package com.vn2bs.nsw_adapter.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ReconciliationReport {

    private int checkedCount;
    private int discrepancyCount;
    private List<String> discrepancies = new ArrayList<>();
    private List<String> replayTriggered = new ArrayList<>();
}
