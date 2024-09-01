package com.iflove.entity;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote 常量值
 */
public class Const {
    // redis 存储前缀
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    public static final String JWT_WHITE_LIST = "jwt:whitelist:";
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit";
    public static final String VERIFY_EMAIL_DATA = "verify:email:data";
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";

    //用于给Jwt令牌签名校验的秘钥
    public static final String JWT_SIGN_KEY = "1145141919810";

    // token 令牌消亡时间，小时为单位
    public static final int EXPIRE_TIME = 7;

    // CorsFilter 过滤器优先级
    public static final int ORDER_CORS = -102;

    // FlowLimitFilter 过滤器优先级
    public static final int ORDER_FLOW_LIMIT = -101;

    // 验证邮件发送冷却时间限制，秒为单位
    public static final int verifyLimit = 60;

    // 指定时间内最大请求次数 (次/秒)
    public static final int FLOW_MAX_REQUEST = 50;

    // 计数时间周期 (秒)
    public static final int FLOW_COUNT_PERIOD = 3;

    // 超出请求限制封禁时间 (秒)
    public static final int FLOW_BLOCK_TIME = 10;

    //用户角色
    public final static String ROLE_DEFAULT = "user";
}
