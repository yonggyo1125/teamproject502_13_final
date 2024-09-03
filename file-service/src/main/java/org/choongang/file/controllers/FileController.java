package org.choongang.file.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.file.constants.FileStatus;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.services.*;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Tag(name="File", description = "파일 API")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService uploadService;
    private final FileDownloadService downloadService;
    private final FileInfoService infoService;
    private final FileDeleteService deleteService;
    private final BeforeFileUploadProcess beforeProcess;
    private final AfterFileUploadProcess afterProcess;
    private final Utils utils;
    private final ThumbnailService thumbnailService;
    private final FileSelectService selectService;

    @Operation(summary = "파일 업로드 처리")
    @ApiResponse(responseCode = "201", description = "파일 업로드 성공시에는 업로드한 파일 목록이 반환됩니다.", headers= @Header(name="Content-Type: multipart/form-data"))
    @Parameters({
            @Parameter(name="file", required = true,  description = "업로드할 파일 목록"),
            @Parameter(name="gid", required = true, description = "그룹 ID"),
            @Parameter(name="location", example="editor", description = "파일 구분 위치"),
            @Parameter(name="imageOnly", example="true", description = "이미지만 업로드 허용 여부"),
            @Parameter(name="single", example="true", description = "단일 파일 업로드 여부"),
            @Parameter(name="done", example="true", description = "업로드 하자마자 그룹 작업 완료 처리")
    })
    @PostMapping("/upload")
    public ResponseEntity<JSONData> upload(@RequestPart("file") MultipartFile[] files,
                                           @Valid RequestUpload form, Errors errors) {
        //ResponseEntity: HTTP 응답의 상태 코드, 헤더, 본문을 포함하는 객체입니다.
        //JSONData: 반환할 JSON 형식의 데이터 구조를 나타내는 클래스입니다. 이 클래스는 서버에서 처리한 결과를 JSON 형식으로 클라이언트에게 응답할 때 사용됩니다.

        form.setFiles(files);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        beforeProcess.process(form); // 파일 업로드 전처리

        List<FileInfo> items = uploadService.upload(files, form.getGid(), form.getLocation());

        afterProcess.process(form); // 파일 업로드 후처리

        HttpStatus status = HttpStatus.CREATED;
        JSONData data = new JSONData(items);
        data.setStatus(status);

        return ResponseEntity.status(status).body(data);
    }

    @Operation(summary = "파일 다운로드")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 파일 등록번호")

    @GetMapping("/download/{seq}")
    public void download(@PathVariable("seq") Long seq) {
        downloadService.download(seq);
    }


    @Operation(summary = "파일 한개 삭제")
    @ApiResponse(responseCode = "200", description = "파일 삭제 완료 후 삭제된 파일 정보 반환")
    @Parameter(name="seq", required = true, description = "경로변수, 파일 등록번호")
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        FileInfo data = deleteService.delete(seq);

        return new JSONData(data);
    }

    @Operation(summary = "파일 여러개 삭제 - gid(그룹ID), location")
    @ApiResponse(responseCode = "200", description = "삭제 완료된 파일 목록 반환")
    @Parameters({
            @Parameter(name="gid", required = true, description = "경로변수, 그룹 ID"),
            @Parameter(name="location", description = "파일 구분 위치")
    })
    @DeleteMapping("/deletes/{gid}")
    public JSONData deletes(@PathVariable("gid") String gid, @RequestParam(name="location", required = false) String location) {
        List<FileInfo> items = deleteService.delete(gid, location);

        return new JSONData(items);
    }

    @Operation(summary = "파일 정보 하나 조회")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 파일 등록번호")
    @GetMapping("/info/{seq}")
    public JSONData get(@PathVariable("seq") Long seq) {
        FileInfo data = infoService.get(seq);

        return new JSONData(data);
    }

    @Operation(summary = "파일 목록 조회 - gid, location")
    @ApiResponse(responseCode = "200", description = "그룹 ID(gid)와 파일 구분 위치(location)으로 파일 목록 조회, location은 gid에 종속되는 검색 조건")
    @Parameters({
            @Parameter(name="gid", required = true, description = "경로변수, 그룹 ID"),
            @Parameter(name="location", description = "파일 구분 위치")
    })
    @GetMapping("/list/{gid}")
    public JSONData getList(@PathVariable("gid") String gid, @RequestParam(name="location", required = false) String location) {
        List<FileInfo> items = infoService.getList(gid, location);

        return new JSONData(items);
    }

    @Operation(summary = "썸네일 생성")
    @ApiResponse(responseCode = "200", description = "생성된 이미지 출력")
    @Parameters({
            @Parameter(name="seq", required = true, description = "파일 등록번호 - seq, url 둘중 하나는 필수"),
            @Parameter(name="url", required = true, description = "파일 URL - seq, url 둘중 하나는 필수"),
            @Parameter(name="width", description = "생성될 이미지 너비, 값이 없다면 10px로 지정됨"),
            @Parameter(name="height", description = "생성될 이미지 높이, 값이 없다면 10px로 지정됨")
    })
    @GetMapping("/thumb")
    public void thumb(RequestThumb form, HttpServletResponse response) {
        String path = thumbnailService.create(form);
        if (!StringUtils.hasText(path)) {
            return;
        }

        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            String contentType = Files.probeContentType(file.toPath());
            response.setHeader("Content-Type", contentType);
            OutputStream out = response.getOutputStream(); // 출력
            out.write(bis.readAllBytes()); // 화면에 바로 출력

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Operation(summary = "파일 선택, 미선택 처리", description = "갤러리 게시판 목록 출력과 같이 여러 이미지 중 하나를 선택하여 출력할 경우 사용")
    @ApiResponse(responseCode = "200", description = "선택, 미선택된 파일 목록 반환")
    @Parameters({
            @Parameter(name="mode", required = true, description = "경로변수, select, deselect 두 값이며 select면 선택, deselect이면 미선택 상태로 변경"),
            @Parameter(name="gid", required = true, description = "그룹 ID"),
            @Parameter(name="location", description = "파일 위치"),
            @Parameter(name="seq", required = true, description = "파일 등록번호"),
            @Parameter(name="cnt", description = "선택된 파일 갯수 제한")
    })
    @PatchMapping("/select/{mode}")
    public JSONData fileSelect(@PathVariable("mode") String mode, @Valid @RequestBody RequestSelect form, Errors errors){

        if(errors.hasErrors()){
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
        selectService.process(mode, form);
        List<FileInfo> items = infoService.getSelectedList(form.getGid(), form.getLocation(), FileStatus.ALL);

        if (form.getCnt() > 0 && items != null && !items.isEmpty()) {
            items = items.stream().limit(form.getCnt()).toList();
        }

        return new JSONData(Objects.requireNonNullElse(items, Collections.EMPTY_LIST));
    }
}
