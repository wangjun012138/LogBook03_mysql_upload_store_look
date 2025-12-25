package com.wangjun.text_proof_platform.modules.proof.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "biz_text_proof") // 业务表通常用 biz_ 前缀
public class TextProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 存证内容（使用 Lob 存储大文本）
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 内容主题
    @Column(nullable = false)
    private String subject;

    // 所属用户 (直接存用户名，方便查询)
    @Column(nullable = false)
    private String username;

    // 当前记录的哈希值 (核心凭证)
    @Column(nullable = false, unique = true)
    private String currentHash;

    // 上一条记录的哈希值 (构建哈希链，由系统自动生成)
    private String previousHash;

    // 存储时间
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}