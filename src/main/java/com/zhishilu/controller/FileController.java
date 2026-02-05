package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.resp.FileUploadResp;
import com.zhishilu.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<FileUploadResp> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileService.upload(file);
        FileUploadResp resp = new FileUploadResp();
        resp.setPath(path);
        return Result.success("上传成功", resp);
    }
    
    /**
     * 上传多个文件
     */
    @PostMapping("/upload/batch")
    public Result<FileUploadResp> uploadBatch(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            paths.add(fileService.upload(file));
        }
        FileUploadResp resp = new FileUploadResp();
        resp.setPaths(paths);
        return Result.success("上传成功", resp);
    }
    
    /**
     * 获取文件
     */
    @GetMapping("/img/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) throws IOException {
        // 获取请求路径中 /img/ 之后的部分
        String requestPath = request.getRequestURI();
        log.info("请求图片 URI: {}", requestPath);
        
        String imagePath = requestPath.substring(requestPath.indexOf("/img/") + 5);
        log.info("解析图片路径: {}", imagePath);
        
        String absolutePath = fileService.getAbsolutePath(imagePath);
        log.info("图片绝对路径: {}", absolutePath);
        
        Path filePath = Paths.get(absolutePath);
        
        if (!Files.exists(filePath)) {
            log.warn("图片文件不存在: {}", absolutePath);
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(filePath);
        String contentType = Files.probeContentType(filePath);
        
        log.info("返回图片，类型: {}", contentType);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .body(resource);
    }
    
    /**
     * 下载文件（保留旧接口兼容性）
     */
    @GetMapping("/download/**")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {
        String absolutePath = fileService.getAbsolutePath(path);
        Path filePath = Paths.get(absolutePath);
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(filePath);
        String contentType = Files.probeContentType(filePath);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }
}
