package com.wangjun.text_proof_platform.modules.proof.service;

import com.wangjun.text_proof_platform.modules.proof.dto.UploadRequest;
import com.wangjun.text_proof_platform.modules.proof.entity.TextProof;
import com.wangjun.text_proof_platform.modules.proof.repository.TextProofRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TextProofService {

    @Autowired
    private TextProofRepository proofRepository;

    /**
     * 上传并存证文本
     */
    @Transactional
    public TextProof saveText(UploadRequest req, String username) {
        TextProof proof = new TextProof();
        proof.setSubject(req.getSubject());
        proof.setContent(req.getContent());
        proof.setUsername(username); // 使用传入的 username

        // 1. 获取当前时间 (作为哈希计算的一部分，增加随机性)
        LocalDateTime now = LocalDateTime.now();
        proof.setCreatedAt(now);

        // 2. 获取上一条记录的哈希值 (构建哈希链)
        String prevHash = proofRepository.findTopByOrderByIdDesc()
                .map(TextProof::getCurrentHash)
                .orElse("00000000000000000000000000000000"); // 创世区块/第一条记录的默认值
        proof.setPreviousHash(prevHash);

        // 3. 计算当前哈希 = SHA256(上一条哈希 + 用户 + 主题 + 内容 + 时间)
        // 【修正点】：这里使用传入的 username 参数，而不是 req.getUsername()
        String rawData = prevHash + username + req.getSubject() + req.getContent() + now.toString();

        String currentHash = calculateSHA256(rawData);
        proof.setCurrentHash(currentHash);

        // 4. 保存入库
        return proofRepository.save(proof);
    }

    /**
     * 查看存证详情
     */
    public TextProof getTextProof(Long id) {
        return proofRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到该存证记录"));
    }
    /**
     * 【修改】查看存证详情
     * 增加 username 参数，强制检查归属权
     */
    public TextProof getTextProof(Long id, String username) {
        // 调用刚才在 Repository 里写的新方法
        return proofRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new RuntimeException("未找到该存证记录或您无权查看"));
    }
    /**
     * 【新增】获取当前用户的所有存证列表
     */
    public List<TextProof> getMyProofs(String username) {
        return proofRepository.findByUsername(username);
    }
    // 辅助方法：计算 SHA-256
    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception e) {
            throw new RuntimeException("哈希计算失败");
        }
    }

    // 辅助方法：字节转十六进制字符串
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}