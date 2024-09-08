package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.board.entities.Board;
import org.choongang.board.services.config.BoardConfigInfoService;
import org.choongang.board.services.config.BoardConfigSaveService;
import org.choongang.board.validators.BoardConfigValidator;
import org.choongang.global.ListData;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name="BoardAdmin", description = "게시판 관리")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BoardAdminController {

    private final BoardConfigSaveService configSaveService;
    private final BoardConfigInfoService configInfoService;
    private final BoardConfigValidator configValidator;
    private final HttpServletRequest request;
    private final Utils utils;

    @Operation(summary = "게시판 등록")
    @ApiResponse(responseCode = "201", description = "게시판 등록 성공시 201")
    @Parameters({
            @Parameter(name="mode", required = true, description = "add - 등록, edit - 수정"),
            @Parameter(name="gid", required = true, description = "그룹 ID, 게시판 상단, 하단 이미지 관련"),
            @Parameter(name="listOrder", description = "진열 가중치, 수치가 높을 수록 먼저 게시판 노출"),
            @Parameter(name="bid", required = true, description = "게시판 아이디"),
            @Parameter(name="bName", required = true, description = "게시판 이름"),
            @Parameter(name="active", example = "false", description = "사용여부")
    })
    @RequestMapping(method={RequestMethod.POST, RequestMethod.PATCH}, path="/save")
    public ResponseEntity<Void> save(@RequestBody @Valid RequestBoardConfig form, Errors errors) {

        String method = request.getMethod().toUpperCase();
        String mode = method.equals("POST") ? "add" : "edit";
        form.setMode(mode);

        configValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        configSaveService.save(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "게시판 설정 하나 조회", description = "설정 조회는 게시판 아이디(bid) 기준 입니다.")
    @ApiResponse(responseCode = "200")
    @Parameter(name="bid", required = true, description = "경로변수", example = "notice")
    @GetMapping("/info/{bid}")
    public JSONData info(@PathVariable("bid") String bid) {
        Board board = configInfoService.get(bid);

        return new JSONData(board);
    }

    @Operation(summary = "게시판 목록 조회")
    @ApiResponse(responseCode = "200", description = "items - 조회된 게시글 목록, pagination - 페이징 기초 데이터")
    @Parameters({
            @Parameter(name="page", description = "페이지 번호", example = "1"),
            @Parameter(name="limit", description = "한페이지당 레코드 갯수", example = "20"),
            @Parameter(name="sopt", description = "검색옵션", example = "ALL"),
            @Parameter(name="skey", description = "검색키워드"),
            @Parameter(name="bid", description = "게시판 ID"),
            @Parameter(name="bids", description = "게시판 ID 목록"),
            @Parameter(name="bName", description = "게시판 이름"),
            @Parameter(name="active", description = "게시판 사용중 여부", example = "true")
    })
    public JSONData list(@ModelAttribute BoardSearch search) {

        ListData data = configInfoService.getList(search, true);

        return new JSONData(data);
    }
}
