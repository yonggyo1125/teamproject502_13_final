package org.choongang.board.services;

import lombok.RequiredArgsConstructor;
import org.choongang.board.entities.Board;
import org.choongang.board.entities.BoardData;
import org.choongang.board.entities.CommentData;
import org.choongang.board.exceptions.BoardNotFoundException;
import org.choongang.board.exceptions.GuestPasswordCheckException;
import org.choongang.board.exceptions.GuestPasswordMismatchException;
import org.choongang.board.services.comment.CommentInfoService;
import org.choongang.board.services.config.BoardConfigInfoService;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.CommonException;
import org.choongang.global.exceptions.UnAuthorizedException;
import org.choongang.global.services.SessionService;
import org.choongang.member.MemberUtil;
import org.choongang.member.constants.Authority;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardAuthService {
    private final MemberUtil memberUtil;
    private final BoardConfigInfoService configInfoService;
    private final BoardInfoService infoService;
    private final CommentInfoService commentInfoService;
    private final PasswordEncoder encoder;
    private final SessionService sessionService;
    private final Utils utils;


    private Board board;
    private BoardData boardData;

    /**
     *  권한 체크
     * @param mode
     *          - list, write, update, view, comment_update, comment_delete ..
     * @param seq : 게시글 번호
     */
    public void check(String mode, Long seq) {

        // 관리자는 권한 체크 X
        if (memberUtil.isAdmin()) {
            return;
        }

        sessionService.save("mode", mode);
        if (List.of("list", "write", "update", "view").contains(mode)) {
            if (seq != null && seq.longValue() != 0L) {
                boardData = infoService.get(seq);
            }

            if (board == null) {
                board = boardData.getBoard();
            }

            // 게시판 사용 여부 체크
            if (!board.isActive()) {
                throw new BoardNotFoundException();
            }

            // 게시글 목록 접근 권한 체크
            Authority authority = board.getListAccessType();
            if (mode.equals("list") && ((authority == Authority.USER && !memberUtil.isLogin()) || (authority == Authority.ADMIN && !memberUtil.isAdmin()))) {
                throw new UnAuthorizedException();
            }

            // 게시글 보기 접근 권한 체크
            Authority viewAuthority = board.getViewAccessType();
            if (mode.equals("view") && ((viewAuthority == Authority.USER && !memberUtil.isLogin()) || (viewAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
                throw new UnAuthorizedException();
            }

            // 글쓰기 접근 권한 체크
            Authority writeAuthority = board.getWriteAccessType();
            if (mode.equals("write") && ((writeAuthority == Authority.USER && !memberUtil.isLogin()) || (writeAuthority == Authority.ADMIN && !memberUtil.isAdmin()))) {
                throw new UnAuthorizedException();
            }

            /**
             * 글 수정, 삭제 - 작성자만 수정 가능
             *      - 회원 게시글은 로그인한 사용자와 일치여부
             *      - 비회원 게시글은 인증 여부 체크 -> 인증 X -> 비밀번호 확인 페이지로 이동 검증
             *      - 검증 완료된 경우, 문제 X
             */

            if (List.of("update", "delete").contains(mode) && boardData != null && !boardData.isEditable()) {
                if (boardData.getEmail() == null) {
                    // 비회원 게시글 - 비밀번호 검증 필요

                    throw new GuestPasswordCheckException();
                }

                throw new UnAuthorizedException();
            }
        } else if (List.of("comment_update", "comment_delete").contains(mode)){
            CommentData commentData = commentInfoService.get(seq);
            if (!commentData.isEditable()) {
                if (commentData.getEmail() == null) {
                    // 비회원 댓글 - 비밀번호 검증 필요

                    throw new GuestPasswordCheckException();
                }

                throw new UnAuthorizedException();
            }
        }
    }

    /**
     *
     * @param bid - 게시판 ID
     * @param mode - write, list
     */
    public void check(String mode, String bid) {
        board = configInfoService.get(bid);

        check(mode, 0L);
    }

    /**
     * 비회원 비밀번호 검증
     *
     * @param password
     * @param boardData
     */
    public void validate(String password, BoardData boardData) {
        if (boardData == null) {
            throw new UnAuthorizedException();
        }


        if (!StringUtils.hasText(password)) {
            throw new CommonException(utils.getMessage("NotBlank.password"), HttpStatus.BAD_REQUEST);
        }

        String mode = sessionService.get("mode");
        mode = StringUtils.hasText(mode) ? mode : "update";
        if (List.of("update", "delete").contains(mode)) { // 게시글 수정, 삭제인 경우
            if (!encoder.matches(password, boardData.getGuestPw())) {
                throw new GuestPasswordMismatchException();
            }

            String key = "confirm_board_data_" + boardData.getSeq();
            sessionService.save(key, "true");
        }


    }

    /**
     * 비회원 댓글 비밀번호 검증
     *
     * @param password
     * @param commentData
     */
    public void validate(String password, CommentData commentData) {
        if (commentData == null) {
            throw new UnAuthorizedException();
        }


        if (!StringUtils.hasText(password)) {
            throw new CommonException(utils.getMessage("NotBlank.password"), HttpStatus.BAD_REQUEST);
        }

        String mode = sessionService.get("mode");
        mode = StringUtils.hasText(mode) ? mode : "comment_update";
        if (List.of("comment_update", "comment_delete").contains(mode)) { // 댓글 수정, 삭제인 경우
            if (!encoder.matches(password, commentData.getGuestPw())) {
                throw new GuestPasswordMismatchException();
            }
            String key = "confirm_comment_data_" + commentData.getSeq();
            sessionService.save(key, "true");
        }
    }
}