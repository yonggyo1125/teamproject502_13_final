package org.choongang.file.services;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.choongang.file.entities.FileInfo;
import org.choongang.global.rests.ApiRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileDeleteService {
    private final ApiRequest apiRequest;

    public FileInfo delete(Long seq) {
        ApiRequest result = apiRequest.request("/delete/" + seq, "file-service", HttpMethod.DELETE);

        if (result.getStatus().is2xxSuccessful() && result.getData().isSuccess()) {
            return result.toJSON(FileInfo.class);
        }

        return null;
    }


    public List<FileInfo> delete(String gid, String location) {
        String url = "/deletes/" + gid;
        if (StringUtils.hasText(location)) url += "&location=" + location;

        ApiRequest result = apiRequest.request(url, "file-service", HttpMethod.DELETE);

        if (result.getStatus().is2xxSuccessful() && result.getData().isSuccess()) {
            return result.toJSON(new TypeReference<>() {});
        }

        return null;
    }

    public List<FileInfo> delete(String gid) {
        return delete(gid, null);
    }

}
