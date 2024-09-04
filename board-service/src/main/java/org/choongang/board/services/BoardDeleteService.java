package org.choongang.board.services;

import lombok.RequiredArgsConstructor;
import org.choongang.board.constants.DeleteStatus;
import org.choongang.board.entities.BoardData;
import org.choongang.board.repositories.BoardDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BoardDeleteService {
    private final BoardInfoService infoService;
    private final BoardDataRepository repository;
    //private final FileDeleteService deleteService;

    /**
     * SOFT 삭제, 삭제 시간 업데이트
     *
     * @param seq
     * @return
     */
    public BoardData delete(Long seq) {
        BoardData data = infoService.get(seq);
        data.setDeletedAt(LocalDateTime.now());

        repository.saveAndFlush(data);

        return data;
    }

    @Transactional
    public BoardData complete(Long seq) {
        BoardData data = infoService.get(seq, DeleteStatus.ALL);

        String gid = data.getGid();

        // 파일 삭제
        //deleteService.delete(gid);

        // 게시글 삭제
        repository.delete(data);
        repository.flush();

        return data;
    }

    /**
     * 게시글 복구
     *  - 삭제 일시 -> null
     * @param seq
     * @return
     */
    public BoardData recover(Long seq) {
        BoardData item = infoService.get(seq);
        item.setDeletedAt(null);

        repository.saveAndFlush(item);

        return item;
    }
}
