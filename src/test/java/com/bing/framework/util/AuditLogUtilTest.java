package com.bing.framework.util;

import com.bing.framework.entity.AuditLog;
import com.bing.framework.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;

/**
 * AuditLogUtil 测试类
 * 测试审计日志工具的核心功能：正常日志记录、成功操作日志、失败操作日志等
 */
class AuditLogUtilTest {

    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private AuditLogUserCache auditLogUserCache;
    
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        // 初始化 Mockito 注解
        closeable = MockitoAnnotations.openMocks(this);
        
        // 重置请求上下文，确保测试独立性
        RequestContextHolder.setRequestAttributes(null);
        
        // 创建并设置 AuditLogUtil 实例用于测试
        AuditLogUtil auditLogUtil = new AuditLogUtil(auditLogService, auditLogUserCache);
        AuditLogUtil.setInstance(auditLogUtil);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 清理资源
        closeable.close();
        // 重置 AuditLogUtil 实例，避免测试间的影响
        AuditLogUtil.resetInstance();
        // 清理请求上下文
        RequestContextHolder.setRequestAttributes(null);
    }

    @Test
    void testLog() {
        // 设置模拟请求和用户信息
        mockRequestContext("testuser", "192.168.1.100");
        
        // 调用测试方法
        AuditLogUtil.log("testModule", "testOperation", "testDescription", "{}", "testResult");
        
        // 验证服务方法被调用
        verify(auditLogService, times(1)).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testLogSuccess() {
        // 设置模拟请求和用户信息
        mockRequestContext("testuser", "192.168.1.100");
        
        // 调用测试方法
        AuditLogUtil.logSuccess("testModule", "testOperation", "testDescription", "{}");
        
        // 验证服务方法被调用
        verify(auditLogService, times(1)).recordAuditLogAsync(any(AuditLog.class));
    }

    @Test
    void testLogFailure() {
        // 设置模拟请求和用户信息
        mockRequestContext("testuser", "192.168.1.100");
        
        // 调用测试方法
        AuditLogUtil.logFailure("testModule", "testOperation", "testDescription", "{}", "testError");
        
        // 验证服务方法被调用
        verify(auditLogService, times(1)).recordAuditLogAsync(any(AuditLog.class));
    }
    
    @Test
    void testLogWithAnonymousUser() {
        // 不设置用户信息，将返回默认的anonymous
        
        // 调用测试方法
        AuditLogUtil.log("testModule", "testOperation", "testDescription", "{}", "testResult");
        
        // 验证服务方法被调用
        verify(auditLogService, times(1)).recordAuditLogAsync(any(AuditLog.class));
    }
    
    @Test
    void testLogWithUnknownIp() {
        // 不设置请求上下文，将返回默认的unknown ip
        
        // 调用测试方法
        AuditLogUtil.log("testModule", "testOperation", "testDescription", "{}", "testResult");
        
        // 验证服务方法被调用
        verify(auditLogService, times(1)).recordAuditLogAsync(any(AuditLog.class));
    }
    
    /**
     * 模拟请求上下文
     * 
     * @param username 用户名
     * @param ipAddress IP地址
     */
    private void mockRequestContext(String username, String ipAddress) {
        // 创建真实的ServletRequestAttributes对象而不是模拟它
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-Name")).thenReturn(username);
        when(request.getHeader("x-forwarded-for")).thenReturn(ipAddress);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        
        // 使用PowerMockito或其他方式可能更好，但这里采用简单方式
        // 直接设置请求上下文
        RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(request));
    }

}