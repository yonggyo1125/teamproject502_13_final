package org.choongang.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RequestUpdate {
    private String email;
    
    @NotBlank
    private String userName;

    private String password;

    private String confirmPassword;

    private String mobile;

    private List<String> authority;
}
