package com.wangjun.text_proof_platform.modules.share.controller;

import com.wangjun.text_proof_platform.common.ApiResponse;
import com.wangjun.text_proof_platform.modules.share.dto.CreateLinkRequest;
import com.wangjun.text_proof_platform.modules.share.dto.ShareRequest;
import com.wangjun.text_proof_platform.modules.share.dto.SharedProofResponse;
import com.wangjun.text_proof_platform.modules.share.entity.ProofShare;
import com.wangjun.text_proof_platform.modules.share.repository.ProofShareRepository;
import com.wangjun.text_proof_platform.modules.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired private ShareService shareService;

    @Autowired private ProofShareRepository shareRepository; // 【新增】用于查询列表


    // ==========================================
    // 0. 获取“我发出的分享”列表 (【新增】用于管理/撤销)
    // ==========================================
    @GetMapping("/sent-list")
    public ApiResponse<List<ProofShare>> getMySentShares(Principal principal) {
        // 查询当前登录用户发出的所有分享
        List<ProofShare> list = shareRepository.findByFromUsernameOrderBySharedAtDesc(principal.getName());
        return ApiResponse.success("获取已发送列表成功", list);
    }


    // ==========================================
    // 1. 获取“我收到的分享”列表 (修复 404 错误)
    // ==========================================
    @GetMapping("/list")
    public ApiResponse<List<ProofShare>> getMyReceivedShares(Principal principal) {
        // 直接查询数据库，返回当前用户收到的所有分享记录（按时间倒序）
        List<ProofShare> list = shareRepository.findByToUsernameOrderBySharedAtDesc(principal.getName());
        return ApiResponse.success("获取成功", list);
    }

    // ==========================================
    // 2. 查看具体的点对点分享详情 (用户点击列表项时调用)
    // ==========================================
    @GetMapping("/detail/{proofId}")
    public ApiResponse<SharedProofResponse> getShareDetail(@PathVariable Long proofId, Principal principal) {
        // 调用 Service 中的 getSharedProofDetail 方法（包含过期和撤销检查）
        SharedProofResponse data = shareService.getSharedProofDetail(proofId, principal.getName());
        return ApiResponse.success("获取详情成功", data);
    }

    // ==========================================
    // 3. 分享给用户 (含时效)
    // ==========================================
    @PostMapping("/to-user")
    public ApiResponse<Void> shareToUser(@RequestBody ShareRequest req, Principal principal) {
        shareService.shareToUser(req.getProofId(), principal.getName(), req.getTargetUsername(), req.getValidMinutes());
        return ApiResponse.success("分享成功");
    }

    // ==========================================
    // 4. 生成分享链接 (含时效)
    // ==========================================
    @PostMapping("/create-link")
    public ApiResponse<String> createLink(@RequestBody CreateLinkRequest req, Principal principal) {
        String token = shareService.createShareLink(req.getProofId(), principal.getName(), req.getValidMinutes());
        // 前端拿到 token 后可以拼接成 http://localhost:8080/share/view?token=...
        return ApiResponse.success("链接生成成功", token);
    }

    // ==========================================
    // 5. 通过链接查看存证 (公开/半公开接口)
    // ==========================================
    @GetMapping("/view-link")
    public ApiResponse<SharedProofResponse> viewByLink(@RequestParam String token) {
        // 这里不需要 Principal，持有链接即可查看（根据业务需求可调整）
        SharedProofResponse data = shareService.getProofByLink(token);
        return ApiResponse.success("获取成功", data);
    }

    // ==========================================
    // 6. 撤销分享 (用户分享)
    // ==========================================
    @PostMapping("/revoke/user")
    public ApiResponse<Void> revokeUserShare(@RequestParam Long shareId, Principal principal) {
        shareService.revokeUserShare(shareId, principal.getName());
        return ApiResponse.success("已取消分享");
    }

    // ==========================================
    // 7. 撤销分享 (链接分享)
    // ==========================================
    @PostMapping("/revoke/link")
    public ApiResponse<Void> revokeLinkShare(@RequestParam String token, Principal principal) {
        shareService.revokeLinkShare(token, principal.getName());
        return ApiResponse.success("链接已失效");
    }
}