package com.vn2bs.nsw_gateway.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.nsw_gateway.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class GuiHoSoValidatorTest {

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    private GuiHoSoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GuiHoSoValidator(guiHoSoRepository);
    }

    @Test
    void validate_nullDto_throwsBadRequest() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("MISSING_THONG_TIN", ex.getErrorCode());
    }

    @Test
    void validate_duplicateMaSoHoSo_throwsConflict() {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setMaSoHoSo("NSW-2026-0001");

        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-0001"))
                .thenReturn(Optional.of(new ThuTuc1_GuiHoSo()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(dto, null));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("DUPLICATE_MA_SO_HO_SO", ex.getErrorCode());
    }

    @Test
    void validate_fileTooLarge_throwsBadRequest() {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setTenNguoiGui("Cong ty ABC");

        MockMultipartFile largeFile = new MockMultipartFile(
                "tepDinhKem",
                "large.pdf",
                "application/pdf",
                new byte[(int) GuiHoSoValidator.MAX_FILE_SIZE_BYTES + 1]);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(dto, List.of(largeFile)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("FILE_TOO_LARGE", ex.getErrorCode());
    }

    @Test
    void validate_validInput_passes() {
        GuiHoSoDto dto = new GuiHoSoDto();
        dto.setTenNguoiGui("Cong ty ABC");

        MockMultipartFile file = new MockMultipartFile(
                "tepDinhKem", "doc.pdf", "application/pdf", new byte[1024]);

        assertDoesNotThrow(() -> validator.validate(dto, List.of(file)));
    }
}
