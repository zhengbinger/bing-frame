package com.bing.framework.util;

import com.bing.framework.entity.User;
import com.bing.framework.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码格式检查器
 * 用于检查和修复数据库中不符合BCrypt格式的密码
 * 
 * @author zhengbing
 * @date 2025-11-10
 */
@Component
public class PasswordFormatChecker {

    private static final Logger log = LoggerFactory.getLogger(PasswordFormatChecker.class);
    
    // BCrypt密码格式正则表达式
    private static final String BCRYPT_PATTERN = "^\\$2[ayb]\\$.{56}$";
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 检查并修复所有用户密码格式
     * 注意：此方法应该谨慎使用，仅在必要时运行
     */
    public void checkAndFixAllPasswords() {
        try {
            List<User> allUsers = userService.getAllUsers();
            int fixedCount = 0;
            int totalCount = allUsers.size();
            
            log.info("开始检查所有用户密码格式，共 {} 个用户", totalCount);
            
            for (User user : allUsers) {
                if (user.getPassword() != null && !isValidBCryptFormat(user.getPassword())) {
                    log.warn("发现非BCrypt格式密码，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
                    
                    // 注意：在实际生产环境中，不应该直接重置用户密码
                    // 这里仅作为示例，实际应用中应该通过其他方式（如发送重置邮件）处理
                    // userService.resetPassword(user.getId(), generateDefaultPassword());
                    // fixedCount++;
                }
            }
            
            log.info("密码格式检查完成，共检查 {} 个用户，发现 {} 个需要修复的密码（已记录但未自动修复）", 
                    totalCount, fixedCount);
            log.info("警告：请联系相关用户重置密码，使用标准BCrypt加密格式");
            
        } catch (Exception e) {
            log.error("检查密码格式时发生错误", e);
        }
    }
    
    /**
     * 检查密码是否为有效的BCrypt格式
     * 
     * @param password 待检查的密码
     * @return 是否为有效BCrypt格式
     */
    public boolean isValidBCryptFormat(String password) {
        if (password == null) {
            return false;
        }
        return password.matches(BCRYPT_PATTERN);
    }
    
    /**
     * 生成默认密码（仅供测试使用）
     * 
     * @return 默认密码
     */
    private String generateDefaultPassword() {
        // 生成一个临时密码
        return "Temp@123456";
    }
}