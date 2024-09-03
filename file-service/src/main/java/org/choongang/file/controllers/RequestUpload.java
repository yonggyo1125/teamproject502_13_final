package org.choongang.file.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestUpload {
    @NotBlank
    private String gid;
    private String location;
    private boolean imageOnly; // 이미지만 업로드
    private boolean single; // 단일 파일 업로드
    private boolean done; // true -> 업로드 하자마자 그룹 작업 완료 처리

    private MultipartFile[] files; // 파일 검증을 위해서 추가
}
