package com.wangjun.text_proof_platform.modules.user.repository;



import com.wangjun.text_proof_platform.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


// 继承 JpaRepository<实体类型, ID类型>，Spring 会自动帮你实现 save, findById 等方法

public interface UserRepository extends JpaRepository<User, Long> {
    // 按照规则命名方法，Spring 就能自动生成 SQL：select * from user where username = ?
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    // 检查是否存在
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}