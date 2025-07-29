package com.zdd.entry.constants;

public class CommonConstant {

    /**
     * redis key 过期时间 1分钟
     */
    public static final Integer REDIS_KEY_TIME_ONE_MIN = 60000;

    public static final Integer REDIS_KEY_TIME_ONE_DAY = REDIS_KEY_TIME_ONE_MIN * 60 * 24;


    /**
     * redis key 前缀
     */
    public static final String REDIS_KEY_PREFIX = "easeymetting:";

    /**
     * 验证码 redis key
     */
    public static final String REDIS_KEY_CHECKCODE = REDIS_KEY_PREFIX + "checkcode:";

    public static final String REDIS_KEY_USER_TOKEN = REDIS_KEY_PREFIX + "token:metting:";

    public static final String REDIS_KEY_MEETING_ROOM = REDIS_KEY_PREFIX + "meeting:room:";

    public static final String REDIS_KEY_INVITE_KEY = REDIS_KEY_PREFIX + "invite:key:";

    public static final String REDIS_KEY_SYSSETTING_KEY = REDIS_KEY_PREFIX + "syssettig:key:";


    /**
     * 正则表达式：密码
     */
    public static final String REGEX_PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,18}$";

    /**
     * md5 加密盐
     */
    public static final String MD5_SALTING = "easymetting";

    /**
     * 随机数长度
     */
    public static final Integer RODMIX_NUMBER = 8;


    public static final String FIlE = "file/";
    public static final String FIlE_TMP = "tmp/";
    public static final String FIlE_AVATAR = "avatar/";
    public static final String FILE_IMAGE = "image_video/";
    public static final String EXE_FILE_PATH = "exe/";
    public static final String EXE_SUFFIX = ".exe";


    // 配置消息分发中间件
    public static final String MESSAGING_HANDLE_CHANNEL = "messaging.handle.channel";
    public static final String MESSAGING_HANDLE_CHANNEL_REDIS = "redis";
    public static final String MESSAGING_HANDLE_CHANNEL_RABBITMQ = "rabbitmq";

    public static final String NETTY_PING = "ping";



    public static final String IMAGE_SUFFIX = ".jpg";
    public static final String IMAGETHUMBNAIL = "_imageThumbnail";
    public static final String VIDEO_SUFFIX = ".mp4";


    public static final String DEFAULT_AVATAR = "/user.png";
}
