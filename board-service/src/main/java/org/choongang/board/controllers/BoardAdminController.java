package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.board.services.config.BoardConfigSaveService;
import org.choongang.board.validators.BoardConfigValidator;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="BoardAdmin", description = "게시판 관리")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BoardAdminController {

    private final BoardConfigSaveService configSaveService;
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

        configSaveService.save(form);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        configSaveService.save(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
