package com.vn2bs.bct_adapter.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<IResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        return responseFactory.error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<IResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {
        return responseFactory.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
