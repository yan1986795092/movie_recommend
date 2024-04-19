# 数据库初始化

-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    `userAccount`  VARCHAR(256)                                                       NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512)                                                       NOT NULL COMMENT '密码',
    `unionId`      VARCHAR(256)                                                       NULL COMMENT '微信开放平台id',
    `mpOpenId`     VARCHAR(256)                                                       NULL COMMENT '公众号openId',
    `userName`     VARCHAR(256)                                                       NULL COMMENT '用户昵称',
    `userAvatar`   VARCHAR(1024)                                                      NULL COMMENT '用户头像',
    `userProfile`  VARCHAR(512)                                                       NULL COMMENT '用户简介',
    `userRole`     VARCHAR(256) DEFAULT 'user'                                        NOT NULL COMMENT '用户角色：user/admin/ban',
    `createTime`   DATETIME     DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    `updateTime`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `isDelete`     TINYINT      DEFAULT 0                                             NOT NULL COMMENT '是否删除',
    INDEX `idx_unionId` (`unionId`)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;


-- 爬虫建表
CREATE TABLE IF NOT EXISTS `movie`
(
    `movieId`             BIGINT AUTO_INCREMENT COMMENT '电影id' PRIMARY KEY,
    `name`                TEXT                                                           NOT NULL COMMENT '电影名',
    `director`            TEXT                                                           COMMENT '导演',
    `actors`              TEXT                                                           COMMENT '主演',
    `type`                TEXT                                                           NULL COMMENT '类型',
    `country`             TEXT                                                           NULL COMMENT '制片国家/地区',
    `releaseDate`         TEXT                                                           NULL COMMENT '上映日期',
    `runtime`             TEXT                                                           NULL COMMENT '片长',
    `ratingNum`           TEXT                                                           NULL COMMENT '豆瓣评分',
    `tags`                TEXT                                                           NULL COMMENT '电影简介',
    `movieUrl`            TEXT                                                           NULL COMMENT '电影地址',
    `local`               TEXT                                                           NULL COMMENT '照片本地址',
    `createTime`          DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    `updateTime`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间'
) DEFAULT CHARSET = utf8 COMMENT ='电影表';


CREATE TABLE IF NOT EXISTS `comments`
(
    `commentsID`          BIGINT    auto_increment  comment '评论id' primary key,
    `movieID`             BIGINT                                                         Null comment '电影id',
    `commentInfo`         TEXT                                                           NULL COMMENT '评论内容',
    `commentAuthor`       TEXT                                                           NULL COMMENT '评论者',
    `commentAuthorImgUrl` TEXT                                                           NULL COMMENT '评论者头像',
    `commentVote`         TEXT                                                           NULL COMMENT '评论点赞数',
    `commentDate`         TEXT                                                           NULL COMMENT '评论时间',
    `createTime`          DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    `updateTime`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间'
)DEFAULT CHARSET = utf8 COMMENT ='影评表';


-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';


-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';