package org.choongang.board.services.comment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.board.controllers.RequestComment;
import org.choongang.board.entities.BoardData;
import org.choongang.board.entities.CommentData;
import org.choongang.board.exceptions.BoardDataNotFoundException;
import org.choongang.board.exceptions.CommentNotFoundException;
import org.choongang.board.repositories.BoardDataRepository;
import org.choongang.board.repositories.CommentDataRepository;
import org.choongang.member.MemberUtil;
import org.choongang.member.entities.Member;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CommentSaveService {

    private final CommentDataRepository commentDataRepository;
    private final BoardDataRepository boardDataRepository;

    private final CommentInfoService commentInfoService;

    private final MemberUtil memberUtil;
    private final PasswordEncoder encoder;
    private final HttpServletRequest request;

    public CommentData save(RequestComment form) {

        String mode = form.getMode();
        Long seq = form.getSeq(); // 댓글 번호

        mode = StringUtils.hasText(mode) ? mode : "write";

        CommentData data = null;
        if (mode.equals("update") && seq != null) { // 댓글 수정
            data = commentDataRepository.findById(seq).orElseThrow(CommentNotFoundException::new);

        } else { // 댓글 추가
            data = new CommentData();
            // 게시글 번호는 변경 X -> 추가할 때 최초 1번만 반영
            Long boardDataSeq = form.getBoardDataSeq();
            BoardData boardData = boardDataRepository.findById(boardDataSeq).orElseThrow(BoardDataNotFoundException::new);

            data.setBoardData(boardData);

            if (memberUtil.isLogin()) {
                Member member = memberUtil.getMember();
                data.setEmail(member.getEmail());
                data.setUserName(member.getUserName());
            }

            data.setIp(request.getRemoteAddr());
            data.setUa(request.getHeader("User-Agent"));
        }

        // 비회원 비밀번호 O -> 해시화 -> 저장
        String guestPw = form.getGuestPw();
        if (StringUtils.hasText(guestPw)) {
            data.setGuestPw(encoder.encode(guestPw));
        }

        String commenter = form.getCommenter();
        if (StringUtils.hasText(commenter)) {
            data.setCommenter(commenter);
        }

        data.setContent(form.getContent());

        commentDataRepository.saveAndFlush(data);

        commentInfoService.updateCommentCount(data.getBoardData().getSeq());

        return data;
    }
}
