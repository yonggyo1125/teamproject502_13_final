package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.board.entities.Board;
import org.choongang.board.entities.BoardData;
import org.choongang.board.services.BoardDeleteService;
import org.choongang.board.services.BoardInfoService;
import org.choongang.board.services.BoardSaveService;
import org.choongang.board.services.BoardViewCountService;
import org.choongang.board.services.config.BoardConfigInfoService;
import org.choongang.board.validators.BoardValidator;
import org.choongang.global.CommonSearch;
import org.choongang.global.ListData;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Board", description = "게시글 API")
@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardConfigInfoService configInfoService;
    private final BoardInfoService infoService;
    private final BoardSaveService saveService;
    private final BoardDeleteService deleteService;
    private final BoardViewCountService viewCountService;
    private final BoardValidator validator;
    private final Utils utils;


    @Operation(summary = "게시판 설정 조회", method = "GET")
    @ApiResponse(responseCode = "200", description = "게시판 ID(bid)로 설정 조회")
    @Parameter(name="bid", required = true, description = "경로변수, 게시판 ID(bid)", example = "notice")
    // 게시판 설정
    @GetMapping("/config/{bid}")
    public JSONData getConfig(@PathVariable("bid") String bid) {

        Board board = configInfoService.get(bid);

        return new JSONData(board);
    }

    @Operation(summary = "게시글 작성", method = "POST")
    @ApiResponse(responseCode = "201")
    @Parameters({
            @Parameter(name="mode", required = true, description = "write로 고정", example="write"),
            @Parameter(name="bid", required = true, description = "게시판 ID", example = "notice"),
            @Parameter(name="gid", required = true, description = "파일 업로드를 위한 그룹 ID(gid)"),
            @Parameter(name="notice", description = "공지글 여부", example = "false"),
            @Parameter(name="poster", description = "작성자", required = true, example = "작성자01"),
            @Parameter(name="guestPw", description = "비회원 비밀번호, 비회원으로 작성할 경우 필수"),
            @Parameter(name="subject", required = true, description = "글 제목"),
            @Parameter(name="content", required = true, description = "글 내용"),
            @Parameter(name="num1", description = "정수 추가 필드1"),
            @Parameter(name="num2", description = "정수 추가 필드2"),
            @Parameter(name="num3", description = "정수 추가 필드3"),
            @Parameter(name="text1", description = "한줄 텍스트 추가 필드1"),
            @Parameter(name="text2", description = "한줄 텍스트 추가 필드2"),
            @Parameter(name="text3", description = "한줄 텍스트 추가 필드3"),
            @Parameter(name="longText1", description = "여러줄 텍스트 추가 필드1"),
            @Parameter(name="longText2", description = "여러줄 텍스트 추가 필드2"),
            @Parameter(name="longText3", description = "여러줄 텍스트 추가 필드3"),
    })
    // 글쓰기
    @PostMapping("/write/{bid}")
    public ResponseEntity<JSONData> write(@PathVariable("bid") String bid, @RequestBody @Valid RequestBoard form, Errors errors) {
        form.setBid(bid);
        form.setMode("write");

        return save(form, errors);
    }


    @Operation(summary = "게시글 수정", method = "POST")
    @ApiResponse(responseCode = "201")
    @Parameters({
            @Parameter(name="seq", required = true, description = "경로변수, 게시글 등록 번호", example="100"),
            @Parameter(name="mode", required = true, description = "update로 고정", example="update"),
            @Parameter(name="bid", required = true, description = "게시판 ID", example = "notice"),
            @Parameter(name="gid", required = true, description = "파일 업로드를 위한 그룹 ID(gid)"),
            @Parameter(name="notice", description = "공지글 여부", example = "false"),
            @Parameter(name="poster", description = "작성자", required = true, example = "작성자01"),
            @Parameter(name="guestPw", description = "비회원 비밀번호, 비회원으로 작성할 경우 필수"),
            @Parameter(name="subject", required = true, description = "글 제목"),
            @Parameter(name="content", required = true, description = "글 내용"),
            @Parameter(name="num1", description = "정수 추가 필드1"),
            @Parameter(name="num2", description = "정수 추가 필드2"),
            @Parameter(name="num3", description = "정수 추가 필드3"),
            @Parameter(name="text1", description = "한줄 텍스트 추가 필드1"),
            @Parameter(name="text2", description = "한줄 텍스트 추가 필드2"),
            @Parameter(name="text3", description = "한줄 텍스트 추가 필드3"),
            @Parameter(name="longText1", description = "여러줄 텍스트 추가 필드1"),
            @Parameter(name="longText2", description = "여러줄 텍스트 추가 필드2"),
            @Parameter(name="longText3", description = "여러줄 텍스트 추가 필드3"),
    })
    // 글 수정
    @PatchMapping("/update/{seq}")
    public ResponseEntity<JSONData> update(@PathVariable("seq") Long seq, @RequestBody @Valid RequestBoard form, Errors errors) {
        form.setSeq(seq);
        form.setMode("update");

        return save(form, errors);
    }

    // 글 작성, 수정 처리
    private ResponseEntity<JSONData> save(RequestBoard form, Errors errors) {

        validator.validate(form, errors);

        if (errors.hasErrors()) { // 검증 실패
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        BoardData data = saveService.save(form);
        data.setBoard(null);
        data.setComments(null);

        JSONData jsonData = new JSONData(data);
        HttpStatus status = HttpStatus.CREATED;
        jsonData.setStatus(status);

        return ResponseEntity.status(status).body(jsonData);
    }

    @Operation(summary = "게시글 하나 조회", method = "GET")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 게시글 등록 번호", example = "100")
    @GetMapping("/info/{seq}")
    public JSONData info(@PathVariable("seq") Long seq) {
        BoardData item = infoService.get(seq);

        viewCountService.update(seq); // 조회수 카운트

        return new JSONData(item);
    }

    @Operation(summary = "게시글 목록", method = "GET")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="bid", required = true, description = "경로변수, 게시판 ID", example = "notice"),
            @Parameter(name="page", description = "페이지 번호", example = "1"),
            @Parameter(name="limit", description = "한페이지당 레코드 갯수", example = "20"),
            @Parameter(name="sopt", description = "검색옵션<br>ALL: 통합검색<br>SUBJECT: 제목검색<br>CONTENT: 내용검색<br>SUBJECT_CONTENT: 제목+내용 검색<br>NAME: 작성자, 회원명 검색", example = "ALL"),
            @Parameter(name="skey", description = "검색키워드"),
            @Parameter(name="bids", description = "게시판 ID 목록"),
            @Parameter(name="sort", description = "게시판 정렬 조건", example = "viewCount_DESC")
    })
    @GetMapping("/list/{bid}")
    public JSONData list(@PathVariable("bid") String bid, @ModelAttribute BoardDataSearch search) {
        ListData<BoardData> data = infoService.getList(bid, search);

        return new JSONData(data);
    }

    @Operation(summary = "게시글 삭제")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 게시글 등록번호", example = "100")
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        BoardData item = deleteService.delete(seq);

        return new JSONData(item);
    }

    @Operation(summary = "찜한 게시글 목록 조회")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="page", description = "페이지 번호", example = "1"),
            @Parameter(name="limit", description = "한페이지당 레코드 갯수", example = "20"),
            @Parameter(name="sopt", description = "검색옵션<br>ALL: 통합검색<br>SUBJECT: 제목검색<br>CONTENT: 내용검색<br>SUBJECT_CONTENT: 제목+내용 검색<br>NAME: 작성자, 회원명 검색", example = "ALL"),
            @Parameter(name="skey", description = "검색키워드")
    })
    @GetMapping("/wish")
    @PreAuthorize("isAuthenticated()")
    public JSONData wishList(CommonSearch search) {
        ListData<BoardData> data = infoService.getWishList(search);

        return new JSONData(data);
    }
}
