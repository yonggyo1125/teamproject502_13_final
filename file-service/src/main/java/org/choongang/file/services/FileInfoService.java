package org.choongang.file.services;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileSelect;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.entities.QFileInfo;
import org.choongang.file.exceptions.FileNotFoundException;
import org.choongang.file.repositories.FileInfoRepository;
import org.choongang.global.Utils;
import org.choongang.global.configs.FileProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Order.asc;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileInfoService {

    private final FileInfoRepository infoRepository;
    private final FileProperties properties;
    private final Utils utils;

    /**
     * 파일 1개 조회
     *
     * @param seq : 파일 등록번호
     * @return
     */
    public FileInfo get(Long seq) {
        FileInfo item = infoRepository.findById(seq).orElseThrow(FileNotFoundException::new);

        /**
         * 2차 가공
         * 1. 파일을 접근할 수 있는 URL - 보여주기 위한 목적
         * 2. 파일을 접근할 수 있는 PATH - 파일 삭제, 다운로드 등등
         */

        addFileInfo(item);

        return item;
    }

    /**
     * 파일 목록 조회
     *
     * @param gid
     * @param location
     * @param status - ALL: 완료 + 미완료, DONE - 완료, UNDONE - 미완료
     * @return
     */
    public List<FileInfo> getList(String gid, String location, FileSelect select, FileStatus status) {

        QFileInfo fileInfo = QFileInfo.fileInfo;
        BooleanBuilder andBuilder = new BooleanBuilder();
        andBuilder.and(fileInfo.gid.eq(gid));

        if (StringUtils.hasText(location)) {
            andBuilder.and(fileInfo.location.eq(location));
        }

        if (status != FileStatus.ALL) {
            andBuilder.and(fileInfo.done.eq(status == FileStatus.DONE));
        }

        /* 파일 선택여부 */
        if(select != FileSelect.ALL){
            andBuilder.and(fileInfo.selected.eq(select == FileSelect.SELECTED));
        }

        List<FileInfo> items = (List<FileInfo>)infoRepository.findAll(andBuilder, Sort.by(asc("createdAt")));

        // 2차 추가 데이터 처리
        items.forEach(this::addFileInfo);

        return items;
    }

    public List<FileInfo> getSelectedList(String gid, String location, FileStatus status){
        return getList(gid, location, FileSelect.SELECTED, status);
    }

    public List<FileInfo> getSelectedList(String gid, String location){
        return getSelectedList(gid, location ,FileStatus.DONE);
    }

    public List<FileInfo> getSelectedList(String gid){
        return getSelectedList(gid, null);
    }
    public  List<FileInfo> getSelectedList(String gid, String location, int cnt){
        List<FileInfo> items = getSelectedList(gid, location, FileStatus.DONE);

        if(cnt == 0){
            return items;
        }
        return items == null || items.isEmpty() ? null :items.stream().limit(cnt).toList();
    }


    public List<FileInfo> getList(String gid, String location, FileStatus status) {
        return getList(gid, location, FileSelect.ALL, status);
    }

    public List<FileInfo> getList(String gid, String location) {
        return getList(gid, location, FileStatus.DONE);
    }

    public List<FileInfo> getList(String gid) {
        return getList(gid, null, FileStatus.DONE);
    }

    /**
     * 파일 정보 추가 처리
     * - fileUrl, filePath
     *
     * @param item
     */
    public void addFileInfo(FileInfo item) {
        String fileUrl = getFileUrl(item);
        String filePath = getFilePath(item);

        item.setFileUrl(fileUrl);
        item.setFilePath(filePath);

        item.setThumbUrl(utils.url("/thumb", "file-service"));
    }

    // 브라우저 접근 주소
    public String getFileUrl(FileInfo item) {
        //return request.getContextPath() + properties.getUrl() + getFolder(item.getSeq()) + "/" + getFileName(item);

        String url =  properties.getUrl() + getFolder(item.getSeq()) + "/" + getFileName(item);

        return utils.url(url, "file-service");
    }

    // 서버 업로드 경로
    public String getFilePath(FileInfo item) {
        return properties.getPath() + "/" + getFolder(item.getSeq()) + "/" + getFileName(item);
    }

    public String getFolder(long seq) {
        return String.valueOf(seq % 10L);
    }

    public String getFileName(FileInfo item) {
        String fileName = item.getSeq() + Objects.requireNonNullElse(item.getExtension(), "");
        return fileName;
    }

    public List<FileInfo> getSelectedImages(String gid, String location) {
        List<FileInfo> items = getSelectedList(gid, location);
        items = Objects.requireNonNullElse(items, Collections.EMPTY_LIST);

        return items.stream()
                .filter(item -> item.getContentType().contains("image/"))
                .toList();
    }

    public List<FileInfo> getSelectedImages(String gid) {
        return getSelectedImages(gid, null);
    }
}
