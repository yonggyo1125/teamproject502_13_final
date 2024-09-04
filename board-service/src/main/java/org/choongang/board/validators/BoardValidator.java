package org.choongang.board.validators;

import lombok.RequiredArgsConstructor;
import org.choongang.board.controllers.RequestBoard;
import org.choongang.global.validators.PasswordValidator;
import org.choongang.member.MemberUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class BoardValidator implements Validator, PasswordValidator {

    private final MemberUtil memberUtil;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestBoard.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestBoard form = (RequestBoard) target;

        String bid = form.getBid();
        Long seq = form.getSeq();
        if (seq == null && !StringUtils.hasText(bid)) {
            errors.rejectValue("bid", "NotBlank");
        }

        // 비회원 비밀번호 유효성 검사
        if (!memberUtil.isLogin()) {
            String guestPw = form.getGuestPw();
            if (!StringUtils.hasText(guestPw)) {
                errors.rejectValue("guestPw", "NotBlank");
            } else {
                /**
                 * 비밀번호 복잡성
                 *  1. 자리수는? 4자리 이상
                 *  2. 숫자 + 알파벳
                 */
                if (guestPw.length() < 4) {
                    errors.rejectValue("guestPw", "Size");
                }

                if (!numberCheck(guestPw) || !alphaCheck(guestPw, true)) {
                    errors.rejectValue("guestPw", "Complexity");
                }
            }
        }

        /**
         * 글 수정 모드인 경우에는 seq 필수
         */
        String mode = form.getMode();
        mode = StringUtils.hasText(mode) ? mode : "write";
        if (mode.equals("update") && (form.getSeq() == null || form.getSeq() < 1L)) {
            errors.rejectValue("seq", "NotBlank");
        }

        // 공지글은 관리자만 작성 가능, 관리자가 아닌 경우 false로 고정
        if (!memberUtil.isAdmin()) {
            form.setNotice(false);
        }
    }
}
