package org.choongang.global.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.global.Utils;
import org.choongang.global.rests.JSONData;
import org.choongang.member.MemberInfo;
import org.choongang.member.constants.Authority;
import org.choongang.member.entities.Authorities;
import org.choongang.member.entities.Member;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginFilter extends GenericFilterBean {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    private final Utils utils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String token = getToken(request);
        if (StringUtils.hasText(token)) {
            loginProcess(token);
        }

        chain.doFilter(request, response);
    }

    /**
     * JWT 토큰 으로 회원 정보 로그인 처리
     *
     * @param token
     */
    private void loginProcess(String token) {


        try {
            String apiUrl = utils.url("/account", "member-service");
            // api서버 주소/account

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<JSONData> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, JSONData.class);

            // 성공 응답인 경우 로그인 처리
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONData data = response.getBody();
                if (data != null && data.isSuccess()) {
                    String json = om.writeValueAsString(data.getData());
                    Member member = om.readValue(json, Member.class);
                    List<Authorities> tmp = member.getAuthorities();
                    if (tmp == null || tmp.isEmpty()) {
                        Authorities authorities = new Authorities();
                        authorities.setAuthority(Authority.USER);
                        tmp = List.of(authorities);
                    }

                    List<SimpleGrantedAuthority> authorities = tmp.stream()
                            .map(a -> new SimpleGrantedAuthority(a.getAuthority().name()))
                            .toList();

                    MemberInfo memberInfo = MemberInfo.builder()
                            .email(member.getEmail())
                            .password(member.getPassword())
                            .member(member)
                            .authorities(authorities)
                            .build();

                    Authentication authentication = new UsernamePasswordAuthenticationToken(memberInfo, token, memberInfo.getAuthorities());
                    // 로그인 처리
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     *  요청 헤더에서 JWT 토큰 추출
     *  Authorization: Bearer JWT토큰 또는 요청 파라미터 token 값
     *
     * @param request
     * @return
     */
    private String getToken(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String bearerToken = req.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)
                && bearerToken.toUpperCase().startsWith("BEARER ")) {
            return bearerToken.substring(7).trim();
        }

        String token = req.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }

        return null;
    }
}
