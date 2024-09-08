package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.choongang.board.services.WishListService;
import org.choongang.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Wish", description = "찜하기 API")
@RestController
@RequestMapping("/wish")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService service;

    @Operation(summary = "찜한 게시글 번호 목록 조회", method = "GET")
    @ApiResponse(responseCode = "200")
    @GetMapping("/list")
    public JSONData list() {
        List<Long> seqs = service.getList();

        return new JSONData(seqs);
    }

    @Operation(summary = "게시글 찜하기", method="GET")
    @ApiResponse(responseCode = "201")
    @Parameter(name="seq", required = true, description = "경로변수, 게시글 등록 번호")
    // 추가
    @GetMapping("/{seq}")
    public ResponseEntity<Void> add(@PathVariable("seq") Long seq) {

        service.add(seq);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(summary = "게시글 찜하기 해제", method="DELETE")
    @ApiResponse(responseCode = "200")
    @Parameter(name="seq", required = true, description = "경로변수, 게시글 등록 번호")
    // 추가
    @DeleteMapping("/{seq}")
    public ResponseEntity<Void> remove(@PathVariable("seq") Long seq) {
        service.remove(seq);

        return ResponseEntity.ok().build();
    }
}
