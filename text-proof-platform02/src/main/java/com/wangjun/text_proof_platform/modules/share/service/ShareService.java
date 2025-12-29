package com.wangjun.text_proof_platform.modules.share.service;

import com.wangjun.text_proof_platform.modules.proof.entity.TextProof;
import com.wangjun.text_proof_platform.modules.proof.repository.TextProofRepository;
import com.wangjun.text_proof_platform.modules.share.dto.SharedProofResponse;
import com.wangjun.text_proof_platform.modules.share.entity.ProofLink;
import com.wangjun.text_proof_platform.modules.share.entity.ProofShare;
import com.wangjun.text_proof_platform.modules.share.repository.ProofLinkRepository;
import com.wangjun.text_proof_platform.modules.share.repository.ProofShareRepository;
import com.wangjun.text_proof_platform.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShareService {

    @Autowired private ProofShareRepository shareRepository;
    @Autowired private ProofLinkRepository linkRepository;
    @Autowired private TextProofRepository proofRepository;
    @Autowired private UserRepository userRepository;

    // ================= 1. 用户点对点分享 =================

    @Transactional
    public void shareToUser(Long proofId, String currentUsername, String targetUsername, Long validMinutes) {
        if (currentUsername.equals(targetUsername)) throw new RuntimeException("不能分享给自己");
        if (!userRepository.existsByUsername(targetUsername)) throw new RuntimeException("目标用户不存在");

        // 【修改点 A】：允许重复分享
        // 之前这里有检查 existsBy... 并抛出异常的代码，现在直接删掉！
        // 逻辑变成：只要 A 想分享，我们就插入一条新的记录（新的有效期）。

        TextProof proof = proofRepository.findByIdAndUsername(proofId, currentUsername)
                .orElseThrow(() -> new RuntimeException("存证不存在或您无权分享"));

        ProofShare share = new ProofShare();
        share.setProofId(proof.getId());
        share.setFromUsername(currentUsername);
        share.setToUsername(targetUsername);

        if (validMinutes != null && validMinutes > 0) {
            share.setExpireTime(LocalDateTime.now().plusMinutes(validMinutes));
        } else {
            share.setExpireTime(null);
        }

        share.setIsRevoked(false);
        shareRepository.save(share);
    }

    // ================= 2. 生成分享链接 =================

    @Transactional
    public String createShareLink(Long proofId, String currentUsername, Long validMinutes) {
        TextProof proof = proofRepository.findByIdAndUsername(proofId, currentUsername)
                .orElseThrow(() -> new RuntimeException("存证不存在或您无权分享"));

        ProofLink link = new ProofLink();
        link.setProofId(proof.getId());
        link.setFromUsername(currentUsername);
        link.setShareToken(UUID.randomUUID().toString().replace("-", ""));

        if (validMinutes != null && validMinutes > 0) {
            link.setExpireTime(LocalDateTime.now().plusMinutes(validMinutes));
        } else {
            link.setExpireTime(null);
        }

        // 如果 ProofLink 也改成了 Boolean，这里也要用 setIsRevoked
        // 如果 ProofLink 还是 boolean，这里保持 setRevoked
        // 建议统一：假设您还没改 ProofLink，这里暂时不动；如果报错请改为 link.setIsRevoked(false)
        link.setRevoked(false);

        linkRepository.save(link);
        return link.getShareToken();
    }

    // ================= 3. 查看逻辑（含时效检查） =================

    /**
     * 查看点对点分享
     */
    public SharedProofResponse getSharedProofDetail(Long proofId, String currentUsername) {
        // 【修改点 B】：获取逻辑改为“查最新一条”
        // 使用 findFirst...OrderBySharedAtDesc
        // 这样即使数据库里有旧的分享记录，或者之前的 4 条重复记录，系统也只会加载最新的一次分享配置
        ProofShare share = shareRepository.findFirstByProofIdAndToUsernameOrderBySharedAtDesc(proofId, currentUsername)
                .orElseThrow(() -> new RuntimeException("未找到分享记录"));

        if (Boolean.TRUE.equals(share.getIsRevoked())) {
            throw new RuntimeException("该分享已被分享者取消");
        }

        if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
            throw new RuntimeException("该分享已过期");
        }

        TextProof proof = proofRepository.findById(proofId).orElseThrow(() -> new RuntimeException("原存证已删除"));
        SharedProofResponse resp = new SharedProofResponse();
        resp.setProof(proof);
        resp.setSharedBy(share.getFromUsername());
        resp.setSharedAt(share.getSharedAt());
        return resp;
    }

    /**
     * 通过链接查看
     */
    public SharedProofResponse getProofByLink(String token) {
        ProofLink link = linkRepository.findByShareToken(token)
                .orElseThrow(() -> new RuntimeException("无效的分享链接"));

        // 【注意】：如果您只改了 ProofShare 而没改 ProofLink，下面这行不用动。
        // 如果 ProofLink 也改成了 Boolean，请改为 link.getIsRevoked()
        if (link.isRevoked()) {
            throw new RuntimeException("链接已失效（被撤销）");
        }

        if (link.getExpireTime() != null && LocalDateTime.now().isAfter(link.getExpireTime())) {
            throw new RuntimeException("链接已过期");
        }

        TextProof proof = proofRepository.findById(link.getProofId())
                .orElseThrow(() -> new RuntimeException("原存证已删除"));

        SharedProofResponse resp = new SharedProofResponse();
        resp.setProof(proof);
        resp.setSharedBy(link.getFromUsername());
        resp.setSharedAt(link.getCreateAt());
        return resp;
    }

    // ================= 4. 撤销/取消分享 =================

    // 撤销给某人的分享
    @Transactional
    public void revokeUserShare(Long shareId, String currentUsername) {
        ProofShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        if (!share.getFromUsername().equals(currentUsername)) {
            throw new RuntimeException("无权操作");
        }

        // 【修改点 2】: setRevoked(true) -> setIsRevoked(true)
        share.setIsRevoked(true);

        shareRepository.save(share);
    }

    // 撤销链接分享
    @Transactional
    public void revokeLinkShare(String token, String currentUsername) {
        ProofLink link = linkRepository.findByShareToken(token)
                .orElseThrow(() -> new RuntimeException("链接不存在"));

        if (!link.getFromUsername().equals(currentUsername)) {
            throw new RuntimeException("无权操作");
        }

        // 同理，如果 ProofLink 没改类型，这里保持 setRevoked
        link.setRevoked(true);

        linkRepository.save(link);
    }
}