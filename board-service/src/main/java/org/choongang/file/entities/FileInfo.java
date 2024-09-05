package org.choongang.file.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {
    private Long seq; // 서버에 업로드될 파일 이름  - seq.확장자

    private String gid;

    private String location; // 그룹 안에 세부 위치

    private String fileName;

    private String extension; // 파일 확장자

    private String contentType;

    private boolean done; // 그룹 작업 완료 여부

    private String fileUrl; // 파일 접근 URL

    private String filePath; // 파일 업로드 경로
}
