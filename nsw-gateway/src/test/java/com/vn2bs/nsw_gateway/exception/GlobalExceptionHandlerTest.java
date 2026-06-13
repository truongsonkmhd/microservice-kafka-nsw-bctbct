package com.vn2bs.nsw_gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.vn2bs.common.dto.IResponse;
import com.vn2bs.common.dto.ResponseFactory;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ResponseFactory responseFactory;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleBusinessException_returnsStatusFromException() {
        BusinessException ex = new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_MA_SO_HO_SO", "duplicate");
        ResponseEntity<IResponse<String>> expected = ResponseEntity.status(HttpStatus.CONFLICT).build();

        when(responseFactory.error(HttpStatus.CONFLICT, "duplicate")).thenReturn(expected);

        ResponseEntity<IResponse<String>> result = globalExceptionHandler.handleBusinessException(ex);

        assertEquals(expected, result);
    }

    @Test
    void handleMissingThongTin_returnsBadRequest() {
        MissingServletRequestPartException ex = new MissingServletRequestPartException("thongTin");
        ResponseEntity<IResponse<String>> expected = ResponseEntity.badRequest().build();

        when(responseFactory.error(HttpStatus.BAD_REQUEST, "thongTin is required")).thenReturn(expected);

        ResponseEntity<IResponse<String>> result = globalExceptionHandler.handleMissingPart(ex);

        assertEquals(expected, result);
    }
}
