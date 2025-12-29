package com.wangjun.text_proof_platform.modules.share.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "biz_proof_link")
public class ProofLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proofId;

    @Column(nullable = false)
    private String fromUsername;

    // 唯一的分享 Token (例如 UUID)
    @Column(nullable = false, unique = true)
    private String shareToken;

    private LocalDateTime createAt;

    // 过期时间 (null = 永久)
    private LocalDateTime expireTime;

    // 是否撤销
    private boolean isRevoked = false;

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }
}