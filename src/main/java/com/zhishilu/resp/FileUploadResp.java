package com.zhishilu.resp;

import lombok.Data;

import java.util.List;

/**
 * 文件上传响应对象
 */
@Data
public class FileUploadResp {
    
    /**
     * 文件路径（单文件上传）
     */
    private String path;
    
    /**
     * 文件路径列表（批量上传）
     */
    private List<String> paths;
}
