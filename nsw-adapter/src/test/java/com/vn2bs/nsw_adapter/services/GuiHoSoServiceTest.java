package com.vn2bs.nsw_adapter.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.common.repositories.ThuTuc1.ThuTuc1_GuiHoSoRepository;
import com.vn2bs.nsw_adapter.client.BctGuiHoSoClient;

@ExtendWith(MockitoExtension.class)
class GuiHoSoServiceTest {

    @Mock
    private ThuTuc1_GuiHoSoRepository guiHoSoRepository;

    @Mock
    private BctGuiHoSoClient bctGuiHoSoClient;

    @InjectMocks
    private GuiHoSoService guiHoSoService;

    @Test
    void process_updatesStatusCreatedToProcessingToCompleted() {
        ThuTuc1_GuiHoSo entity = new ThuTuc1_GuiHoSo();
        entity.setId(1L);
        entity.setMaSoHoSo("NSW-2026-0001");
        entity.setStatus(Status.CREATED);

        List<Status> savedStatuses = new ArrayList<>();

        when(guiHoSoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(guiHoSoRepository.save(any(ThuTuc1_GuiHoSo.class))).thenAnswer(inv -> {
            ThuTuc1_GuiHoSo saved = inv.getArgument(0);
            savedStatuses.add(saved.getStatus());
            return saved;
        });
        when(bctGuiHoSoClient.sendGuiHoSo("NSW-2026-0001", null)).thenReturn("success");

        guiHoSoService.process(entity);

        assertEquals(List.of(Status.PROCESSING, Status.COMPLETED), savedStatuses);
    }
}
