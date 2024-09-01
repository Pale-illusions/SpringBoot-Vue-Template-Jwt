package com.iflove.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iflove.entity.dto.Account;
import com.iflove.entity.dto.Role;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.security.core.parameters.P;

import java.util.Date;
import java.util.List;

/**
* @author 苍镜月
* @description 针对表【account】的数据库操作Mapper
* @createDate 2024-08-30 13:42:18
* @Entity com.iflove.domain.Account
*/
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "role_id", property = "roles", many =
                @Many(select = "getRolesById")
            )
    })
    @Select("select * from account where username = #{username}")
    Account getUserByName(String username);

    @Select("select role from roles where id = #{id}")
    List<Role> getRolesById(String id);

    @Insert("insert into account (username, password, email, register_time, role_id) values (#{username}, #{password}, #{email}, #{registerTime}, #{id})")
    boolean saveUser(@Param("username") String username,
                     @Param("password") String password,
                     @Param("email") String email,
                     @Param("registerTime") Date registerTime,
                     @Param("id") String id);

    @Insert("insert into roles values (#{id}, #{role})")
    boolean saveRole(@Param("role") String role,
                     @Param("id") String id);
}




