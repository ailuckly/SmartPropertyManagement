package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Lease;
import com.example.propertymanagement.model.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    Page<Lease> findAll(Pageable pageable);

    Page<Lease> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Lease> findAllByOwnerId(Long ownerId, Pageable pageable);

    // 统计查询方法
    Long countByOwnerId(Long ownerId);
    
    Long countByTenantId(Long tenantId);
    
    List<Lease> findByEndDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, LeaseStatus status);
    
    List<Lease> findByOwnerIdAndEndDateBetweenAndStatus(Long ownerId, LocalDate startDate, LocalDate endDate, LeaseStatus status);
    
    List<Lease> findTop5ByOrderByIdDesc();
    
    List<Lease> findTop5ByOwnerIdOrderByIdDesc(Long ownerId);
    
    List<Lease> findTop5ByTenantIdOrderByIdDesc(Long tenantId);
    
    /**
     * 搜索租约（支持租户姓名、物业地址关键词搜索）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的租约列表
     */
    @Query("SELECT l FROM Lease l WHERE " +
           "LOWER(l.tenantUsername) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.propertyAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Lease> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
