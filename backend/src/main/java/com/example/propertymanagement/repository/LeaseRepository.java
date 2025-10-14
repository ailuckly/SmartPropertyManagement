package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Lease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
    
    List<Lease> findByEndDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, Lease.Status status);
    
    List<Lease> findByProperty_OwnerIdAndEndDateBetweenAndStatus(Long ownerId, LocalDate startDate, LocalDate endDate, Lease.Status status);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByOrderByIdDesc();
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByProperty_OwnerIdOrderByIdDesc(Long ownerId);
    
    @EntityGraph(attributePaths = {"property", "tenant"})
    List<Lease> findTop5ByTenantIdOrderByIdDesc(Long tenantId);
}
