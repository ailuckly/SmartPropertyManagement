package com.example.propertymanagement.service;

import com.example.propertymanagement.config.JwtProperties;
import com.example.propertymanagement.dto.auth.AuthResponse;
import com.example.propertymanagement.dto.auth.LoginRequest;
import com.example.propertymanagement.dto.auth.RegisterRequest;
import com.example.propertymanagement.exception.BadRequestException;
import com.example.propertymanagement.exception.UnauthorizedException;
import com.example.propertymanagement.mapper.UserMapper;
import com.example.propertymanagement.model.RefreshToken;
import com.example.propertymanagement.model.Role;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.RefreshTokenRepository;
import com.example.propertymanagement.repository.RoleRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.JwtTokenProvider;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.CookieUtils;
import com.example.propertymanagement.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("邮箱已被注册");
        }

        RoleName roleName = request.role() != null ? request.role() : RoleName.ROLE_TENANT;
        Role role = roleRepository.findByName(roleName)
            .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .phoneNumber(request.phoneNumber())
            .roles(Set.of(role))
            .build();

        userRepository.save(user);

        attachTokensToResponse(user.getUsername(), user, httpRequest, httpResponse);

        return new AuthResponse("注册成功", UserMapper.toDto(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("用户不存在"));

            attachTokensToResponse(principal.getUsername(), user, httpRequest, httpResponse);

            return new AuthResponse("登录成功", UserMapper.toDto(user));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("用户名或密码错误");
        }
    }

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = extractRefreshToken(request);
        if (refreshTokenValue == null) {
            throw new UnauthorizedException("缺少刷新令牌");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new UnauthorizedException("刷新令牌无效"));

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteById(storedToken.getId());
            throw new UnauthorizedException("刷新令牌已过期");
        }

        User user = storedToken.getUser();
        attachTokensToResponse(user.getUsername(), user, request, response);

        return new AuthResponse("令牌刷新成功", UserMapper.toDto(user));
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityUtils.getCurrentUserPrincipal().ifPresent(principal ->
            refreshTokenRepository.deleteByUserId(principal.getId())
        );

        String refreshTokenValue = extractRefreshToken(request);
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> refreshTokenRepository.deleteById(token.getId()));
        }

        boolean secure = request.isSecure();
        CookieUtils.deleteCookie(response, jwtProperties.getAccessCookieName(), secure);
        CookieUtils.deleteCookie(response, jwtProperties.getRefreshCookieName(), secure);
    }

    @Transactional(readOnly = true)
    public com.example.propertymanagement.dto.user.UserDto me() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
            .orElseThrow(() -> new UnauthorizedException("未登录"));
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new UnauthorizedException("用户不存在"));
        return UserMapper.toDto(user);
    }

    private void attachTokensToResponse(String username,
                                        User user,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(username);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        Instant refreshExpiry = jwtTokenProvider.getRefreshTokenExpiryInstant();
        refreshTokenRepository.findByUserId(user.getId())
            .ifPresent(existing -> refreshTokenRepository.deleteById(existing.getId()));

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(refreshTokenValue)
            .expiryDate(refreshExpiry)
            .build();
        refreshTokenRepository.save(refreshToken);

        boolean secure = request.isSecure();
        CookieUtils.addCookie(
            response,
            jwtProperties.getAccessCookieName(),
            accessToken,
            Duration.ofMinutes(jwtProperties.getAccessTokenExpirationMinutes()),
            secure
        );
        CookieUtils.addCookie(
            response,
            jwtProperties.getRefreshCookieName(),
            refreshTokenValue,
            Duration.ofDays(jwtProperties.getRefreshTokenExpirationDays()),
            secure
        );
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (jwtProperties.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
