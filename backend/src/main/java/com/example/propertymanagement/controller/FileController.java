package com.example.propertymanagement.controller;

import com.example.propertymanagement.model.File;
import com.example.propertymanagement.model.FileCategory;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 提供文件上传、下载、删除、查询等REST API接口
 * 
 * @author Development Team
 * @since 2025-10-14
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param category 文件分类
     * @param entityId 关联实体ID（可选）
     * @param description 文件描述（可选）
     * @param currentUser 当前登录用户
     * @return 上传成功的文件信息
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            // 转换分类字符串为枚举
            FileCategory fileCategory = FileCategory.valueOf(category.toUpperCase());
            
            // 上传文件
            File uploadedFile = fileService.uploadFile(
                    file, 
                    fileCategory, 
                    entityId, 
                    currentUser.getId(), 
                    description
            );
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("file", uploadedFile);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 下载/访问文件
     * 
     * @param fileName 存储文件名
     * @return 文件资源
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileService.loadFileAsResource(fileName);
            
            // 获取文件信息用于设置响应头
            File fileInfo = fileService.getFile(
                    Long.parseLong(fileName.substring(0, fileName.lastIndexOf('.')))
            );
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 在线预览文件（图片、PDF等）
     * 
     * @param fileName 存储文件名
     * @return 文件资源（inline方式）
     */
    @GetMapping("/preview/{fileName:.+}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
        try {
            Resource resource = fileService.loadFileAsResource(fileName);
            
            // 获取文件类型
            File fileInfo = fileService.getFile(
                    Long.parseLong(fileName.substring(0, fileName.lastIndexOf('.')))
            );
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取文件详情
     * 
     * @param fileId 文件ID
     * @return 文件详细信息
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<File> getFile(@PathVariable Long fileId) {
        try {
            File file = fileService.getFile(fileId);
            return ResponseEntity.ok(file);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取关联实体的所有文件
     * 
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件列表
     */
    @GetMapping("/entity/{category}/{entityId}")
    public ResponseEntity<List<File>> getFilesByEntity(
            @PathVariable String category,
            @PathVariable Long entityId) {
        
        try {
            FileCategory fileCategory = FileCategory.valueOf(category.toUpperCase());
            List<File> files = fileService.getFilesByEntity(fileCategory, entityId);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取物业封面图片
     * 
     * @param propertyId 物业ID
     * @return 封面图片信息
     */
    @GetMapping("/cover/{propertyId}")
    public ResponseEntity<File> getCoverImage(@PathVariable Long propertyId) {
        File coverImage = fileService.getCoverImage(propertyId);
        if (coverImage != null) {
            return ResponseEntity.ok(coverImage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 设置封面图片
     * 
     * @param fileId 文件ID
     * @param propertyId 物业ID
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @PutMapping("/{fileId}/set-cover")
    public ResponseEntity<Map<String, Object>> setCoverImage(
            @PathVariable Long fileId,
            @RequestParam Long propertyId,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            fileService.setCoverImage(fileId, propertyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "封面设置成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param currentUser 当前用户
     * @return 操作结果
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            fileService.deleteFile(fileId, currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件删除成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 统计文件数量
     * 
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件数量
     */
    @GetMapping("/count/{category}/{entityId}")
    public ResponseEntity<Map<String, Object>> countFiles(
            @PathVariable String category,
            @PathVariable Long entityId) {
        
        try {
            FileCategory fileCategory = FileCategory.valueOf(category.toUpperCase());
            long count = fileService.countFiles(fileCategory, entityId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
