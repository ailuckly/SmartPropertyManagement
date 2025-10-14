package com.example.propertymanagement.config;

import com.example.propertymanagement.model.Role;
import com.example.propertymanagement.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // 初始化系统角色
        List<String> roleNames = Arrays.asList("ROLE_ADMIN", "ROLE_OWNER", "ROLE_TENANT");
        
        roleNames.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder()
                    .name(roleName)
                    .isDeleted(0)
                    .build());
            }
        });
    }
}
