package com.nowcoder.community.component;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     * 0-普通用户; 1-超级管理员; 2-版主;  user表 type字段
     */
    int USER_TYPE = 0;
    int ROOT_TYPE = 1;
    int MODERATOR_type=2;

    /**
     * 0未激活 1已激活
     */
    int ACTIVATION_STATUS = 1;
    int NOT_ACTIVATION_STATUS = 0;

    /**
     * 登录凭证表状态 0有效 1无效
     */
    int NOT_LOGINTICKET_STATUS =1;
    int YES_LOGINTICKET_STATUS =0;

    /**
     * 验证码  存储在session中的key
     */
    String KAPTCHA = "kaptcha";

    /**
     * cookie key
     */
    String TICKET = "ticket";



    /**
     * 实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 主题: 发帖
     */
    String TOPIC_PUBLISH = "publish";

}
