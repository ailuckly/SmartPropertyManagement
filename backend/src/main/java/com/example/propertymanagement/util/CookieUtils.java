package com.example.propertymanagement.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Convenience helpers to issue and clear HttpOnly cookies without repeating header boilerplate.
 */
public final class CookieUtils {

    private CookieUtils() {
    }

    /**
     * Adds an HttpOnly cookie with sane defaults (SameSite=Lax, secure flag toggled by parameter).
     */
    public static void addCookie(HttpServletResponse response,
                                 String name,
                                 String value,
                                 Duration maxAge,
                                 boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .sameSite("Lax")
            .maxAge(maxAge)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Clears an HttpOnly cookie by re-issuing it with a zero max-age.
     */
    public static void deleteCookie(HttpServletResponse response,
                                    String name,
                                    boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .sameSite("Lax")
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
