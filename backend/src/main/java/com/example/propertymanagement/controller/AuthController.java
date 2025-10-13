package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.auth.AuthResponse;
import com.example.propertymanagement.dto.auth.LoginRequest;
import com.example.propertymanagement.dto.auth.RegisterRequest;
import com.example.propertymanagement.dto.user.UserDto;
import com.example.propertymanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoints for the authentication workflow. All heavy lifting is delegated to {@link AuthService}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new account and immediately authenticates the session.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        return ResponseEntity.status(201).body(authService.register(request, httpRequest, httpResponse));
    }

    /**
     * Performs login using username + password and returns the authenticated profile.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
    }

    /**
     * Uses the refresh token inside cookies to generate a new access token pair.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }

    /**
     * Deletes refresh-token persistence and clears authentication cookies.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(Map.of("message", "已注销"));
    }

    /**
     * Returns the current authenticated user. Convenience endpoint for the SPA bootstrap.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        return ResponseEntity.ok(authService.me());
    }
}
