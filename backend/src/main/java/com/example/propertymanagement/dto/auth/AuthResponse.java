package com.example.propertymanagement.dto.auth;

import com.example.propertymanagement.dto.user.UserDto;

public record AuthResponse(String message, UserDto user) {
}
