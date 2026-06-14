package com.vn2bs.bct_gateway.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn2bs.bct_gateway.services.BCTReceiveHandler;
import com.vn2bs.bct_gateway.xsd.bct.guihoso.GuiHoSoResponse;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("bct/thu-tuc-1")
@Slf4j
@Tags(value = { @Tag(name = "BCT ThuTuc1 Dev") })
public class BCT_ThuTuc1Rest {

    @Autowired
    private BCTReceiveHandler bctReceiveHandler;

    @Operation(
            summary = "[Dev] Nhận hồ sơ GuiHoSo qua REST",
            description = "Endpoint dev/test — production dùng SOAP từ NSW")
    @PostMapping("gui-ho-so")
    public ResponseEntity<IResponse<GuiHoSoResponse>> guiHoSo(@RequestBody GuiHoSoDto thongTin) {
        GuiHoSoResponse result = bctReceiveHandler.receiveRest(thongTin);

        IResponse<GuiHoSoResponse> body = new IResponse<>();
        body.setStatus(org.springframework.http.HttpStatus.OK);
        body.setMessage("Ok");
        body.setData(result);
        return ResponseEntity.ok(body);
    }
}
