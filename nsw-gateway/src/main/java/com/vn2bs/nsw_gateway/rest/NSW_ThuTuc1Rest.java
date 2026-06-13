package com.vn2bs.nsw_gateway.rest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoDto;
import com.vn2bs.common.dto.ThuTuc1.GuiHoSoSubmitResponse;
import com.vn2bs.nsw_gateway.services.NSWMessageHandler;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
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

    @PostMapping("gui-ho-so")
    public ResponseEntity<IResponse<GuiHoSoSubmitResponse>> guiHoSo(
            @RequestPart(name = "thongTin") GuiHoSoDto thongTin,
            @RequestPart(required = false) List<MultipartFile> tepDinhKem)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
            IllegalArgumentException, IOException {

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
