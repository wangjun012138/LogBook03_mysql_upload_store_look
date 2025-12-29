package com.wangjun.text_proof_platform.modules.share.dto;

import lombok.Data;

@Data
public class CreateLinkRequest {
    private Long proofId;
    // 有效期（分钟），null 或 <=0 为永久
    private Long validMinutes;
}