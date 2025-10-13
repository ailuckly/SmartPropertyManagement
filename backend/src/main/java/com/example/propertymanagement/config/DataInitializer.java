package com.example.propertymanagement.config;

import com.example.propertymanagement.model.Role;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        Arrays.stream(RoleName.values())
            .forEach(roleName -> roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build())));
    }
}
