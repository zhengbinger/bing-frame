package com.bing.framework.runner;

import com.bing.framework.util.PasswordFormatChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 密码格式检查Runner
 * 在应用启动时检查数据库中用户密码的格式
 * 
 * @author zhengbing
 * @date 2025-11-10
 */
@Component
public class PasswordFormatCheckRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordFormatCheckRunner.class);
    
    @Autowired
    private PasswordFormatChecker passwordFormatChecker;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("启动密码格式检查...");
        // 检查所有用户的密码格式
        passwordFormatChecker.checkAndFixAllPasswords();
        log.info("密码格式检查完成");
    }
}