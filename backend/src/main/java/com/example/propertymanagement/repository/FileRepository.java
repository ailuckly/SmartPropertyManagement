package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.File;
import com.example.propertymanagement.model.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件数据访问接口
 * 提供文件的数据库操作方法
 * 
 * @author Development Team
 * @since 2025-10-14
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    /**
     * 根据存储文件名查询文件
     * @param storedFileName 存储文件名
     * @return 文件对象
     */
    Optional<File> findByStoredFileName(String storedFileName);
    
    /**
     * 根据分类和关联实体ID查询文件列表
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件列表
     */
    List<File> findByCategoryAndEntityId(FileCategory category, Long entityId);
    
    /**
     * 根据分类和关联实体ID查询文件列表（按上传时间降序）
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件列表
     */
    List<File> findByCategoryAndEntityIdOrderByUploadedAtDesc(FileCategory category, Long entityId);
    
    /**
     * 根据分类查询文件列表
     * @param category 文件分类
     * @return 文件列表
     */
    List<File> findByCategory(FileCategory category);
    
    /**
     * 根据上传用户ID查询文件列表
     * @param uploadedBy 上传用户ID
     * @return 文件列表
     */
    List<File> findByUploadedBy(Long uploadedBy);
    
    /**
     * 根据分类和关联实体ID查询封面图片
     * @param category 文件分类（通常为PROPERTY_IMAGE）
     * @param entityId 关联实体ID
     * @param isCover 是否为封面
     * @return 封面图片对象
     */
    Optional<File> findByCategoryAndEntityIdAndIsCover(FileCategory category, Long entityId, Boolean isCover);
    
    /**
     * 根据关联实体ID删除所有文件
     * @param entityId 关联实体ID
     */
    void deleteByEntityId(Long entityId);
    
    /**
     * 统计指定分类和关联实体的文件数量
     * @param category 文件分类
     * @param entityId 关联实体ID
     * @return 文件数量
     */
    long countByCategoryAndEntityId(FileCategory category, Long entityId);
}
