package org.choongang.board.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestComment {
    private String mode = "write";

    private Long seq; // 댓글 등록번호

    private Long boardDataSeq; // 게시글 번호

    @NotBlank
    private String commenter; // 작성자

    private String guestPw; // 비회원 비밀번호

    @NotBlank
    private String content; // 댓글 내용
}