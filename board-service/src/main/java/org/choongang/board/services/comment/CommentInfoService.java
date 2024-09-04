package org.choongang.board.services.comment;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.board.controllers.RequestComment;
import org.choongang.board.entities.BoardData;
import org.choongang.board.entities.CommentData;
import org.choongang.board.entities.QCommentData;
import org.choongang.board.exceptions.CommentNotFoundException;
import org.choongang.board.repositories.BoardDataRepository;
import org.choongang.board.repositories.CommentDataRepository;
import org.choongang.global.services.SessionService;
import org.choongang.member.MemberUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.asc;

@Service
@RequiredArgsConstructor
public class CommentInfoService {

    private final CommentDataRepository commentDataRepository;
    private final BoardDataRepository boardDataRepository;
    private final MemberUtil memberUtil;
    private final HttpServletRequest request;
    private final SessionService sessionService;

    /**
     * 댓글 단일 조회
     *
     * @param seq : 댓글 번호
     * @return
     */
    public CommentData get(Long seq) {
        CommentData data = commentDataRepository.findById(seq).orElseThrow(CommentNotFoundException::new);

        addCommentInfo(data);

        return data;
    }

    public RequestComment getForm(Long seq) {
        CommentData data = get(seq);
        RequestComment form = new ModelMapper().map(data, RequestComment.class);

        form.setBoardDataSeq(data.getBoardData().getSeq());

        return form;
    }

    /**
     * 게시글별 댓글 목록 조회
     *
     * @param boardDataSeq
     * @return
     */
    public List<CommentData> getList(Long boardDataSeq) {
        QCommentData commentData = QCommentData.commentData;
        BooleanBuilder andBuilder = new BooleanBuilder();
        andBuilder.and(commentData.boardData.seq.eq(boardDataSeq));

        List<CommentData> items = (List<CommentData>)commentDataRepository.findAll(andBuilder, Sort.by(asc("createdAt")));

        items.forEach(this::addCommentInfo);

        return items;
    }


    /**
     * 댓글 추가 정보 처리
     *
     * @param data
     */
    private void addCommentInfo(CommentData data) {
        boolean editable = false, mine = false;

        String memberEmail = data.getEmail(); // 댓글을 작성한 회원

        /*
         * 1) 관리자는 댓글 수정, 삭제 제한 없음
         *
         */
        if (memberUtil.isAdmin()) {
            editable = true;
        }

        /**
         * 회원이 작성한 댓글이면 현재 로그인 사용자의 아이디와 동일해야 수정, 삭제 가능
         *
         */
        if (memberEmail != null && memberUtil.isLogin()
                && memberEmail.equals(memberUtil.getMember().getEmail())) {
            editable =  mine = true;
        }

        // 비회원 -> 비회원 비밀번호가 확인 된 경우 삭제, 수정 가능
        // 비회원 비밀번호 인증 여부 세션에 있는 guest_confirmed_게시글번호 true -> 인증
        String key = "confirm_comment_data_" + data.getSeq();
        String guestConfirmed = sessionService.get(key);
        if (memberEmail == null && guestConfirmed != null && guestConfirmed.equals("true")) {
            editable = true;
            mine = true;
        }

        // 수정 버튼 노출 여부
        // 관리자 - 노출, 회원 게시글 - 직접 작성한 게시글, 비회원
        boolean showEdit = memberUtil.isAdmin() || mine || memberEmail == null;

        data.setShowEdit(showEdit);

        data.setEditable(editable);
        data.setMine(mine);
    }

    /**
     * 게시글별 댓글 수 업데이트
     *
     * @param boardDataSeq : 게시글 번호
     */
    public void updateCommentCount(Long boardDataSeq) {
        BoardData data = boardDataRepository.findById(boardDataSeq).orElse(null);
        if (data == null) {
            return;
        }

        int total = commentDataRepository.getTotal(boardDataSeq);

        data.setCommentCount(total);

        boardDataRepository.flush();

    }
}
