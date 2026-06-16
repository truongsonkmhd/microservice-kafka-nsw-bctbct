package com.vn2bs.common.services;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.exception.InvalidStatusTransitionException;

@Component
public class BusinessStatusValidator {

    private static final Map<BusinessStatus, Set<BusinessStatus>> ALLOWED = buildAllowed();

    public void validateTransition(BusinessStatus current, BusinessStatus target) {
        if (current == null) {
            current = BusinessStatus.KHOI_TAO;
        }
        if (current == target) {
            return;
        }
        Set<BusinessStatus> allowedTargets = ALLOWED.getOrDefault(current, Set.of());
        if (!allowedTargets.contains(target)) {
            throw new InvalidStatusTransitionException(
                    "Invalid business status transition: " + current + " -> " + target);
        }
    }

    private static Map<BusinessStatus, Set<BusinessStatus>> buildAllowed() {
        Map<BusinessStatus, Set<BusinessStatus>> map = new EnumMap<>(BusinessStatus.class);
        map.put(BusinessStatus.KHOI_TAO, EnumSet.of(BusinessStatus.CHO_XU_LY, BusinessStatus.CHO_PHE_DUYET));
        map.put(BusinessStatus.CHO_XU_LY, EnumSet.of(BusinessStatus.DA_XU_LY));
        map.put(BusinessStatus.DA_XU_LY, EnumSet.of(BusinessStatus.DA_GUI_KET_QUA));
        map.put(BusinessStatus.CHO_PHE_DUYET, EnumSet.of(BusinessStatus.DA_PHE_DUYET, BusinessStatus.TU_CHOI));
        map.put(BusinessStatus.DA_PHE_DUYET, EnumSet.noneOf(BusinessStatus.class));
        map.put(BusinessStatus.TU_CHOI, EnumSet.noneOf(BusinessStatus.class));
        map.put(BusinessStatus.DA_GUI_KET_QUA, EnumSet.noneOf(BusinessStatus.class));
        map.put(BusinessStatus.DA_HUY, EnumSet.noneOf(BusinessStatus.class));
        return map;
    }
}
