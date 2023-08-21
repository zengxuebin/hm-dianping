package com.hmdp.constant;

/**
 * redis常量
 *
 * @author ZengXuebin
 * @since 2023/8/21 20:09
 */
public class RedisConstants {

    private RedisConstants() {}

    /**
     * 登录验证码
     */
    public static final String LOGIN_CODE_KEY = "login:code:";

    /**
     * 验证码有效期
     */
    public static final Long LOGIN_CODE_TTL = 2L;

    /**
     * token
     */
    public static final String LOGIN_TOKEN_KEY = "login:token:";

    /**
     * token有效期 30分钟
     */
    public static final Long LOGIN_TOKEN_TTL = 30L * 60L * 1000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;

    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop_type";

    public static final String LOCK_SHOP_KEY = "lock:shop:";

    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    public static final String BLOG_LIKED_KEY = "blog:liked:";

    public static final String FEED_KEY = "feed:";

    public static final String SHOP_GEO_KEY = "shop:geo:";

    public static final String USER_SIGN_KEY = "sign:";
}
