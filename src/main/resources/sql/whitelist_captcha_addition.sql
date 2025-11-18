-- ===========================================
-- 验证码接口白名单数据添加脚本
-- 为验证码相关接口添加到白名单中，确保验证码功能能够正常访问
-- 创建时间: 2025-11-16
-- 作者: zhengbing
-- ===========================================

-- 检查验证码白名单是否已存在，如果不存在则添加
-- 先检查是否已存在该白名单
INSERT INTO `sys_white_list` (`pattern`, `type`, `description`, `enabled`)
SELECT * FROM (
    SELECT '/api/captcha/**' as pattern, 'URL' as type, '验证码管理接口，包括生成和刷新验证码' as description, 1 as enabled
) AS temp
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_white_list` WHERE pattern = '/api/captcha/**'
);

-- 验证是否添加成功
SELECT * FROM `sys_white_list` WHERE pattern LIKE '%captcha%' ORDER BY create_time DESC;

-- 查询当前所有白名单数据，确认验证码接口已添加
SELECT 
    pattern,
    type,
    description,
    enabled,
    create_time
FROM `sys_white_list` 
WHERE enabled = 1 
ORDER BY create_time DESC;

-- 显示统计信息
SELECT 
    COUNT(*) as total_white_list_count,
    COUNT(CASE WHEN pattern LIKE '%captcha%' THEN 1 END) as captcha_white_list_count
FROM `sys_white_list` 
WHERE enabled = 1;