package org.choongang.global.exceptions;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends CommonException {

    public UnAuthorizedException(String code) {
        super(code, HttpStatus.UNAUTHORIZED);
        setErrorCode(true);
    }

    public UnAuthorizedException() {
        this("UNAUTHORIZED");
    }
}
