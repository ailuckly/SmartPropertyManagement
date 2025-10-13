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

/**
 * 认证服务：封装注册、登录、刷新令牌、注销等全流程逻辑。
 * <p>
 * - 负责协调用户/角色持久化、密码加密、JWT 签发与 HttpOnly Cookie 管理；
 * - 统一保证刷新令牌与客户端 Cookies 状态一致，防止出现“服务端已失效但客户端仍携带旧 cookie”的安全风险；
 * - 所有对外方法均带事务，确保认证相关数据的一致性。
 */
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

    /**
     * 用户注册：创建帐号、自动登录并返回会话信息。
     *
     * @param request       注册表单
     * @param httpRequest   原始 HTTP 请求（用于检测 HTTPS 等信息）
     * @param httpResponse  原始 HTTP 响应（用于写入 HttpOnly Cookie）
     * @return 注册后构建的 {@link AuthResponse}，包含用户信息
     * @throws BadRequestException 用户名或邮箱已存在时抛出
     */
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

    /**
     * 用户登录：校验凭证、生成访问令牌 / 刷新令牌并返回会话信息。
     *
     * @param request       登录表单
     * @param httpRequest   原始 HTTP 请求
     * @param httpResponse  原始 HTTP 响应
     * @return 登录成功后的 {@link AuthResponse}
     * @throws UnauthorizedException 当用户名/密码错误时抛出
     */
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

    /**
     * 刷新令牌：通过 HttpOnly Cookie 中携带的刷新令牌获取新的访问令牌。
     *
     * @param request  原始请求（包含 Cookie）
     * @param response 用于写入新的 Cookie
     * @return 刷新后的用户会话
     * @throws UnauthorizedException 当刷新令牌缺失、无效或过期时抛出
     */
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

    /**
     * 注销：移除数据库中的刷新令牌记录并清除客户端的访问/刷新 Cookie。
     *
     * @param request  原始请求
     * @param response 原始响应
     */
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

    /**
     * 查询当前登录用户。
     *
     * @return 登录用户的 DTO
     * @throws UnauthorizedException 当上下文中没有认证信息时抛出
     */
    @Transactional(readOnly = true)
    public com.example.propertymanagement.dto.user.UserDto me() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal()
            .orElseThrow(() -> new UnauthorizedException("未登录"));
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new UnauthorizedException("用户不存在"));
        return UserMapper.toDto(user);
    }

    /**
     * 生成访问令牌 / 刷新令牌并与客户端 Cookie、数据库刷新令牌表同步。
     *
     * @param username   用户名（写入 JWT）
     * @param user       数据库用户实体
     * @param request    原始请求，用于判断是否 HTTPS
     * @param response   原始响应，用于写入 Cookie
     */
    private void attachTokensToResponse(String username,
                                        User user,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(username);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        Instant refreshExpiry = jwtTokenProvider.getRefreshTokenExpiryInstant();
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();

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

    /**
     * 从请求 Cookie 中提取刷新令牌值。若不存在则返回 {@code null}。
     */
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
