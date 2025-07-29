package com.zdd.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP地址工具类
 * 支持从HTTP请求中获取客户端真实IP（兼容代理、负载均衡）
 */
public class IpUtils {

    // 常见代理服务器转发的IP头字段（按优先级排序）
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",      // 最常用，可能包含多个IP（逗号分隔）
            "Proxy-Client-IP",      // Apache代理
            "WL-Proxy-Client-IP",   // WebLogic代理
            "HTTP_X_FORWARDED_FOR", // Nginx代理
            "HTTP_X_FORWARDED",     // 其他代理
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "REMOTE_ADDR"           // 最后回退到直接连接的IP
    };


    //        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

    /**
     * 从HttpServletRequest中获取客户端真实IP
     * @param request HTTP请求对象
     * @return 真实IP地址（IPv4或IPv6）
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0"; // 默认值（实际使用时建议抛异常或返回null）
        }

        // 遍历所有可能的IP头字段
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 处理X-Forwarded-For可能包含多个IP的情况（取第一个非unknown的IP）
                if (header.equals("X-Forwarded-For")) {
                    ip = extractFirstValidIp(ip);
                }
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }

        // 回退到直接连接的IP
        return request.getRemoteAddr();
    }

    /**
     * 从X-Forwarded-For中提取第一个有效IP（可能包含多个IP，如：192.168.1.1, 10.0.0.1）
     * @param xForwardedFor X-Forwarded-For头内容
     * @return 第一个有效IP
     */
    private static String extractFirstValidIp(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isEmpty()) {
            return "0.0.0.0";
        }

        // 按逗号分割IP列表
        String[] ips = xForwardedFor.split(",");
        for (String ip : ips) {
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp)) {
                return trimmedIp;
            }
        }

        return "0.0.0.0"; // 默认值
    }

    /**
     * 验证IP地址是否合法（支持IPv4和IPv6）
     * @param ip IP地址字符串
     * @return 是否合法
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        try {
            // 使用InetAddress验证IP格式（兼容IPv4和IPv6）
            InetAddress inetAddress = InetAddress.getByName(ip);
            return !inetAddress.isAnyLocalAddress()
                    && !inetAddress.isLoopbackAddress()
                    && !inetAddress.isLinkLocalAddress()
                    && !inetAddress.isMulticastAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 获取本地主机IP（非127.0.0.1）
     * @return 本地非回环IP
     */
    public static String getLocalHostIp() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost.getHostAddress();
            }

            // 如果回环地址，尝试获取其他非回环IP
            InetAddress[] allInetAddresses = InetAddress.getAllByName(localHost.getHostName());
            for (InetAddress inetAddress : allInetAddresses) {
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            // 忽略异常
        }
        return "127.0.0.1"; // 默认回退
    }
}