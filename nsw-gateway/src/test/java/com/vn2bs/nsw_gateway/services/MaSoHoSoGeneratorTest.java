package com.vn2bs.nsw_gateway.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;

@ExtendWith(MockitoExtension.class)
class MaSoHoSoGeneratorTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-13T10:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    private MaSoHoSoGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new MaSoHoSoGenerator(guiHoSoRepository, FIXED_CLOCK);
    }

    @Test
    void resolve_whenProvided_returnsTrimmedValue() {
        assertEquals("BCT-2026-0001", generator.resolve("  BCT-2026-0001  "));
    }

    @Test
    void resolve_whenNull_generatesFirstCodeForYear() {
        when(guiHoSoRepository.findByMaSoHoSo(anyString())).thenReturn(Optional.empty());

        String result = generator.resolve(null);

        assertEquals("NSW-2026-0001", result);
        assertTrue(MaSoHoSoGenerator.CODE_PATTERN.matcher(result).matches());
    }

    @Test
    void resolve_whenBlank_skipsTakenSequence() {
        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-0001"))
                .thenReturn(Optional.of(new ThuTuc1_GuiHoSo()));
        when(guiHoSoRepository.findByMaSoHoSo("NSW-2026-0002"))
                .thenReturn(Optional.empty());

        String result = generator.resolve("   ");

        assertEquals("NSW-2026-0002", result);
    }
}
