package com.communityhelp.app.security;

import com.communityhelp.app.common.exceptions.ApiErrorResponse;
import com.communityhelp.app.common.exceptions.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SecurityResponseWriter {

    private final ObjectMapper objectMapper;

    public void writeSecurityError(
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode,
            String message
    ) throws IOException {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(status.value())
                .code(errorCode.name())
                .message(message)
                .errors(Collections.emptyList())
                .build();

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), body);
    }
}