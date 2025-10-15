package com.example.propertymanagement.service;

import com.example.propertymanagement.config.FileStorageConfig;
import com.example.propertymanagement.model.File;
import com.example.propertymanagement.model.FileCategory;
import com.example.propertymanagement.model.Property;
import com.example.propertymanagement.repository.FileRepository;
import com.example.propertymanagement.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文件管理服务类
 * 提供文件上传、下载、删除、查询等核心功能
 * 
 * @author Development Team
 * @since 2025-10-14
 */
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final FileRepository fileRepository;
    private final FileStorageConfig fileStorageConfig;
    private final PropertyRepository propertyRepository;
    
    /**
     * 文件上传
     * 
     * @param multipartFile 上传的文件
     * @param category 文件分类
     * @param entityId 关联实体ID（可选）
     * @param userId 上传用户ID
     * @param description 文件描述（可选）
     * @return 保存的文件对象
     * @throws IOException 文件操作异常
     */
    @Transactional
    public File uploadFile(MultipartFile multipartFile, FileCategory category, 
                          Long entityId, Long userId, String description) throws IOException {
        
        // 验证文件是否为空
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        
        // 获取原始文件名
        String originalFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        
        // 验证文件名
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法字符: " + originalFileName);
        }
        
        // 验证文件类型
        String contentType = multipartFile.getContentType();
        validateFileType(contentType, category);
        
        // 验证文件大小
        validateFileSize(multipartFile.getSize(), contentType);
        
        // 生成唯一的存储文件名
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 根据分类创建子目录
        String subDir = getCategoryDirectory(category);
        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir(), subDir);
        
        // 确保目录存在
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件到磁盘
        Path targetLocation = uploadPath.resolve(storedFileName);
        Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // 创建文件记录
        File file = new File();
        file.setOriginalFileName(originalFileName);
        file.setStoredFileName(storedFileName);
        file.setFilePath(subDir + "/" + storedFileName);
        file.setFileType(contentType);
        file.setFileSize(multipartFile.getSize());
        file.setCategory(category);
        file.setEntityId(entityId);
        file.setDescription(description);
        file.setUploadedBy(userId);
        file.setUploadedAt(LocalDateTime.now());
        
        // 保存到数据库
        return fileRepository.save(file);
    }
    
    /**
     * 根据文件ID获取文件
     * 
     * @param fileId 文件ID
     * @return 文件对象
     */
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在，ID: " + fileId));
    }
    
    /**
     * 根据存储文件名获取文件
     * 
     * @param storedFileName 存储文件名
     * @return 文件对象
     */
    public File getFileByStoredName(String storedFileName) {
        return fileRepository.findByStoredFileName(storedFileName)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + storedFileName));
    }
    
    /**
     * 根据存储文件名加载文件资源
     * 
     * @param storedFileName 存储文件名
     * @return 文件资源
     * @throws MalformedURLException URL格式异常
     */
    public Resource loadFileAsResource(String storedFileName) throws MalformedURLException {
        File file = fileRepository.findByStoredFileName(storedFileName)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + storedFileName));
        
        Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(file.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IllegalArgumentException("文件不存在或无法读取: " + storedFileName);
        }
    }
    
    /**
     * 根据分类和关联实体ID获取文件列表
     * 
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件列表
     */
    public List<File> getFilesByEntity(FileCategory category, Long entityId) {
        return fileRepository.findByCategoryAndEntityIdOrderByUploadedAtDesc(category, entityId);
    }
    
    /**
     * 获取封面图片
     * 
     * @param entityId 关联实体ID（物业ID）
     * @return 封面图片对象，如果不存在返回null
     */
    public File getCoverImage(Long entityId) {
        return fileRepository.findByCategoryAndEntityIdAndIsCover(
                FileCategory.PROPERTY_IMAGE, entityId, true
        ).orElse(null);
    }
    
    /**
     * 设置封面图片
     * 
     * @param fileId 文件ID
     * @param entityId 关联实体ID
     */
    @Transactional
    public void setCoverImage(Long fileId, Long entityId) {
        // 取消当前封面
        File currentCover = getCoverImage(entityId);
        if (currentCover != null) {
            currentCover.setIsCover(false);
            fileRepository.save(currentCover);
        }
        
        // 设置新封面
        File newCover = getFile(fileId);
        if (!newCover.getCategory().equals(FileCategory.PROPERTY_IMAGE)) {
            throw new IllegalArgumentException("只有物业图片可以设置为封面");
        }
        if (!newCover.getEntityId().equals(entityId)) {
            throw new IllegalArgumentException("文件不属于该物业");
        }
        
        newCover.setIsCover(true);
        fileRepository.save(newCover);
        
        // 同步更新 Property 表的 coverImagePath 字段
        Property property = propertyRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("物业不存在"));
        property.setCoverImagePath(newCover.getStoredFileName());
        propertyRepository.save(property);
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param userId 操作用户ID（用于权限验证）
     * @throws IOException 文件删除异常
     */
    @Transactional
    public void deleteFile(Long fileId, Long userId) throws IOException {
        File file = getFile(fileId);
        
        // 删除磁盘文件
        Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(file.getFilePath()).normalize();
        Files.deleteIfExists(filePath);
        
        // 删除数据库记录
        fileRepository.delete(file);
    }
    
    /**
     * 删除关联实体的所有文件
     * 
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @throws IOException 文件删除异常
     */
    @Transactional
    public void deleteFilesByEntity(FileCategory category, Long entityId) throws IOException {
        List<File> files = getFilesByEntity(category, entityId);
        
        for (File file : files) {
            // 删除磁盘文件
            Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(file.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        }
        
        // 批量删除数据库记录
        fileRepository.deleteAll(files);
    }
    
    /**
     * 统计文件数量
     * 
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件数量
     */
    public long countFiles(FileCategory category, Long entityId) {
        return fileRepository.countByCategoryAndEntityId(category, entityId);
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 验证文件类型
     */
    private void validateFileType(String contentType, FileCategory category) {
        if (contentType == null) {
            throw new IllegalArgumentException("无法识别文件类型");
        }
        
        boolean isValid = false;
        
        // 根据分类验证文件类型
        if (category == FileCategory.PROPERTY_IMAGE || category == FileCategory.MAINTENANCE_IMAGE 
                || category == FileCategory.USER_AVATAR) {
            // 验证图片类型
            for (String allowedType : fileStorageConfig.getAllowedImageTypes()) {
                if (contentType.equals(allowedType)) {
                    isValid = true;
                    break;
                }
            }
        } else if (category == FileCategory.LEASE_CONTRACT) {
            // 验证文档类型
            for (String allowedType : fileStorageConfig.getAllowedDocumentTypes()) {
                if (contentType.equals(allowedType)) {
                    isValid = true;
                    break;
                }
            }
        } else {
            // OTHER分类允许所有类型
            isValid = true;
        }
        
        if (!isValid) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }
    
    /**
     * 验证文件大小
     */
    private void validateFileSize(long fileSize, String contentType) {
        long maxSize;
        
        if (contentType.startsWith("image/")) {
            maxSize = fileStorageConfig.getMaxImageSize();
        } else {
            maxSize = fileStorageConfig.getMaxDocumentSize();
        }
        
        if (fileSize > maxSize) {
            throw new IllegalArgumentException(
                    String.format("文件大小超过限制。当前: %d 字节，最大: %d 字节", fileSize, maxSize)
            );
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
    
    /**
     * 根据分类获取存储子目录
     */
    private String getCategoryDirectory(FileCategory category) {
        switch (category) {
            case PROPERTY_IMAGE:
                return "properties";
            case LEASE_CONTRACT:
                return "contracts";
            case MAINTENANCE_IMAGE:
                return "maintenance";
            case USER_AVATAR:
                return "avatars";
            default:
                return "others";
        }
    }
}
