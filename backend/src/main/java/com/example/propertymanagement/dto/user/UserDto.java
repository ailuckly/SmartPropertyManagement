package com.example.propertymanagement.dto.user;

import java.time.Instant;
import java.util.Set;

public record UserDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    Set<String> roles,
    Instant createdAt
) {
}
