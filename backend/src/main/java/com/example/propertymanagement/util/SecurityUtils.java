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
     * Extracts the authenticated {@link UserPrincipal} from the security context if present.
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    /**
     * Checks whether the supplied principal carries the requested authority.
     */
    public static boolean hasRole(UserPrincipal principal, String role) {
        return principal.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
