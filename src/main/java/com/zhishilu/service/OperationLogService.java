package com.zhishilu.service;

import com.zhishilu.entity.OperationLog;
import com.zhishilu.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {
    
    private final OperationLogRepository operationLogRepository;
    
    /**
     * 异步保存操作日志
     */
    @Async
    public void saveLog(String userId, String username, String path, String method, 
                        String params, String ip, Integer statusCode, Long executionTime) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setId(UUID.randomUUID().toString());
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setPath(path);
            operationLog.setMethod(method);
            operationLog.setParams(params);
            operationLog.setIp(ip);
            operationLog.setStatusCode(statusCode);
            operationLog.setExecutionTime(executionTime);
            operationLog.setOperationTime(LocalDateTime.now());
            
            operationLogRepository.save(operationLog);
            log.info("保存操作日志: {} {} {}", username, method, path);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
