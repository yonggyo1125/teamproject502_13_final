package org.choongang.file.services;

import org.choongang.file.constants.FileStatus;
import org.choongang.file.entities.FileInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class FileInfoServiceTest {

    @Autowired
    private FileInfoService infoService;

    @Test
    void test1() {
        FileInfo item = infoService.get(1L);
        System.out.println(item);
    }

    @Test
    void test2() {
        List<FileInfo> items = infoService.getList("testgid", null, FileStatus.ALL);
        items.forEach(System.out::println);
    }
}
