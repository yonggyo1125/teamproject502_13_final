package org.choongang.member.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.choongang.global.exceptions.UnAuthorizedException;
import org.choongang.member.MemberInfo;
import org.choongang.member.services.MemberInfoService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class TokenProvider {

    private final JwtProperties properties;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberInfoService infoService;

    /**
     * JWT 토큰 생성
     *
     * @param email
     * @param password
     * @return
     */
    public String createToken(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) { // 로그인 성공시 -> JWT 토큰 발급
            long now = (new Date()).getTime();
            Date validity = new Date(now + properties.getValidSeconds() * 1000);

            return Jwts.builder()
                    .setSubject(authentication.getName()) // 사용자 email
                    .signWith(getKey(), SignatureAlgorithm.HS512) // HMAC + SHA512
                    .expiration(validity)
                    .compact();
        }

        return null;
    }

    /**
     * 토큰으로 회원 인증 객체 조회
     *
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {
        // 토큰 검증
        validateToken(token);

        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getPayload();

        String email = claims.getSubject();

        MemberInfo memberInfo = (MemberInfo)infoService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(memberInfo, token, memberInfo.getAuthorities());
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token
     */
    public void validateToken(String token) {
        String errorCode = null;

        try {
            Jwts.parser().setSigningKey(getKey()).build().parseClaimsJws(token).getPayload();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 변조된 JWT 토큰
            errorCode = "Malformed.jwt";
            e.printStackTrace();

        } catch (ExpiredJwtException e) {
            // 유효시간이 만료된 JWT 토큰
            errorCode = "Expired.jwt";
            e.printStackTrace();

        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 형식의 JWT 토큰
            errorCode = "Unsupported.jwt";
            e.printStackTrace();
        } catch (Exception e) {
            errorCode = "Error.jwt";
            e.printStackTrace();
        }

        if (errorCode != null) {
            throw new UnAuthorizedException(errorCode);
        }
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
