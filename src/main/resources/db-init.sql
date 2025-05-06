-- 创建数据库
CREATE DATABASE IF NOT EXISTS traveldiary CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE traveldiary;

-- 创建用户并授权（可根据实际需求修改密码）
CREATE USER IF NOT EXISTS 'traveldiary_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON traveldiary.* TO 'traveldiary_user'@'localhost';
FLUSH PRIVILEGES;

-- 如果需要初始测试数据，可以在下方添加INSERT语句
-- 注意：如果使用spring.jpa.hibernate.ddl-auto=update，表结构会自动创建 