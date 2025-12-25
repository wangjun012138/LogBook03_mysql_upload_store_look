package com.wangjun.text_proof_platform.modules.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;



@Entity
@Data
@Table(name = "sys_verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    private String code;
    private LocalDateTime expireTime;
    private boolean used =false;
}
