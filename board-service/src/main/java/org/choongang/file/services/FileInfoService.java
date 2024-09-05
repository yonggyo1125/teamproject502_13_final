package org.choongang.file.services;

import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.entities.FileInfo;
import org.choongang.global.rests.ApiRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileInfoService {

    private final ApiRequest apiRequest;

    /**
     * 파일 1개 조회
     *
     * @param seq : 파일 등록 번호
     * @return
     */
    public FileInfo get(Long seq) {
        ApiRequest result = apiRequest.request("/info/" + seq, "file-service");

        if (result.getResponse().getStatusCode().is2xxSuccessful()) {
            return result.toJSON(FileInfo.class);
        }

        return null;
    }

    public List<FileInfo> getList(String gid, String location, FileStatus status) {

        return null;
    }

    public List<FileInfo> getList(String gid, String location) {
        return getList(gid, location, FileStatus.DONE);
    }

    public List<FileInfo> getList(String gid) {
        return getList(gid, null, FileStatus.DONE);
    }

}
