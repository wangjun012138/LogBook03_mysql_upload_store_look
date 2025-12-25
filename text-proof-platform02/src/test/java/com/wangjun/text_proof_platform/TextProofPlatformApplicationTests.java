package com.wangjun.text_proof_platform;

// 请检查这些 Import 是否对应你实际的类路径
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wangjun.text_proof_platform.modules.user.dto.LoginRequest;
import com.wangjun.text_proof_platform.modules.user.dto.RegisterRequest;
import com.wangjun.text_proof_platform.modules.user.entity.VerificationCode;
import com.wangjun.text_proof_platform.modules.user.repository.UserRepository;
import com.wangjun.text_proof_platform.modules.user.repository.VerificationCodeRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

// 静态导入，用于简化代码
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 开启 MockMvc，模拟 HTTP 请求
@Transactional        // 测试结束后自动回滚事务，不污染数据库
class TextProofPlatformApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VerificationCodeRepository codeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper; // 用于将对象转为 JSON 字符串

    @Test
    void testRegisterAndLoginFlow() throws Exception {
        // 准备测试数据
        String email = "junit_test@example.com";
        String username = "junit_user";
        String password = "password123";

        // ==========================================
        // 第一步：发送验证码
        // ==========================================
        mockMvc.perform(post("/api/auth/code")
                        .param("email", email)) // 发送 URL 参数
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("验证码已发送"))
                .andDo(print()); // 打印请求和响应详情，方便调试

        // ==========================================
        // 第二步：从数据库获取刚才生成的验证码 (模拟用户查邮件)
        // ==========================================
        VerificationCode vc = codeRepository.findFirstByEmailAndUsedFalseOrderByExpireTimeDesc(email)
                .orElseThrow(() -> new RuntimeException("测试失败：数据库中未找到验证码"));
        String code = vc.getCode();
        System.out.println(">>> 测试获取到的验证码: " + code);

        // ==========================================
        // 第三步：注册用户
        // ==========================================
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail(email);
        registerReq.setUsername(username);
        registerReq.setPassword(password);
        registerReq.setCode(code);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON) // 设置请求头为 JSON
                        .content(objectMapper.writeValueAsString(registerReq))) // 将对象转为 JSON 字符串
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("注册成功"));

        // 验证数据库中确实有了这个用户
        Assertions.assertTrue(userRepository.findByUsername(username).isPresent());

        // ==========================================
        // 第四步：登录测试
        // ==========================================
        LoginRequest loginReq = new LoginRequest();
        loginReq.setAccount(username);
        loginReq.setPassword(password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").exists()); // 确保返回了用户ID
    }
}