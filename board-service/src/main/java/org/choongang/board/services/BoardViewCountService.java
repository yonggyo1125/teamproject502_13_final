package org.choongang.board.services;

import lombok.RequiredArgsConstructor;
import org.choongang.board.entities.BoardData;
import org.choongang.board.entities.BoardView;
import org.choongang.board.entities.QBoardView;
import org.choongang.board.repositories.BoardDataRepository;
import org.choongang.board.repositories.BoardViewRepository;
import org.choongang.global.Utils;
import org.choongang.member.MemberUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardViewCountService {
    private final BoardViewRepository viewRepository;
    private final BoardDataRepository dataRepository;
    private final MemberUtil memberUtil;
    private final Utils utils;

    public void update(Long seq) {
        BoardData data = dataRepository.findById(seq).orElse(null);
        if (data == null) {
            return;
        }

        int uid = memberUtil.isLogin() ? memberUtil.getMember().getSeq().intValue() : utils.guestUid();

        BoardView boardView = new BoardView(seq, uid);
        viewRepository.saveAndFlush(boardView);

        // 전체 조회수
        QBoardView bv = QBoardView.boardView;
        long total = viewRepository.count(bv.seq.eq(seq));

        data.setViewCount((int)total);
        dataRepository.saveAndFlush(data);
    }
}
