## 项目介绍

<p align=center>
  <a href="https://weblive.fz688.dpdns.org/" style="border-radius: 50%;width: 100px;height: 100px">
    <img src="logo.ico" alt="weblive" >
  </a>
</p>

<p align="center">
   <a target="_blank" href="https://github.com/FZ688">
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/JDK-17+-green.svg"/>
      <img src="https://img.shields.io/badge/springboot-3.5.7-green"/>
      <img src="https://img.shields.io/badge/mysql-8.0.23-green"/>
      <img src="https://img.shields.io/badge/mybatis--plus-3.5.14-green"/>
      <img src="https://img.shields.io/badge/redis-5.0.14.1-green"/>
      <img src="https://img.shields.io/badge/elasticsearch-8.18.8-green"/>
      <img src="https://img.shields.io/badge/kafka-3.7.0-green"/>
   </a>
</p>

[在线地址](#在线地址) | [目录结构](#目录结构) | [主要功能模块](#主要功能模块) | [项目特点](#项目特点) | [技术介绍](#技术介绍) | [运行环境](#运行环境)


## 在线地址

**项目链接：** [weblive视频分享](https://weblive.fz688.dpdns.org/)


账号:test 密码:123456


您的star是我坚持的动力，感谢大家的支持，欢迎提交pr共同改进项目。

## 目录结构

后端项目采用Maven多模块架构，位于weblive目录下。

SQL文件位于根目录下的 weblive.sql

可直接导入该项目于本地，修改后端配置文件中的数据库等连接信息，项目中使用到的Redis、Elasticsearch、Kafka等中间件需要自行安装配置。

当你克隆项目到本地后可使用账号：admin，密码：123456 进行登录

**ps：请确保MySQL、Redis、Elasticsearch、Kafka等服务正常运行后，再启动后端项目。**

```
weblive
├── weblive-admin   -- 后台管理系统模块（用户管理、视频审核、分类管理等）
├── weblive-common  -- 通用模块（实体类、Mapper、Service、工具类等）
├── weblive-web     -- 前台门户模块（视频浏览、上传、评论、弹幕等）
├── easylive.sql    -- 数据库脚本
└── pom.xml         -- 父项目Maven配置
```

## 主要功能模块

**前台功能（weblive-web）：**
- 视频播放：支持多P视频播放、弹幕实时显示
- 视频上传：支持分P上传、视频信息编辑、封面上传
- 评论互动：发表评论、回复评论、点赞评论、置顶评论
- 用户中心：个人主页、视频管理、互动管理、统计数据
- 视频合集：创建和管理视频系列
- 播放历史：记录播放进度，支持续播
- 消息通知：系统消息、互动消息推送
- 视频搜索：基于Elasticsearch的高性能搜索
- 用户关注：关注UP主，查看关注列表

**后台功能（weblive-admin）：**
- 用户管理：用户列表、权限管理、角色管理
- 视频管理：视频审核、视频列表、分类管理
- 互动管理：评论审核、弹幕管理
- 系统设置：网站配置、参数管理
- 数据统计：视频统计、用户统计


## 项目特点

- 支持视频上传和在线播放，采用FFmpeg进行视频转码和处理
- 支持视频分P上传，多分集管理
- 实时弹幕功能，增强观看体验
- 完善的评论系统，支持评论回复、点赞、置顶等功能
- 支持视频合集/系列管理，方便组织相关视频内容
- 视频播放历史记录，方便用户续播
- 采用Elasticsearch实现视频搜索，支持高亮分词，响应速度快
- 用户关注系统和消息通知功能
- 视频分类管理，支持一级和二级分类
- 视频互动设置，UP主可自由控制评论区和弹幕开关
- 采用Kafka进行异步消息处理，提升系统性能
- 支持Redis缓存和分布式锁，优化系统响应
- 前后端分离架构，适应当前主流技术趋势
- 采用Sa-Token进行权限认证，支持RBAC权限模型
- 后台管理系统支持视频审核、用户管理、数据统计等功能
- 新增AOP注解实现操作日志和限流管理
- 代码支持多种文件上传模式（本地上传），可扩展OSS对象存储
- 代码遵循阿里巴巴开发规范，利于开发者学习


## 技术介绍

**后端技术栈：** 

- **核心框架：** Spring Boot 3.5.7 + JDK 17
- **持久层：** MyBatis-Plus 3.5.14 + MySQL 8.0.23
- **缓存：** Redis 5.0.14.1
- **搜索引擎：** Elasticsearch 8.18.8
- **消息队列：** Kafka 3.7.0
- **权限认证：** Sa-Token
- **视频处理：** FFmpeg
- **其他：** Logback（日志）、Docker（部署）

**数据库：** MySQL 8.0.23

**中间件：** Redis、Elasticsearch、Kafka

**部署：** Nginx + Docker

## 运行环境

**推荐配置：** 2核4G服务器

**最低配置：** 1核2G服务器（建议关闭Elasticsearch，使用MySQL进行搜索）

**中间件要求：**
- MySQL 8.0+
- Redis 5.0+
- Elasticsearch 8.0+（可选）
- Kafka 3.0+
- FFmpeg（视频转码必需）

## 开发环境

| 开发工具      |           说明            |
|-----------| ------------------------- |
| IDEA      | Java开发工具IDE            |
| VSCode    | Vue开发工具IDE             |
| Navicat   | MySQL远程连接工具          |
| Tiny RDM  | Redis远程连接工具          |
| MobaXterm | Linux远程连接和文件上传工具 |

| 开发环境         | 版本       |
|--------------|----------|
| JDK          | 17       |
| MySQL        | 8.0.23   |
| Redis        | 5.0.14.1 |
| Elasticsearch | 8.18.8   |
| Kafka        | 3.7.0    |
| FFmpeg       | 无版本要求    |
