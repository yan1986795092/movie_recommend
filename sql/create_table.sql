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


-- 电影表
CREATE TABLE IF NOT EXISTS `movie`
(
    `movieId`     BIGINT AUTO_INCREMENT COMMENT '电影id' PRIMARY KEY,
    `userID`      BIGINT                                                         Null comment '创建用户id',
    `name`        TEXT                                                           NOT NULL COMMENT '电影名',
    `director`    TEXT                                                           NULL COMMENT '导演',
    `actors`      TEXT                                                           NULL COMMENT '主演',
    `type`        TEXT                                                           NULL COMMENT '类型',
    `country`     TEXT                                                           NULL COMMENT '制片国家/地区',
    `releaseDate` TEXT                                                           NULL COMMENT '上映日期',
    `runtime`     TEXT                                                           NULL COMMENT '片长',
    `ratingNum`   TEXT                                                           NULL COMMENT '豆瓣评分',
    `thumbNum`    int      default 0                                             NULL comment '点赞数',
    `favourNum`   int      default 0                                             NULL comment '收藏数',
    `tags`        TEXT                                                           NULL COMMENT '电影简介',
    `movieUrl`    TEXT                                                           NULL COMMENT '电影地址',
    `local`       TEXT                                                           NULL COMMENT '照片本地地址',
    `createTime`  DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    `updateTime`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `isDelete`    TINYINT  DEFAULT 0                                             NOT NULL COMMENT '是否删除'
) DEFAULT CHARSET = utf8 COMMENT ='电影表';

-- 推荐电影表
CREATE TABLE IF NOT EXISTS `movie_recommend`
(
    `movieId`     BIGINT AUTO_INCREMENT COMMENT '电影id' PRIMARY KEY,
    `userID`      BIGINT                                                         Null comment '创建用户id',
    `name`        TEXT                                                           NOT NULL COMMENT '电影名',
    `director`    TEXT                                                           NULL COMMENT '导演',
    `actors`      TEXT                                                           NULL COMMENT '主演',
    `type`        TEXT                                                           NULL COMMENT '类型',
    `country`     TEXT                                                           NULL COMMENT '制片国家/地区',
    `releaseDate` TEXT                                                           NULL COMMENT '上映日期',
    `runtime`     TEXT                                                           NULL COMMENT '片长',
    `ratingNum`   TEXT                                                           NULL COMMENT '豆瓣评分',
    `thumbNum`    int      default 0                                             NULL comment '点赞数',
    `favourNum`   int      default 0                                             NULL comment '收藏数',
    `tags`        TEXT                                                           NULL COMMENT '电影简介',
    `movieUrl`    TEXT                                                           NULL COMMENT '电影地址',
    `local`       TEXT                                                           NULL COMMENT '照片本地地址',
    `createTime`  DATETIME DEFAULT CURRENT_TIMESTAMP                             NOT NULL COMMENT '创建时间',
    `updateTime`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `isDelete`    TINYINT  DEFAULT 0                                             NOT NULL COMMENT '是否删除'
) DEFAULT CHARSET = utf8 COMMENT ='电影表';

-- 评论表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    movieId    bigint                             null comment '电影id',
    userId     bigint                             null comment '创建用户 id',
    content    text                               null comment '内容',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '是否删除',
    index idx_userId (userId)
) comment '评论' collate = utf8mb4_unicode_ci;

-- 评论点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    movieId    bigint                             null comment '电影 id',
    userId     bigint                             null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (movieId),
    index idx_userId (userId)
) comment '电影点赞';


-- 评论收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    movieId    bigint                             null comment '电影 id',
    userId     bigint                             null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (movieId),
    index idx_userId (userId)
) comment '电影收藏';

-- 图表表
create table if not exists chart
(
    id           bigint auto_increment comment 'id' primary key,
    goal				 text  null comment '分析目标',
    `name`               varchar(128) null comment '图表名称',
    chartData    text  null comment '图表数据',
    chartType	   varchar(128) null comment '图表类型',
    genChart		 text	 null comment '生成的图表数据',
    genResult		 text	 null comment '生成的分析结论',
    status       varchar(128) not null default 'wait' comment 'wait,running,succeed,failed',
    execMessage  text   null comment '执行信息',
    userId       bigint null comment '创建用户 id',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;