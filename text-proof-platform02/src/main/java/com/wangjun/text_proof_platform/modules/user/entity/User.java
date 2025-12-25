package com.wangjun.text_proof_platform.modules.user.entity;





import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
//字段：id，username，email，password，createdAt(LocalDateTime)
//函数：onCreate注册时间createdAt
@Entity
@Data
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    private LocalDateTime creatAt;
    @PrePersist
    protected void OnCreate() {
        this.creatAt = LocalDateTime.now();
    }
}