package org.choongang.global.advices;

import lombok.RequiredArgsConstructor;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.CommonException;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestControllerAdvice("org.choongang")
public class CommonControllerAdvice {
    private final Utils utils;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSONData> errorHandler(Exception e) {

        Object message = e.getMessage();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        if (e instanceof CommonException commonException) {
            status = commonException.getStatus();

            // 에러 코드인 경우는 메세지 조회
            if (commonException.isErrorCode()) message = utils.getMessage(e.getMessage());

            Map<String, List<String>> errorMessages = commonException.getErrorMessages();
            if (errorMessages != null) message = errorMessages;
        }

        String errorCode = null;
        if (e instanceof BadCredentialsException) { // 아이디 또는 비밀번호가 일치하지 않는 경우
            errorCode = "BadCredentials.Login";
        } else if (e instanceof DisabledException) { // 탈퇴한 회원
            errorCode = "Disabled.Login";
        } else if (e instanceof CredentialsExpiredException) { // 비밀번호 유효기간 만료
            errorCode = "CredentialsExpired.Login";
        } else if (e instanceof AccountExpiredException) { // 사용자 계정 유효기간 만료
            errorCode = "AccountExpired.Login";
        } else if (e instanceof LockedException) { // 사용자 계정이 잠겨있는 경우
            errorCode = "Locked.Login";
        } else if (e instanceof AuthenticationException) {
            errorCode = "Fail.Login";
        } else if (e instanceof AuthorizationDeniedException) {
            errorCode = "UnAuthorized";
        }

        if (StringUtils.hasText(errorCode)) {
            message = utils.getMessage(errorCode);
            status = HttpStatus.UNAUTHORIZED;
        }

        JSONData data = new JSONData();
        data.setSuccess(false);
        data.setMessage(message);
        data.setStatus(status);

        e.printStackTrace();

        return ResponseEntity.status(status).body(data);
    }
}
