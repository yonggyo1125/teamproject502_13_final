package org.choongang.file.services;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.entities.FileInfo;
import org.choongang.global.rests.ApiRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        if (result.getStatus().is2xxSuccessful() && result.getData().isSuccess()) {
            return result.toJSON(FileInfo.class);
        }

        return null;
    }

    /**
     * 파일 목록 조회
     *
     * @param gid
     * @param location
     * @param status
     * @return
     */
    public List<FileInfo> getList(String gid, String location, FileStatus status) {
        String url = "/list/" + gid;
        if (StringUtils.hasText(location)) url += "&location=" + location;
        if (status != null) url += "&status=" + status.name();

        ApiRequest result = apiRequest.request(url, "file-service");
        if (result.getStatus().is2xxSuccessful() && result.getData().isSuccess()) {
            return result.toJSON(new TypeReference<>(){});
        }

        return null;
    }

    public List<FileInfo> getList(String gid, String location) {
        return getList(gid, location, FileStatus.DONE);
    }

    public List<FileInfo> getList(String gid) {
        return getList(gid, null, FileStatus.DONE);
    }
}
