package org.choongang.file.controllers;

import lombok.Data;

@Data
public class RequestThumb {
    private Long seq; // 파일 등록번호 // 1순위
    private String url; // 원격 파일 url // 2순위
    private Integer width; // 너비
    private Integer height; // 높이
}
