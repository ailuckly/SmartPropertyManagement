package com.example.propertymanagement.dto.auth;

import com.example.propertymanagement.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在3到50个字符之间")
    String username,

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    String email,

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在6到100个字符之间")
    String password,

    String firstName,

    String lastName,

    @Pattern(regexp = "^[+0-9\\-\\s]{0,20}$", message = "手机号格式不正确")
    String phoneNumber,

    RoleName role
) {
}
