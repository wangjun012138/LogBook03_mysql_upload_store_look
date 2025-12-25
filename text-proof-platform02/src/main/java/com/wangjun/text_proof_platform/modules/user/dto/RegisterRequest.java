package com.wangjun.text_proof_platform.modules.user.dto;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String code;
}