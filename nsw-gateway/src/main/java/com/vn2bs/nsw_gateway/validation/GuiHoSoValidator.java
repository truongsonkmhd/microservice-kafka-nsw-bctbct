package com.vn2bs.nsw_gateway.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.nsw_gateway.exception.BusinessException;

@Component
public class GuiHoSoValidator {

    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Autowired
    public GuiHoSoValidator(ThuTuc1_GuiHoSoRepository guiHoSoRepository) {
        this.guiHoSoRepository = guiHoSoRepository;
    }

    public void validate(GuiHoSoDto dto, List<MultipartFile> tepDinhKem) {
        if (dto == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "MISSING_THONG_TIN", "thongTin is required");
        }

        if (dto.getMaSoHoSo() != null && !dto.getMaSoHoSo().isBlank()) {
            String maSoHoSo = dto.getMaSoHoSo().trim();
            if (guiHoSoRepository.findByMaSoHoSo(maSoHoSo).isPresent()) {
                throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_MA_SO_HO_SO",
                        "maSoHoSo already exists: " + maSoHoSo);
            }
        }

        if (tepDinhKem == null) {
            return;
        }

        for (MultipartFile file : tepDinhKem) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE",
                        "File size must not exceed 10MB: " + file.getOriginalFilename());
            }
        }
    }
}
