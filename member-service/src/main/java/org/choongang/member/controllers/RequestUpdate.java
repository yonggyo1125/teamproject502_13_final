package org.choongang.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestUpdate {
    @NotBlank
    private String userName;

    private String password;

    private String confirmPassword;

    private String mobile;
}
