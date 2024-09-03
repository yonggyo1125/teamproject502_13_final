package org.choongang.file.services;

import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.controllers.RequestUpload;
import org.choongang.file.exceptions.FileTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BeforeFileUploadProcess {

    private final FileDeleteService deleteService;

    /**
     * 파일 업로드 전 처리
     *
     * @param form
     */
    public void process(RequestUpload form) {
        MultipartFile[] files = form.getFiles();

        // 업로드된 파일에서 이미지만 포함되어 있는지 체크
        if (form.isImageOnly()) {
            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                // 이미지이면 image/png, image/gif ..
                // 이미지가 아닌 파일이 포함된 경우
                if (!contentType.contains("image/")) {
                    throw new FileTypeException(HttpStatus.BAD_REQUEST);
                }
            }
        }

        /**
         * 단일 파일 업로드
         *  - 기존 파일을 gid + location 값을 가지고 삭제
         *
         */
        if (form.isSingle()) {
            deleteService.delete(form.getGid(), form.getLocation(), FileStatus.ALL);
            form.setFiles(new MultipartFile[] { files[0] });
        }
    }
}
