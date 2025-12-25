package com.wangjun.text_proof_platform.modules.user.dto;
import lombok.Data;

@Data
public class ChangePwdRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}