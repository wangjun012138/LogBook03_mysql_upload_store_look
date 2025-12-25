package com.wangjun.text_proof_platform.modules.user.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String account;
    private String password;
}