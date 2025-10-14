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

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAllByTenantId(Long tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    Page<Lease> findAllByPropertyOwnerId(Long ownerId, Pageable pageable);

    // 统计查询方法
    Long countByProperty_OwnerId(Long ownerId);
    
    Long countByTenantId(Long tenantId);
    
    List<Lease> findByEndDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, LeaseStatus status);
    
    List<Lease> findByProperty_OwnerIdAndEndDateBetweenAndStatus(Long ownerId, LocalDate startDate, LocalDate endDate, LeaseStatus status);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByOrderByIdDesc();
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByProperty_OwnerIdOrderByIdDesc(Long ownerId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByTenantIdOrderByIdDesc(Long tenantId);
    
    /**
     * 搜索租约（支持租户姓名、物业地址关键词搜索）
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的租约列表
     */
    @EntityGraph(attributePaths = {"property", "property.owner", "tenant"})
    @Query("SELECT l FROM Lease l JOIN l.property p JOIN l.tenant t " +
           "WHERE LOWER(t.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Lease> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
