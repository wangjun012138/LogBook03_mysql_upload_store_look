package com.wangjun.text_proof_platform.modules.user.dto;
import lombok.Data;

@Data
public class ResetPwdRequest {
    private String email;
    private String code;
    private String newPassword;
}