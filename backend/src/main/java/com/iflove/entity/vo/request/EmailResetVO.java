package com.iflove.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Data
public class EmailResetVO {
    @Email
    String email;
    @Length(min = 6, max = 20)
    String password;
    @Length(min = 6, max = 6)
    String code;
}
