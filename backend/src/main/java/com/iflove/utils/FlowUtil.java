package com.iflove.utils;

import com.iflove.entity.Const;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote 限流通用工具，针对于不同的情况进行限流操作，支持限流升级
 */
@Component
public class FlowUtil {
    @Resource
    private StringRedisTemplate template;

    /**
     * 存放邮件验证码，过期时间3分钟
     * @param email 邮箱
     * @param code 验证码
     */
    public void addVerifyCode(String email, int code) {
        template.opsForValue().set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
    }

    /**
     * 获得邮箱验证码
     * @param email 邮箱
     * @return 验证码
     */
    public String getVerifyCode(String email) {
        return template.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
    }

    /**
     * 删除邮箱验证码
     * @param email 邮箱
     */
    public void deleteVerifyCode(String email) {
        template.delete(Const.VERIFY_EMAIL_DATA + email);
    }

    /**
     * 实现邮箱验证码请求限流操作，blockTime内不能重复请求
     * @param key key
     * @param blockTime 限流时间
     * @return 能否请求
     */
    public boolean limitEmailRequest(String key, int blockTime) {
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            return false;
        } else {
            template.opsForValue().set(key, "", blockTime, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 尝试对指定ip地址请求计数，如果被限制无法继续访问
     * @param address ip地址
     * @return 未限制(true) / 限制(false)
     */
    public boolean tryCount(String address) {
        synchronized (address.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + address)))
                return false;
            String counterKey = Const.FLOW_LIMIT_COUNTER + address;
            String blockKey = Const.FLOW_LIMIT_BLOCK + address;
            return this.limitPeriodCheck(counterKey, blockKey, Const.FLOW_BLOCK_TIME, Const.FLOW_MAX_REQUEST, Const.FLOW_COUNT_PERIOD);
        }
    }

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求，如3秒内不能再次发起请求
     * @param key 键
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceCheck(String key, int blockTime){
        return this.internalCheck(key, 1, blockTime, (overclock) -> false);
    }

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
     * 如3秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
     * @param key 键
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceUpgradeCheck(String key, int frequency, int baseTime, int upgradeTime){
        return this.internalCheck(key, frequency, baseTime, (overclock) -> {
            if (overclock)
                template.opsForValue().set(key, "1", upgradeTime, TimeUnit.SECONDS);
            return false;
        });
    }

    /**
     * 针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
     * @param counterKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period){
        return this.internalCheck(counterKey, frequency, period, (overclock) -> {
            if (overclock)
                template.opsForValue().set(blockKey, "", blockTime, TimeUnit.SECONDS);
            return !overclock;
        });
    }

    /**
     * 内部使用请求限制主要逻辑
     * @param key 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        String count = template.opsForValue().get(key);
        if (count != null) {
            long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            int c = Integer.parseInt(count);
            // 如果新的计数值不等于原计数值加一，说明可能存在并发问题，重新设置过期时间。
            if(value != c + 1)
                template.expire(key, period, TimeUnit.SECONDS);
            return action.run(value > frequency);
        } else {
            template.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}
