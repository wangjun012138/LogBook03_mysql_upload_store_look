package com.wangjun.text_proof_platform.modules.user.repository;



import com.wangjun.text_proof_platform.modules.user.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    // 找：指定邮箱 + 没用过(used=false) + 按过期时间倒序排 的 第一个

    // 这一句长长的方法名，直接翻译成了复杂的 SQL 查询！

    Optional<VerificationCode> findFirstByEmailAndUsedFalseOrderByExpireTimeDesc(String email);

}