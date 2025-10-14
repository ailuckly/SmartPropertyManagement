package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findAllByOwnerId(Long ownerId, Pageable pageable);
    
    // 统计查询方法
    Long countByOwnerId(Long ownerId);
    
    List<Property> findByOwnerId(Long ownerId);
}
