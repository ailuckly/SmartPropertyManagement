package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findAllByOwnerId(Long ownerId, Pageable pageable);
    
    // 统计查询方法
    Long countByOwnerId(Long ownerId);
    
    List<Property> findByOwnerId(Long ownerId);
    
    /**
     * 搜索物业（支持地址、城市、邮编关键词搜索）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的物业列表
     */
    @Query("SELECT p FROM Property p WHERE " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.zipCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Property> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 搜索指定业主的物业
     * @param ownerId 业主ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的物业列表
     */
    @Query("SELECT p FROM Property p WHERE p.ownerId = :ownerId AND (" +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.zipCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Property> searchByOwnerIdAndKeyword(@Param("ownerId") Long ownerId, 
                                              @Param("keyword") String keyword, 
                                              Pageable pageable);
                                              
    /**
     * 批量删除指定ID的物业
     * @param ids 物业ID列表
     */
    void deleteAllByIdIn(List<Long> ids);
    
    /**
     * 高级筛选查询物业
     */
    @Query("SELECT p FROM Property p WHERE " +
           "(:ownerId IS NULL OR p.ownerId = :ownerId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:propertyType IS NULL OR p.propertyType = :propertyType) AND " +
           "(:minRent IS NULL OR p.rentAmount >= :minRent) AND " +
           "(:maxRent IS NULL OR p.rentAmount <= :maxRent) AND " +
           "(:minBedrooms IS NULL OR p.bedrooms >= :minBedrooms) AND " +
           "(:maxBedrooms IS NULL OR p.bedrooms <= :maxBedrooms) AND " +
           "(:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    Page<Property> findPropertiesWithFilters(
        @Param("ownerId") Long ownerId,
        @Param("status") String status,
        @Param("propertyType") String propertyType,
        @Param("minRent") Double minRent,
        @Param("maxRent") Double maxRent,
        @Param("minBedrooms") Integer minBedrooms,
        @Param("maxBedrooms") Integer maxBedrooms,
        @Param("city") String city,
        Pageable pageable);
}
