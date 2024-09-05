package org.choongang.global.rests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.choongang.global.Utils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ApiRequest {
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    private final Utils utils;

    private ResponseEntity<String> response;

    public ApiRequest request(String url, String serviceId) {
        return request(url, serviceId, HttpMethod.GET, null);
    }

    public ApiRequest request(String url, String serviceId, HttpMethod method, Object data) {
        String requestUrl = utils.url(url, serviceId);
        method = Objects.requireNonNullElse(method, HttpMethod.GET);

        HttpHeaders headers = new HttpHeaders();
        String token = utils.getToken();
        if (StringUtils.hasText(token)) { // 토큰이 있다면 토큰 함께 전달
            headers.setBearerAuth(token);
        }

        if (method != HttpMethod.GET && method != HttpMethod.DELETE) { // POST, PUT, PATCH 방식인 경우
            headers.setContentType(MediaType.APPLICATION_JSON);
            try {
                String body = om.writeValueAsString(data);
                HttpEntity<String> request = new HttpEntity<>(body, headers);

                this.response = restTemplate.exchange(URI.create(requestUrl), method, request, String.class);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else { // GET 또는 DELETE 방식인 경우

            HttpEntity<Void> request = new HttpEntity<>(headers);
            this.response = restTemplate.exchange(URI.create(requestUrl), method, request, String.class);
        }

        return this;
    }

    /**
     * 응답 코드
     * @return
     */
    public HttpStatusCode getStatus() {
        return response.getStatusCode();
    }

    public ResponseEntity<String> getResponse() {
        return response;
    }

    /**
     * JSON으로 응답 데이터 변환
     *
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T toJSON(Class<T> clazz) {

        String body = response.getBody();
        if (StringUtils.hasText(body)) {
            try {
                return om.readValue(body, clazz);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * JSON으로 응답 데이터 변환
     *
     * @param typeReference
     * @return
     * @param <T>
     */
    public <T> T toJson(TypeReference<T> typeReference) {
        String body = response.getBody();
        if (StringUtils.hasText(body)) {
            try {
                return om.readValue(body, typeReference);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 헤더 정보 조회
     *
     * @return
     */
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    /**
     * 문자열로 응답 데이터 조회
     *
     * @return
     */
    public String toString() {
        return response.getBody();
    }
}
