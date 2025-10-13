package com.example.propertymanagement.util;

import com.example.propertymanagement.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Small helper class to encapsulate common SecurityContext lookups used across services.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 从 SecurityContext 中提取当前登录主体。
     *
     * @return 登录主体，可为空
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    /**
     * 判断主体是否具备指定角色（Spring Security 中角色即 authority）。
     *
     * @param principal 用户主体
     * @param role      角色名称，如 {@code ROLE_ADMIN}
     * @return 是否具有角色
     */
    public static boolean hasRole(UserPrincipal principal, String role) {
        return principal.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
