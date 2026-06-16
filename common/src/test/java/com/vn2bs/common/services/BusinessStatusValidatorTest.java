package com.vn2bs.common.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.exception.InvalidStatusTransitionException;

class BusinessStatusValidatorTest {

    private final BusinessStatusValidator validator = new BusinessStatusValidator();

    @Test
    void allowsKhoiTaoToChoPheDuyet() {
        assertDoesNotThrow(() -> validator.validateTransition(BusinessStatus.KHOI_TAO, BusinessStatus.CHO_PHE_DUYET));
    }

    @Test
    void allowsChoPheDuyetToDaPheDuyet() {
        assertDoesNotThrow(
                () -> validator.validateTransition(BusinessStatus.CHO_PHE_DUYET, BusinessStatus.DA_PHE_DUYET));
    }

    @Test
    void rejectsChoPheDuyetToChoXuLy() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validateTransition(BusinessStatus.CHO_PHE_DUYET, BusinessStatus.CHO_XU_LY));
    }

    @Test
    void rejectsTerminalStateChange() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validateTransition(BusinessStatus.DA_HUY, BusinessStatus.DA_PHE_DUYET));
    }
}
