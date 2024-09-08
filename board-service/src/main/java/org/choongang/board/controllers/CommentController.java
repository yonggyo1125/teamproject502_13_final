package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.board.entities.CommentData;
import org.choongang.board.services.comment.CommentDeleteService;
import org.choongang.board.services.comment.CommentInfoService;
import org.choongang.board.services.comment.CommentSaveService;
import org.choongang.board.validators.CommentValidator;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.choongang.global.rests.JSONData;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Comment", description = "댓글 API")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentInfoService infoService;
    private final CommentSaveService saveService;
    private final CommentDeleteService deleteService;
    private final CommentValidator validator;
    private final Utils utils;

    @Operation(summary = "댓글 작성", method = "POST")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="mode", required = true, description = "write로 고정", example = "write"),
            @Parameter(name="boardDataSeq", required = true, description = "게시글 등록 번호"),
            @Parameter(name="commenter", required = true, description = "작성자", example = "작성자01"),
            @Parameter(name="content", required = true, description = "댓글 내용")
    })
    @PostMapping
    public JSONData write(@RequestBody @Valid RequestComment form, Errors errors) {
        return save(form, errors);
    }

    @Operation(summary = "댓글 수정", method = "PATCH")
    @ApiResponse(responseCode = "200")
    @Parameters({
            @Parameter(name="mode", required = true, description = "update로 고정", example = "update"),
            @Parameter(name="seq", required = true, description = "댓글 등록번호", example = "100"),
            @Parameter(name="commenter", required = true, description = "작성자", example = "작성자01"),
            @Parameter(name="content", required = true, description = "댓글 내용")
    })   @PatchMapping
    public JSONData update(@RequestBody @Valid RequestComment form, Errors errors) {
        return save(form, errors);
    }

    public JSONData save(RequestComment form, Errors errors) {
        validator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        saveService.save(form);

        List<CommentData> items = infoService.getList(form.getBoardDataSeq());

        return new JSONData(items);
    }

    @Operation(summary = "댓글 하나 조회", description = "댓글 번호를 가지고 작성된 댓글을 조회 한다.", method = "GET")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 댓글 등록 번호")
    @GetMapping("/info/{seq}")
    public JSONData getInfo(@PathVariable("seq") Long seq) {
        CommentData item = infoService.get(seq);

        return new JSONData(item);
    }

    @Operation(summary = "댓글 조회", description = "게시글 번호를 가지고 작성된 댓글 목록을 조회 한다.", method = "GET")
    @ApiResponse(responseCode = "200")
    @Parameter(name="bSeq", required = true, description = "경로변수, 게시글 번호")
    @GetMapping("/list/{bSeq}")
    public JSONData getList(@PathVariable("bSeq") Long bSeq) {
        List<CommentData> items = infoService.getList(bSeq);

        return new JSONData(items);
    }

    @Operation(summary = "댓글 하나 삭제", description = "댓글 번호를 가지고 작성된 댓글을 삭제한다.", method = "DELETE")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 댓글 등록 번호")
    @DeleteMapping("/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        Long bSeq = deleteService.delete(seq);

        List<CommentData> items = infoService.getList(bSeq);

        return new JSONData(items);
    }
}
