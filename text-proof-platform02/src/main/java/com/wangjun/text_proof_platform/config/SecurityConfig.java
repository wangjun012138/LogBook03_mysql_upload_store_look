package com.wangjun.text_proof_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. 只允许 /api/auth/** (登录/注册) 匿名访问
                        .requestMatchers("/api/auth/**").permitAll()
                        // 2. 【关键】其他所有请求（包括 /api/proof/**）都必须登录！
                        // 如果你之前加了 .requestMatchers("/api/proof/**").permitAll()，请删掉它！
                        .anyRequest().authenticated()
                )
                // 3. 配置 Session 管理（Spring Security 6 默认需要显式保存上下文）
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository())
                )
                // ============ 【新增：退出登录配置】 ============
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // 1. 自定义退出的接口 URL
                        .invalidateHttpSession(true)   // 2. 让 Session 失效
                        .deleteCookies("JSESSIONID")   // 3. 删除 Cookie
                        .logoutSuccessHandler((req, resp, auth) -> {
                            // 4. 退出成功后，返回 JSON 数据，而不是跳转页面
                            resp.setContentType("application/json;charset=UTF-8");
                            resp.getWriter().write("{\"code\":200, \"message\":\"退出成功\"}");
                        })
                )
                // ==============================================
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 【新增】注册一个存储库 Bean，用于在登录时手动保存 Session
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}