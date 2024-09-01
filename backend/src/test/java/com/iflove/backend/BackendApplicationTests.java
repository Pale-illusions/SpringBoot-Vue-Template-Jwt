package com.iflove.backend;

import com.iflove.entity.RestBean;
import com.iflove.mapper.AccountMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

@SpringBootTest
class BackendApplicationTests {
    @Resource
    StringRedisTemplate template;

    @Test
    void contextLoads() {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }

    @Test
    public void test() {
        System.out.println(RestBean.success().asJSONString());
    }

    @Resource
    AccountMapper mapper;

}
