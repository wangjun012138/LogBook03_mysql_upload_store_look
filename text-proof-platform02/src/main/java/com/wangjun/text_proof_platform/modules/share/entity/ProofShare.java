package com.wangjun.text_proof_platform.modules.share.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "biz_proof_share")
public class ProofShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proofId;

    @Column(nullable = false)
    private String fromUsername;

    @Column(nullable = false)
    private String toUsername;

    private LocalDateTime sharedAt;

    // --- 新增字段 ---

    // 过期时间，如果为 null 则代表“永久有效”
    private LocalDateTime expireTime;

    // 是否已撤销/取消分享 (默认 false)
    @Column(nullable = false, columnDefinition = "bit(1) default 0") // 强制数据库不为null（可选）
    private Boolean isRevoked = false; // 使用 Boolean 包装类，防止拆箱NPE
    @PrePersist
    protected void onCreate() {
        this.sharedAt = LocalDateTime.now();
    }
}