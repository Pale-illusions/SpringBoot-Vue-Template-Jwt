package com.iflove.utils;

import com.iflove.entity.Const;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@Component
public class RedisUtil {
    @Resource
    private StringRedisTemplate template;

    /**
     * 将 Token 列入 Redis 白名单
     * @param key uuid
     * @param expire 过期时间
     */
    public void set(String key, long expire) {
        template.opsForValue().set(Const.JWT_WHITE_LIST + key, "", expire, TimeUnit.MILLISECONDS);
    }

    /**
     * 将 Token 列入 Redis 黑名单
     * @param key Token 特有uuid
     * @return 成功？
     */
    public Boolean delete(String key) {
        template.delete(Const.JWT_WHITE_LIST + key);
        template.opsForValue().set(Const.JWT_BLACK_LIST + key, "");
        return true;
    }

    /**
     * 判断是否存在黑名单中（是否有效）
     * @param key uuid
     * @return 有效？
     */
    public Boolean isInvalid(String key) {
        return template.hasKey(Const.JWT_BLACK_LIST + key);
    }

    /**
     * 设定过期时间
     * @return 过期时间
     */
    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, Const.EXPIRE_TIME);
        return calendar.getTime();
    }
}
