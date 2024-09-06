package org.choongang.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.global.ListData;
import org.choongang.global.Utils;
import org.choongang.global.exceptions.BadRequestException;
import org.choongang.global.rests.JSONData;
import org.choongang.member.MemberInfo;
import org.choongang.member.entities.Member;
import org.choongang.member.jwt.TokenProvider;
import org.choongang.member.services.MemberInfoService;
import org.choongang.member.services.MemberSaveService;
import org.choongang.member.validators.JoinValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 인증 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final JoinValidator joinValidator;
    private final MemberSaveService saveService;
    private final MemberInfoService infoService;
    private final TokenProvider tokenProvider;
    private final Utils utils;

    @Operation(summary = "인증(로그인)한 회원 정보 조회")
    @ApiResponse(responseCode = "200")
    // 로그인한 회원 정보 조회
    @GetMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {
        Member member = memberInfo.getMember();

        return new JSONData(member);
    }

    @Operation(summary = "회원가입")
    @ApiResponse(responseCode = "201", description = "회원가입 성공시 201")
    @Parameters({
            @Parameter(name="email", required = true, description = "이메일"),
            @Parameter(name="password", required = true, description = "비밀번호"),
            @Parameter(name="confirmPassword", required = true, description = "비밀번호 확인"),
            @Parameter(name="userName", required = true, description = "사용자명"),
            @Parameter(name="mobile", description = "휴대전화번호, 형식 검증 있음"),
            @Parameter(name="agree", required = true, description = "회원가입약관 동의")
    })
    @PostMapping("/account")
    public ResponseEntity join(@RequestBody @Valid RequestJoin form, Errors errors) {

        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        saveService.save(form);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "인증 및 토큰 발급", description = "인증 성공시 JWT 토큰 발급")
    @ApiResponse(responseCode = "201", headers = @Header(name="application/json"), description = "data이 발급 받은 토큰")

    @Parameters({
        @Parameter(name="email", required = true, description = "이메일"),
            @Parameter(name="password", required = true, description = "비밀번호")
    })
    @PostMapping("/account/token")
    public JSONData token(@RequestBody @Valid RequestLogin form, Errors errors) {

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        String token = tokenProvider.createToken(form.getEmail(), form.getPassword());

        return new JSONData(token);
    }

    @Operation(summary = "회원 조회", description = "학생, 교수/상담자, 관리자에 따라 개인정보 조회 범위가 다르다")
    @ApiResponse(responseCode = "200", description = "검색된 학생 목록")
    @Parameters({
            @Parameter(name="page", example = "1", description = "페이지 번호"),
            @Parameter(name="limit", example = "20", description = "페이지당 레코드 갯수"),
    })
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'COUNSELOR', 'PROFESSOR')")
    public JSONData list(@ModelAttribute MemberSearch search) {

        ListData<Member> data = infoService.getList(search);

        return new JSONData(data);
    }
}
