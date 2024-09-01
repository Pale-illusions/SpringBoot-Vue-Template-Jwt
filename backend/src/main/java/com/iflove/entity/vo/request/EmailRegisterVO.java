package com.iflove.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote 用户注册表单信息
 */
@Data
public class EmailRegisterVO {
    @Email
    String email;
    @Length(min = 6, max = 6)
    String code;
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1, max = 10)
    String username;
    @Length(min = 6, max = 20)
    String password;
}
