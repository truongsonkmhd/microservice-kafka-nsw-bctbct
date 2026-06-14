package com.vn2bs.bct_adapter.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vn2bs.bct_adapter.dto.DuyetRequest;
import com.vn2bs.bct_adapter.dto.GuiHoSoDetailDto;
import com.vn2bs.bct_adapter.dto.TuChoiRequest;
import com.vn2bs.bct_adapter.services.HoSoService;
import com.vn2bs.common.domains.BusinessStatus;
import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("bct/thu-tuc-1/ho-so")
@Slf4j
@Tags(value = { @Tag(name = "BCT ThuTuc1 — Cán bộ") })
public class HoSoRestController {

    @Autowired
    private HoSoService hoSoService;

    @Autowired
    private ResponseFactory responseFactory;

    @Operation(summary = "Danh sách hồ sơ theo trạng thái nghiệp vụ")
    @GetMapping
    public ResponseEntity<IResponse<List<GuiHoSoDto>>> list(
            @RequestParam(defaultValue = "CHO_XU_LY") BusinessStatus status) {
        List<GuiHoSoDto> data = hoSoService.listByStatus(status);
        return responseFactory.success(data);
    }

    @Operation(summary = "Chi tiết hồ sơ theo mã số")
    @GetMapping("{maSoHoSo}")
    public ResponseEntity<IResponse<GuiHoSoDetailDto>> detail(@PathVariable String maSoHoSo) {
        GuiHoSoDetailDto data = hoSoService.getDetail(maSoHoSo);
        return responseFactory.success(data);
    }

    @Operation(summary = "Phê duyệt hồ sơ")
    @PostMapping("{maSoHoSo}/duyet")
    public ResponseEntity<IResponse<String>> duyet(
            @PathVariable String maSoHoSo,
            @RequestBody DuyetRequest request) {
        hoSoService.duyet(maSoHoSo, request);
        return responseFactory.success("Duyet thanh cong");
    }

    @Operation(summary = "Từ chối hồ sơ (bắt buộc lyDo)")
    @PostMapping("{maSoHoSo}/tu-choi")
    public ResponseEntity<IResponse<String>> tuChoi(
            @PathVariable String maSoHoSo,
            @RequestBody TuChoiRequest request) {
        hoSoService.tuChoi(maSoHoSo, request);
        return responseFactory.success("Tu choi thanh cong");
    }
}
