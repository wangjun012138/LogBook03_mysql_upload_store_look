package com.wangjun.text_proof_platform.modules.proof.repository;

import com.wangjun.text_proof_platform.modules.proof.entity.TextProof;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // 引入 List
import java.util.Optional;

public interface TextProofRepository extends JpaRepository<TextProof, Long> {

    // 1. 获取最新记录（用于哈希链，已存在）
    Optional<TextProof> findTopByOrderByIdDesc();

    // 2. 核心：根据 ID 和 用户名 查找（确保只能看自己的，已存在）
    Optional<TextProof> findByIdAndUsername(Long id, String username);

    // 3. 【新增建议】查找该用户的所有记录（方便用户列表展示）
    List<TextProof> findByUsername(String username);
}