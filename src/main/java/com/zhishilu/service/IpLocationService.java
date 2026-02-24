package com.zhishilu.service;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * IP定位服务
 * 基于 ip2region 实现 IP 地址到地理位置的转换
 */
@Slf4j
@Service
public class IpLocationService {

    private Searcher searcher;

    private static final String XDB_FILE = "ip2region/ip2region_v4.xdb";

    /**
     * 初始化 ip2region 搜索器
     */
    @PostConstruct
    public void init() {
        try {
            // 尝试加载 ip2region 数据文件
            java.io.File file = new java.io.File(XDB_FILE);
            if (file.exists()) {
                byte[] cBuff = java.nio.file.Files.readAllBytes(file.toPath());
                searcher = Searcher.newWithBuffer(cBuff);
                log.info("IP定位服务初始化成功，数据文件: {}", file.getAbsolutePath());
            } else {
                // 尝试从类路径加载
                org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(XDB_FILE);
                if (resource.exists()) {
                    byte[] cBuff = resource.getInputStream().readAllBytes();
                    searcher = Searcher.newWithBuffer(cBuff);
                    log.info("IP定位服务初始化成功（类路径）");
                } else {
                    log.warn("未找到 ip2region 数据文件: {}，IP定位功能将不可用", XDB_FILE);
                }
            }
        } catch (IOException e) {
            log.error("IP定位服务初始化失败", e);
        }
    }

    // 缓存服务器公网IP，避免重复获取
    private String serverPublicIp;

    /**
     * 根据 IP 地址获取地理位置
     *
     * @param ip IP地址
     * @return 格式：省 市 区/县，如 "北京市 北京市 朝阳区"
     */
    public String getLocation(String ip) {
        if (searcher == null) {
            log.warn("IP定位服务未初始化");
            return null;
        }

        // 判断是否为本地地址
        boolean isLocalIp = isLocalAddress(ip);

        // 如果是本地地址，尝试使用服务器公网IP查询
        if (isLocalIp) {
            String publicIp = getServerPublicIp();
            if (publicIp != null) {
                log.debug("本地IP[{}]，使用服务器公网IP[{}]查询位置", ip, publicIp);
                ip = publicIp;
            } else {
                log.warn("无法获取服务器公网IP，返回本地网络");
                return "本地网络";
            }
        }

        try {
            long sTime = System.nanoTime();
            String region = searcher.search(ip);
            long cost = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - sTime);
            log.debug("IP定位查询: ip={}, region={}, cost={}μs", ip, region, cost);

            return formatLocation(region);
        } catch (Exception e) {
            log.error("IP定位查询失败: ip={}", ip, e);
            return null;
        }
    }

    /**
     * 判断是否为本地地址
     */
    private boolean isLocalAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        return ip.equals("127.0.0.1") || ip.equals("localhost") ||
               ip.startsWith("192.168.") || ip.startsWith("10.") ||
               ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") || ip.startsWith("172.31.") ||
               ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }

    /**
     * 获取服务器公网IP
     * 通过访问外部服务获取
     */
    private String getServerPublicIp() {
        // 如果已缓存，直接返回
        if (serverPublicIp != null) {
            return serverPublicIp;
        }

        // 尝试多个服务获取公网IP（优先使用直接返回IP的服务）
        String[] ipServices = {
            "https://api64.ipify.org",
            "https://httpbin.org/ip",
            "https://api.ipify.org"
        };

        for (String service : ipServices) {
            try {
                java.net.URL url = new java.net.URL(service);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    // 读取完整响应（处理多行JSON）
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    if (!response.isEmpty()) {
                        // 处理不同服务的返回格式
                        String ip = extractIpFromResponse(response, service);
                        if (ip != null && !ip.isEmpty() && !isLocalAddress(ip)) {
                            serverPublicIp = ip;
                            log.info("获取服务器公网IP成功: {} (via {})", ip, service);
                            return ip;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("从 {} 获取公网IP失败: {}", service, e.getMessage());
            }
        }

        log.warn("无法从任何服务获取服务器公网IP");
        return null;
    }

    /**
     * 从响应中提取IP地址
     */
    private String extractIpFromResponse(String response, String service) {
        try {
            String trimmed = response.trim();
            if (service.contains("ipify.org")) {
                // ipify 直接返回IP
                return trimmed;
            } else if (service.contains("httpbin.org")) {
                // httpbin 返回 JSON: {"origin": "xxx.xxx.xxx.xxx"}
                // 使用正则表达式提取IP
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    "\"origin\"\\s*:\\s*\"([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})\""
                );
                java.util.regex.Matcher matcher = pattern.matcher(trimmed);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            // 尝试直接匹配IP地址格式
            java.util.regex.Pattern ipPattern = java.util.regex.Pattern.compile(
                "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})"
            );
            java.util.regex.Matcher ipMatcher = ipPattern.matcher(trimmed);
            if (ipMatcher.find()) {
                return ipMatcher.group(1);
            }
            return trimmed;
        } catch (Exception e) {
            log.debug("解析IP响应失败: {}", response);
            return null;
        }
    }

    /**
     * 格式化位置信息
     * ip2region v4.xdb 返回格式：国家|省份|城市|ISP|iso-alpha2-code
     * 例如：中国|广东省|深圳市|电信|CN
     * 转换为：省 市
     */
    private String formatLocation(String region) {
        if (region == null || region.isEmpty() || region.contains("未分配")) {
            return null;
        }

        String[] parts = region.split("\\|");
        log.debug("IP region原始数据: {}, 分段数: {}", region, parts.length);

        if (parts.length < 3) {
            return region.replace("|", " ").trim();
        }

        // ip2region v4.xdb 格式: 国家|省份|城市|ISP|iso-alpha2-code
        // parts[0]=国家, parts[1]=省份, parts[2]=城市, parts[3]=ISP, parts[4]=国家代码
        StringBuilder sb = new StringBuilder();

        // 省份 (parts[1])
        String province = parts[1].trim();
        if (!province.isEmpty() && !"0".equals(province)) {
            sb.append(province);
        }

        // 城市 (parts[2])
        String city = parts[2].trim();
        if (!city.isEmpty() && !"0".equals(city)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(city);
        }

        // 如果省份和城市相同（如北京、上海等直辖市），只保留一个
        String result = sb.toString();
        if (result.contains("北京") || result.contains("上海") ||
            result.contains("天津") || result.contains("重庆")) {
            String[] locParts = result.split(" ");
            if (locParts.length >= 2 && locParts[0].equals(locParts[1])) {
                return locParts[0];
            }
        }

        return result.isEmpty() ? region.replace("|", " ").trim() : result;
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        if (searcher != null) {
            try {
                searcher.close();
                log.info("IP定位服务已关闭");
            } catch (IOException e) {
                log.error("关闭IP定位服务失败", e);
            }
        }
    }
}
