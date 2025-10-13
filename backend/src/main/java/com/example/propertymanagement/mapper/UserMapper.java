package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.user.UserDto;
import com.example.propertymanagement.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            roles,
            user.getCreatedAt()
        );
    }
}
