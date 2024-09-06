package org.choongang.file.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.choongang.global.Utils;
import org.choongang.global.rests.JSONData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
//@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FileControllerTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Utils utils;

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void init() throws Exception {
        // 회원 가입 시키기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> params = new HashMap<>();
        params.put("email", "user" + System.currentTimeMillis() + "@test.org");
        params.put("password", "_aA123456");
        params.put("confirmPassword", "_aA123456");
        params.put("userName", "사용자02");
        params.put("mobile", "01010001000");
        params.put("agree", "true");

        String jsonParams = om.writeValueAsString(params);

        HttpEntity<String> request = new HttpEntity<>(jsonParams, headers);

        String url = utils.url("/account", "member-service");

        ResponseEntity<Void> response = restTemplate.postForEntity(URI.create(url), request, Void.class);
       if (response.getStatusCode() == HttpStatus.CREATED) { // 성공

           Map<String, String> params2 = new HashMap<>();
           params2.put("email", params.get("email"));
           params2.put("password", params.get("password"));
           String jsonParams2 = om.writeValueAsString(params2);

           HttpEntity<String> req = new HttpEntity<>(jsonParams2, headers);

           JSONData data = restTemplate.postForObject(URI.create(utils.url("/account/token", "member-service")), req, JSONData.class);
           token = (String)data.getData();
       }

    }

    @Test
    void uploadTest() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.png", "image/png", "abc".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/upload")
                        .file(file1)
                        .file(file2)
                .header("Authentication", "Bearer " + token)
                        .param("gid", "testgid")
                        .param("testlocation", "testlocation")
                ).andDo(print());
    }
}
