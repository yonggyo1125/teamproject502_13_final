package org.choongang.board.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.choongang.global.entities.BaseMemberEntity;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(name="idx_comment_basic", columnList = "createdAt ASC"))
public class CommentData extends BaseMemberEntity {
    @Id @GeneratedValue
    private Long seq;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private BoardData boardData;

    @Column(length=80)
    private String email; // 로그인한 회원 이메일 주소

    @Column(length=40)
    private String userName; // 로그인한 회원명

    @Column(length=40, nullable = false)
    private String commenter; // 작성자

    @Column(length=65)
    private String guestPw; // 비회원 댓글 수정, 삭제 비밀번호

    @Lob
    @Column(nullable = false)
    private String content; // 댓글 내용

    @Column(length=20)
    private String ip; // 작성자 IP 주소

    @Column(length=150)
    private String ua; // 작성자 User-Agent 정보

    @Transient
    private boolean editable; // 수정, 삭제 가능 여부

    @Transient
    private boolean mine; // 소유자

    @Transient
    private boolean showEdit; // 수정, 삭제 버튼 노출 여부
}
