package com.cine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginReq {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
}

