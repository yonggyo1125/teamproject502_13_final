package org.choongang.board.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

@Data
@Entity
@IdClass(WishListId.class)
public class WishList {
    @Id
    private Long seq; // 게시글 번호

    @Id
    @Column(length=80)
    private String email; // 회원 이메일 주소
}
