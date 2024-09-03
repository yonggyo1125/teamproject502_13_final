package org.choongang.file.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RequestSelect {
    @NotBlank
    private String gid;
    private String location;
    @NotNull
    private List<Long> seq;
    private int cnt;
}
