package com.hmdp.dto;

import lombok.Data;

/**
 * 登录表单DTO
 *
 * @author ZengXuebin
 * @since 2023-08-20 20:05:16
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
