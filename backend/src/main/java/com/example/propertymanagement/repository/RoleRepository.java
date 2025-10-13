package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Role;
import com.example.propertymanagement.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
