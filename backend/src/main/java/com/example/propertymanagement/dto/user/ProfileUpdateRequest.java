package com.example.propertymanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户资料更新请求 DTO
 */
@Data
public class ProfileUpdateRequest {
    
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Size(max = 50, message = "名字长度不能超过50个字符")
    private String firstName;
    
    @Size(max = 50, message = "姓氏长度不能超过50个字符")
    private String lastName;
    
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phoneNumber;
}
