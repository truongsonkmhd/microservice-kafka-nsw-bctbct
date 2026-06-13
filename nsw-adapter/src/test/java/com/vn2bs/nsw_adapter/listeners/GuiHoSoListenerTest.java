package com.vn2bs.nsw_adapter.listeners;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vn2bs.common.domains.Status;
import com.vn2bs.common.domains.ThuTuc1.ThuTuc1_GuiHoSo;
import com.vn2bs.nsw_adapter.services.GuiHoSoService;

@ExtendWith(MockitoExtension.class)
class GuiHoSoListenerTest {

    @Mock
    private GuiHoSoService guiHoSoService;

    @InjectMocks
    private GuiHoSoListener guiHoSoListener;

    @Test
    void onGuiHoSo_delegatesToService() {
        ThuTuc1_GuiHoSo message = new ThuTuc1_GuiHoSo();
        message.setMaSoHoSo("NSW-2026-0001");
        message.setStatus(Status.CREATED);

        guiHoSoListener.onGuiHoSo(message);

        verify(guiHoSoService).process(message);
    }
}
