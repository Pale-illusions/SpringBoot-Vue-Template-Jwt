package com.iflove.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@TableName(value ="account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private Integer id;

    private String username;

    private String password;

    private String email;

    private Date registerTime;

    private List<Role> roles;
}