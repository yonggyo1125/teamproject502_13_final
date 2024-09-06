package org.choongang.board.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
public class BoardAdminController {

    @Operation(summary = "게시판 등록")
    @ApiResponse(responseCode = "201", description = "게시판 등록 성공시 201")
    @RequestMapping(method={RequestMethod.POST, RequestMethod.PATCH}, path="/save")
    public ResponseEntity<Void> save(@RequestBody @Valid RequestBoardConfig form, Errors errors) {



        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
