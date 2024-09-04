package org.choongang.board.exceptions;

import org.choongang.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class GuestPasswordCheckException extends CommonException {
    public GuestPasswordCheckException() {
        super("RequiredCheck.guestPw", HttpStatus.UNAUTHORIZED);
        setErrorCode(true);
    }
}
