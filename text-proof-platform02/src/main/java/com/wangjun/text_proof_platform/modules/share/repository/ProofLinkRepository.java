package com.wangjun.text_proof_platform.modules.share.repository;

import com.wangjun.text_proof_platform.modules.share.entity.ProofLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProofLinkRepository extends JpaRepository<ProofLink, Long> {
    // 根据 Token 查找
    Optional<ProofLink> findByShareToken(String shareToken);
}