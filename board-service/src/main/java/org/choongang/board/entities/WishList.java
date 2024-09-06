package org.choongang.board.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.choongang.global.entities.BaseMemberEntity;

@Data
@Entity
public class WishList extends BaseMemberEntity {
    @Id
    private Long seq; // 게시글 번호
}
