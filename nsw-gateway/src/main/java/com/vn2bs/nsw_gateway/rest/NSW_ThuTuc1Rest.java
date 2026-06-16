package com.vn2bs.nsw_gateway.rest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoSubmitResponse;
import com.vn2bs.common.dto.ThuTuc1.HoSoTraCuuResponse;
import com.vn2bs.nsw_gateway.services.HoSoTraCuuService;
import com.vn2bs.nsw_gateway.services.NSWMessageHandler;
import com.vn2bs.nsw_gateway.validation.GuiHoSoValidator;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("nsw/thu-tuc-1")
@Slf4j
@Tags(value = { @Tag(name = "NSW ThuTuc1") })
public class NSW_ThuTuc1Rest {

    @Autowired
    private NSWMessageHandler nswMessageHandler;

    @Autowired
    private GuiHoSoValidator guiHoSoValidator;

    @Autowired
    private HoSoTraCuuService hoSoTraCuuService;

    @Operation(summary = "Tra cứu trạng thái hồ sơ theo mã số")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin hồ sơ",
                    content = @Content(schema = @Schema(implementation = HoSoTraCuuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ")
    })
    @GetMapping("ho-so/{maSoHoSo}")
    public ResponseEntity<IResponse<HoSoTraCuuResponse>> traCuu(@PathVariable String maSoHoSo) {
        HoSoTraCuuResponse result = hoSoTraCuuService.traCuu(maSoHoSo);

        IResponse<HoSoTraCuuResponse> body = new IResponse<>();
        body.setStatus(HttpStatus.OK);
        body.setMessage("Ok");
        body.setData(result);
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "Nộp hồ sơ ThuTuc1",
            description = "Multipart form-data: `thongTin` (JSON GuiHoSoDto, required), `tepDinhKem` (files, optional)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hồ sơ đã tạo",
                    content = @Content(schema = @Schema(implementation = GuiHoSoSubmitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu thongTin hoặc file vượt 10MB"),
            @ApiResponse(responseCode = "409", description = "maSoHoSo trùng")
    })
    @PostMapping("gui-ho-so")
    public ResponseEntity<IResponse<GuiHoSoSubmitResponse>> guiHoSo(
            @RequestPart(name = "thongTin") GuiHoSoDto thongTin,
            @RequestPart(required = false) List<MultipartFile> tepDinhKem)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
            IllegalArgumentException, IOException {

        guiHoSoValidator.validate(thongTin, tepDinhKem);

        log.info("NSW_ThuTuc1Rest - guiHoSo: maSoHoSo={} tenNguoiGui={}",
                thongTin.getMaSoHoSo(), thongTin.getTenNguoiGui());

        GuiHoSoSubmitResponse result = nswMessageHandler.ThuTuc1_GuiHoSo(thongTin, tepDinhKem);

        IResponse<GuiHoSoSubmitResponse> body = new IResponse<>();
        body.setStatus(HttpStatus.OK);
        body.setMessage("Ok");
        body.setData(result);
        return ResponseEntity.ok(body);
    }
}
