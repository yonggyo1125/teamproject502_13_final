package org.choongang.file.services;

import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.controllers.RequestSelect;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.repositories.FileInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FileSelectService {

    private final FileInfoService infoService;
    private final FileInfoRepository repository;

    public void process(String mode, List<Long> seqs, String gid, String location){
        List<FileInfo> items = infoService.getList(gid, location, FileStatus.ALL);
        items.forEach(item -> {
            if (seqs != null && !seqs.isEmpty() && seqs.contains(item.getSeq())) {
                item.setSelected(mode.equals("deselect") ? false : true);
            }
        });

        repository.saveAllAndFlush(items);
    }

    public void process(String mode, List<Long> seqs, String gid){
        process(mode, seqs, gid, null);
    }

    public void process(String mode, RequestSelect form){
        process(mode, form.getSeq(), form.getGid(), form.getLocation());
    }
}
