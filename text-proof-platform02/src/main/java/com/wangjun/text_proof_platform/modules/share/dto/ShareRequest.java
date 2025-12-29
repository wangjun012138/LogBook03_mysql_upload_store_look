package com.wangjun.text_proof_platform.modules.share.dto;

import lombok.Data;

@Data
public class ShareRequest {
    private Long proofId;
    private String targetUsername;

    // 有效期（分钟），如果传 null 或 <=0 则表示永久
    private Long validMinutes;
}