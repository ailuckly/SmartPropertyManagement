package com.example.propertymanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 用户更新请求DTO（管理员专用）
 */
@Data
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 新密码（可选，如果不修改密码则不传）
     */
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @Size(max = 50, message = "名字长度不能超过50个字符")
    private String firstName;

    @Size(max = 50, message = "姓氏长度不能超过50个字符")
    private String lastName;

    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phoneNumber;

    /**
     * 角色名称列表，例如：["ROLE_ADMIN", "ROLE_TENANT"]
     */
    private Set<String> roleNames;
}
