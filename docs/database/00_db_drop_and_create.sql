-- ============================================================
-- 智慧物业管理平台：数据库删除与重建脚本
-- 版本：2.0（阿里巴巴规范版）
-- 创建日期：2025-01-14
-- ============================================================
-- 说明：
--   1. 此脚本会删除旧的数据库，请谨慎执行
--   2. 删除前请确认已备份重要数据
--   3. 适用于开发环境的完全重建
-- ============================================================

-- 删除旧数据库（如果存在）
DROP DATABASE IF EXISTS `property_db`;
DROP DATABASE IF EXISTS `smart_property_db`;

-- 创建新数据库（符合阿里巴巴命名规范）
CREATE DATABASE IF NOT EXISTS `smart_property_system`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用新数据库
USE `smart_property_system`;

-- 显示当前数据库信息
SELECT 
    DATABASE() as current_database,
    @@character_set_database as charset,
    @@collation_database as collation;

-- ============================================================
-- 执行完成提示
-- ============================================================
SELECT '数据库创建成功！请继续执行 01_db_schema.sql' as message;
