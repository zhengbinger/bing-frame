package com.bing.framework.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * IP地址工具类
 * 提供获取真实客户端IP地址的功能，支持负载均衡和代理场景
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Slf4j
public class IpUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private IpUtil() {}

    /**
     * 获取客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        try {
            // 检查多个可能包含真实IP的请求头
            String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP", 
                "X-Originating-IP",
                "CF-Connecting-IP",
                "X-Client-IP",
                "X-Forwarded",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
            };

            for (String header : headers) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    // 处理多个IP的情况，取第一个（最接近客户端的）
                    String[] ips = ip.split(",");
                    String realIp = ips[0].trim();
                    if (isValidIp(realIp)) {
                        return realIp;
                    }
                }
            }

            // 如果没有找到有效的代理IP头，使用原始的remote address
            String clientIp = request.getRemoteAddr();
            
            // 如果是本地地址，检查是否有负载均衡器
            if ("127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp)) {
                String localIp = getLocalIp();
                if (localIp != null) {
                    return localIp;
                }
            }
            
            return clientIp;
            
        } catch (Exception e) {
            log.warn("获取客户端IP地址失败", e);
            return "0.0.0.0";
        }
    }

    /**
     * 获取服务器本地IP地址
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // 跳过虚拟接口、环回接口和未启用的接口
                if (networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                
                String name = networkInterface.getName();
                
                // 跳过常见的虚拟接口名称
                if (name.startsWith("lo") || name.startsWith("docker") || 
                    name.startsWith("veth") || name.startsWith("br-")) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    if (address instanceof java.net.Inet4Address) {
                        String ip = address.getHostAddress();
                        
                        // 跳过回环地址和私有地址（用于公网环境）
                        if (!ip.startsWith("127.") && !ip.startsWith("169.254.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取本地IP地址失败", e);
        }
        
        return "127.0.0.1";
    }

    /**
     * 验证IP地址格式是否有效
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4格式验证
        String ipv4Regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (ip.matches(ipv4Regex)) {
            return true;
        }
        
        // IPv6格式验证（简化版本）
        String ipv6Regex = "^[0-9a-fA-F:]+$";
        return ip.contains(":") && ip.matches(ipv6Regex);
    }

    /**
     * 检查是否为内网IP地址
     */
    public static boolean isPrivateIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        // 私有IP范围检查
        return ip.startsWith("10.") ||
               ip.startsWith("172.") && ip.matches("^172\\.1[6-9]\\..*") ||
               ip.startsWith("172.") && ip.matches("^172\\.2[0-9]\\..*") ||
               ip.startsWith("172.") && ip.matches("^172\\.3[0-1]\\..*") ||
               ip.startsWith("192.168.") ||
               "127.0.0.1".equals(ip) ||
               "::1".equals(ip);
    }

    /**
     * 获取IP地址的地理位置（简化版本）
     */
    public static String getLocationByIp(String ip) {
        // 实现基于IP的地理位置查询
        // 推荐集成MaxMind GeoIP2、腾讯位置服务或其他地理位置API
        // 可以通过本地数据库或在线服务获取IP对应的地理信息
        if (isPrivateIp(ip)) {
            return "内网";
        }
        return "未知";
    }
}