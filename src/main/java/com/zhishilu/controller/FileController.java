package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileService.upload(file);
        return Result.success("上传成功", path);
    }
    
    /**
     * 上传多个文件
     */
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            paths.add(fileService.upload(file));
        }
        return Result.success("上传成功", paths);
    }
    
    /**
     * 获取文件
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
