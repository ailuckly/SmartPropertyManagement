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
     * 构造并写入 HttpOnly Cookie，统一使用 SameSite=Lax。
     *
     * @param response 响应
     * @param name     Cookie 名称
     * @param value    Cookie 值
     * @param maxAge   生存时长
     * @param secure   是否仅在 HTTPS 传输（根据请求协议决定）
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
     * 清除指定 HttpOnly Cookie。
     *
     * @param response 响应
     * @param name     Cookie 名称
     * @param secure   是否仅在 HTTPS 传输
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
