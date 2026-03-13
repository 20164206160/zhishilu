package com.zhishilu.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户修改密码请求对象
 */
@Data
public class UpdatePasswordReq {

    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String newPassword;
}
