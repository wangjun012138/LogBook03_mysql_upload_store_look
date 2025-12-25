package com.wangjun.text_proof_platform.modules.user.controller;


import com.wangjun.text_proof_platform.common.ApiResponse;
import com.wangjun.text_proof_platform.modules.user.dto.ChangePwdRequest;
import com.wangjun.text_proof_platform.modules.user.dto.LoginRequest;
import com.wangjun.text_proof_platform.modules.user.dto.RegisterRequest;
import com.wangjun.text_proof_platform.modules.user.dto.ResetPwdRequest;
import com.wangjun.text_proof_platform.modules.user.entity.User;
import com.wangjun.text_proof_platform.modules.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    // 注入我们在 SecurityConfig 里定义的 Repository
    @Autowired
    private SecurityContextRepository securityContextRepository;
    // 发送验证码
    @PostMapping("/code")
    public ApiResponse<Void> sendCode(@RequestParam String email) {
        authService.sendCode(email);
        return ApiResponse.success("验证码已发送");
    }

    // 注册
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest req) {
        authService.register(req.getEmail(), req.getUsername(), req.getPassword(), req.getCode());
        return ApiResponse.success("注册成功");
    }

    // 登录
    @PostMapping("/login")
    public ApiResponse<Object> login(@RequestBody LoginRequest req,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        try {
            // 1. 业务层验证账号密码 (如果错误会抛异常)
            User user = authService.login(req.getAccount(), req.getPassword());

            // 2. 【核心修改】手动帮用户“登录”到 Spring Security
            // 创建认证令牌 (这里赋予一个默认角色 ROLE_USER)
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_USER")
            );

            // 3. 创建空的上下文并设置认证信息
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(token);
            SecurityContextHolder.setContext(context);

            // 4. 【关键】将上下文保存到 Session 中 (JSESSIONID)
            securityContextRepository.saveContext(context, request, response);

            return ApiResponse.success("登录成功", user.getId());
        } catch (Exception e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    // 修改密码 (旧密码方式)
    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@RequestBody ChangePwdRequest req) {
        authService.changePassword(req.getUsername(), req.getOldPassword(), req.getNewPassword());
        return ApiResponse.success("密码修改成功");
    }

    // 重置密码 (忘记密码方式)
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPwdRequest req) {
        authService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return ApiResponse.success("密码重置成功");
    }
}
