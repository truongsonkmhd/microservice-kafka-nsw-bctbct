package com.vn2bs.nsw_gateway.services;

import java.time.Clock;
import java.time.Year;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

@Service
public class MaSoHoSoGenerator {

    static final Pattern CODE_PATTERN = Pattern.compile("^NSW-\\d{4}-\\d{4}$");

    private static final String CODE_PREFIX = "NSW";
    private static final int MAX_ATTEMPTS = 9999;

    private final ThuTuc1_GuiHoSoRepository guiHoSoRepository;
    private final Clock clock;

    @Autowired
    public MaSoHoSoGenerator(ThuTuc1_GuiHoSoRepository guiHoSoRepository) {
        this(guiHoSoRepository, Clock.systemDefaultZone());
    }

    MaSoHoSoGenerator(ThuTuc1_GuiHoSoRepository guiHoSoRepository, Clock clock) {
        this.guiHoSoRepository = guiHoSoRepository;
        this.clock = clock;
    }

    public String resolve(String maSoHoSo) {
        if (maSoHoSo != null && !maSoHoSo.isBlank()) {
            return maSoHoSo.trim();
        }
        return generateUnique();
    }

    private String generateUnique() {
        int year = Year.now(clock).getValue();
        String prefix = CODE_PREFIX + "-" + year + "-";

        for (int seq = 1; seq <= MAX_ATTEMPTS; seq++) {
            String candidate = prefix + String.format("%04d", seq);
            if (guiHoSoRepository.findByMaSoHoSo(candidate).isEmpty()) {
                return candidate;
            }
        }

        throw new IllegalStateException("Cannot generate unique maSoHoSo for year " + year);
    }
}
