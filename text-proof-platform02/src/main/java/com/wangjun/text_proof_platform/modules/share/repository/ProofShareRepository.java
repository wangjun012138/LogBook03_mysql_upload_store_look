package com.wangjun.text_proof_platform.modules.share.repository;

import com.wangjun.text_proof_platform.modules.share.entity.ProofShare;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProofShareRepository extends JpaRepository<ProofShare, Long> {

    // 1. 【接收列表】查找某人接收到的所有分享记录（按时间倒序）
    // 作用：用于 B 用户查看“我收到了哪些文件”
    List<ProofShare> findByToUsernameOrderBySharedAtDesc(String toUsername);

    // 2. 【发送列表 - 新增】查找某人发出的所有分享记录（按时间倒序）
    // 作用：用于 A 用户查看“我分享给谁了”，从而获取 id 进行撤销
    List<ProofShare> findByFromUsernameOrderBySharedAtDesc(String fromUsername);

    // 3. 查重校验
    boolean existsByProofIdAndToUsername(Long proofId, String toUsername);

    // 4. 【详情查询】查找特定的分享记录 -> 只取最新的一条
    // 作用：防止数据库有重复脏数据导致报错
    Optional<ProofShare> findFirstByProofIdAndToUsernameOrderBySharedAtDesc(Long proofId, String toUsername);
}