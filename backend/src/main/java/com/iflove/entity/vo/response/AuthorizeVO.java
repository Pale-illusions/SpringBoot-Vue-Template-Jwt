package com.iflove.entity.vo.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote 用户登录验证信息
 */
@Data
public class AuthorizeVO {
    private String username;
    private List<String> roles;
    private String token;
    private Date expire;
}
