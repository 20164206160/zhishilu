package com.zhishilu.service;

import com.zhishilu.exception.BusinessException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Slf4j
@Service
public class FileService {
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.allowed-types}")
    private String allowedTypes;
    
    @Value("${upload.max-size}")
    private Long maxSize;
    
    private List<String> allowedTypeList;
    private String absoluteUploadPath;
    
    @PostConstruct
    public void init() {
        allowedTypeList = Arrays.asList(allowedTypes.split(","));
        
        // 将相对路径转换为绝对路径
        File uploadDir = new File(uploadPath);
        absoluteUploadPath = uploadDir.getAbsolutePath();
        
        // 确保上传目录存在
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("创建上传目录: {}", absoluteUploadPath);
            }
        }
        log.info("文件上传路径: {}", absoluteUploadPath);
    }
    
    /**
     * 上传文件
     */
    public String upload(MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过10MB");
        }
        
        // 检查文件类型
        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        if (!allowedTypeList.contains(extension)) {
            throw new BusinessException("不支持的文件类型: " + extension);
        }
        
        // 生成文件名和路径 - 格式: yyyyMMdd/随机文件名.扩展名
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = dateDir + "/" + fileName;
        
        // 创建目录 - 使用绝对路径
        Path dirPath = Paths.get(absoluteUploadPath, dateDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.info("创建日期目录: {}", dirPath.toAbsolutePath());
        }
        
        // 保存文件 - 使用绝对路径
        Path filePath = Paths.get(absoluteUploadPath, relativePath);
        file.transferTo(filePath.toFile());
        
        log.info("文件上传成功: {}", relativePath);
        return relativePath;
    }
    
    /**
     * 删除文件
     */
    public void delete(String relativePath) throws IOException {
        Path filePath = Paths.get(absoluteUploadPath, relativePath);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("文件删除成功: {}", relativePath);
        }
    }
    
    /**
     * 获取文件绝对路径
     */
    public String getAbsolutePath(String relativePath) {
        return Paths.get(absoluteUploadPath, relativePath).toAbsolutePath().toString();
    }
}
