package com.wangjun.text_proof_platform.modules.share.dto;

import com.wangjun.text_proof_platform.modules.proof.entity.TextProof;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SharedProofResponse {
    // 包含原始存证信息
    private TextProof proof;
    // 额外信息
    private String sharedBy;     // 谁分享的
    private LocalDateTime sharedAt; // 什么时候分享的
}