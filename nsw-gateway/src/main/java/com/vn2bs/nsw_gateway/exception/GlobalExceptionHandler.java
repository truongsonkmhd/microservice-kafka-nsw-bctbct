package com.vn2bs.nsw_gateway.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ResponseFactory responseFactory;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<IResponse<String>> handleBusinessException(BusinessException ex) {
        return responseFactory.error(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<IResponse<String>> handleMissingPart(MissingServletRequestPartException ex) {
        if ("thongTin".equals(ex.getRequestPartName())) {
            return responseFactory.error(HttpStatus.BAD_REQUEST, "thongTin is required");
        }
        return responseFactory.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
