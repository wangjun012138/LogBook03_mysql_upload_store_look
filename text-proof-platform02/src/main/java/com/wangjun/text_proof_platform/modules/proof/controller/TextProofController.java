package com.wangjun.text_proof_platform.modules.proof.controller;

import com.wangjun.text_proof_platform.common.ApiResponse;
import com.wangjun.text_proof_platform.modules.proof.dto.UploadRequest;
import com.wangjun.text_proof_platform.modules.proof.entity.TextProof;
import com.wangjun.text_proof_platform.modules.proof.service.TextProofService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/proof")
public class TextProofController {

    @Autowired
    private TextProofService textProofService;

    /**
     * 【新增】获取当前用户的存证列表
     * 对应需求：检索与验证
     * 注意：这个方法必须定义在 `/{id}` 之前，或者使用正则区分
     */
    @GetMapping("/list")
    public ApiResponse<List<TextProof>> getMyProofList(Principal principal) {
        if (principal == null) {
            return ApiResponse.error(403, "请先登录");
        }
        String currentUsername = principal.getName();

        // 调用你在 Service 中新写的 getMyProofs 方法
        List<TextProof> list = textProofService.getMyProofs(currentUsername);

        return ApiResponse.success("获取列表成功", list);
    }

    /**
     * 上传文本接口
     * 对应需求：上传与存储 [cite: 21]
     */
    @PostMapping("/upload")
    public ApiResponse<TextProof> uploadText(@RequestBody UploadRequest req, Principal principal) {
        if (principal == null) {
            return ApiResponse.error(403, "请先登录");
        }
        String currentUsername = principal.getName();

        // 调用 Service 层的哈希链生成逻辑
        TextProof proof = textProofService.saveText(req, currentUsername);

        return ApiResponse.success("文本存证成功", proof);
    }

    /**
     * 【核心修复】获取单条存证详情
     * 对应需求：查看信息 [cite: 26]
     * * 修改点：@GetMapping("/{id:\\d+}")
     * 解释：{id:\\d+} 表示 id 只能是纯数字。
     * 效果：当请求 /api/proof/list 时，因为 "list" 不是数字，Spring 以前会匹配错，现在会直接跳过这个方法，
     * 正确匹配到上面的 /list 接口。
     */
    @GetMapping("/{id:\\d+}")
    public ApiResponse<TextProof> getTextProof(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ApiResponse.error(403, "请先登录");
        }

        try {
            String currentUsername = principal.getName();
            // 带着用户名去查数据，确保数据保密性 [cite: 27]
            TextProof proof = textProofService.getTextProof(id, currentUsername);
            return ApiResponse.success("获取成功", proof);
        } catch (Exception e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}