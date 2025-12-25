package com.wangjun.text_proof_platform.modules.user.service;



import com.wangjun.text_proof_platform.modules.user.entity.User;
import com.wangjun.text_proof_platform.modules.user.entity.VerificationCode;
import com.wangjun.text_proof_platform.modules.user.repository.UserRepository;
import com.wangjun.text_proof_platform.modules.user.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service // 告诉 Spring：这是业务逻辑层
public class AuthService {

    // 2. 自动配备助手
    @Autowired private UserRepository userRepository;           // 管用户的
    @Autowired private VerificationCodeRepository codeRepository; // 管验证码的
    @Autowired private PasswordEncoder passwordEncoder;         // 管加密的
    // 1. 发送验证码逻辑
    public void sendCode(String email) {
        // 1. 造一个 6 位随机数 (100000 ~ 999999)
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        // 2. 填单子 (创建实体对象)
        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setExpireTime(LocalDateTime.now().plusMinutes(5)); // 5分钟后过期

        // 3. 存档 (存入数据库)
        codeRepository.save(vc);

        // 4. 假装发邮件 (控制台打印)
        System.out.println(">>> 邮件发送至 [" + email + "] 验证码: " + code);
    }

    // 2. 注册逻辑
    @Transactional // 事务：保证一系列数据库操作要么全成功，要么全失败
    public void register(String email, String username, String password, String code) {
        verifyCode(email, code); // 第一步：先看验证码对不对，不对直接报错退出

        // 第二步：查重 (邮箱或用户名是不是被占用了)
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            throw new RuntimeException("注册失败：邮箱或用户名已存在");
        }

        // 第三步：建档 (创建用户对象)
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);

        // 重点！！密码必须加密！存进去的是 "$2a$10$..." 这种乱码
        user.setPassword(passwordEncoder.encode(password));

        // 第四步：保存
        userRepository.save(user);
    }

    // 3. 登录逻辑
    public User login(String account, String password) {
        // 1. 灵活查找：用户可能填的是用户名，也可能填的是邮箱
        Optional<User> userOpt = userRepository.findByUsername(account);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(account); // 没找到用户名，试试是不是邮箱
        }

        // 2. 核对身份
        // userOpt.isEmpty(): 根本没这个人
        // !matches(...): 密码对不上 (注意：这里用 matches 方法比对明文和密文)
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            throw new RuntimeException("登录失败：账号或密码错误");
        }

        return userOpt.get(); // 3. 登录成功，把人带出来
    }
    // 4. 旧密码修改
    @Transactional
    public void changePassword(String username, String oldPwd, String newPwd) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new RuntimeException("修改失败：旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPwd));
        userRepository.save(user);
    }

    // 5. 忘记密码重置
    @Transactional
    public void resetPassword(String email, String code, String newPwd) {
        verifyCode(email, code); // 校验验证码

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));

        user.setPassword(passwordEncoder.encode(newPwd));
        userRepository.save(user);
    }
    // 辅助方法：校验验证码
    private void verifyCode(String email, String inputCode) {
        VerificationCode vc = codeRepository.findFirstByEmailAndUsedFalseOrderByExpireTimeDesc(email)
                .orElseThrow(() -> new RuntimeException("验证码无效或不存在"));

        if (vc.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期");
        }
        if (!vc.getCode().equals(inputCode)) {
            throw new RuntimeException("验证码错误");
        }

        vc.setUsed(true); // 标记为已使用，防止二次使用
        codeRepository.save(vc);
    }

    // ... (修改密码等逻辑可参考原文件，此处省略以保持简洁)
}