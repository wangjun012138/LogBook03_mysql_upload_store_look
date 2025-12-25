package com.wangjun.text_proof_platform.modules.proof.dto;

import lombok.Data;

@Data
public class UploadRequest {
    private String subject;   // 主题
    private String content;   // 文本内容
    //private String username;  // 当前演示阶段，暂时由前端传用户名（实际生产应从 Token 获取）
}