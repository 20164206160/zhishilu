package com.zhishilu.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * HEIC/HEIF 图片转换工具（依赖系统 ImageMagick convert 命令）
 */
public class HeicConvertUtil {

    private HeicConvertUtil() {}

    /**
     * 判断文件是否为 HEIC/HEIF 格式
     */
    public static boolean isHeicLike(MultipartFile file, String fileName) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String lowerName = fileName == null ? "" : fileName.toLowerCase();

        return contentType.contains("heic")
                || contentType.contains("heif")
                || lowerName.endsWith(".heic")
                || lowerName.endsWith(".heif");
    }

    /**
     * 调用 ImageMagick 将 HEIC/HEIF 转换为 JPG
     *
     * @param inputPath  原始 HEIC 文件路径
     * @param outputPath 输出 JPG 文件路径
     */
    public static void convertToJpg(Path inputPath, Path outputPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "convert",
                inputPath.toAbsolutePath().toString(),
                outputPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("HEIC 转 JPG 失败，exitCode=" + exitCode + ", output=" + output);
        }
    }
}
