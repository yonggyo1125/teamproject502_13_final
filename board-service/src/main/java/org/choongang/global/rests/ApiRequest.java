package org.choongang.global.rests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.choongang.global.Utils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ApiRequest {
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    private final Utils utils;

    public ApiRequest request(String url, String serviceId, HttpMethod method, Object data) {
        String requestUrl = utils.url(url, serviceId);

        HttpHeaders headers = new HttpHeaders();
        if (method != HttpMethod.GET && method != HttpMethod.DELETE) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = utils.getToken();
            if (StringUtils.hasText(token)) { // 토큰이 있다면 토큰 함께 전달
                headers.setBearerAuth(token);
            }
        }
        return this;
    }
}
