-- ============================================================
-- 智慧物业管理平台：模拟数据脚本
-- 版本：2.0
-- 创建日期：2025-01-14
-- ============================================================
-- 说明：
--   1. 使用子查询动态获取ID，避免硬编码
--   2. 数据包括：业主、租户、物业、租约、支付记录、维修请求
--   3. 符合阿里巴巴规范（表名单数，新增字段等）
-- ============================================================

USE `smart_property_system`;

-- ============================================================
-- 1. 创建业主用户（3个）
-- ============================================================
INSERT INTO `user` (`username`, `email`, `password`, `first_name`, `last_name`, `phone_number`) VALUES
('owner_wang', 'wang@smartproperty.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '明', '王', '13800001001'),
('owner_li', 'li@smartproperty.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '华', '李', '13800001002'),
('owner_zhang', 'zhang@smartproperty.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '伟', '张', '13800001003');

-- 为业主分配角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u
CROSS JOIN `role` r
WHERE u.username IN ('owner_wang', 'owner_li', 'owner_zhang')
AND r.name = 'ROLE_OWNER';

-- ============================================================
-- 2. 创建租户用户（7个）
-- ============================================================
INSERT INTO `user` (`username`, `email`, `password`, `first_name`, `last_name`, `phone_number`) VALUES
('tenant_zhao', 'zhao@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '丽', '赵', '13800002001'),
('tenant_chen', 'chen@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '强', '陈', '13800002002'),
('tenant_liu', 'liu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '芳', '刘', '13800002003'),
('tenant_huang', 'huang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '刚', '黄', '13800002004'),
('tenant_xu', 'xu@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '敏', '徐', '13800002005'),
('tenant_sun', 'sun@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '杰', '孙', '13800002006'),
('tenant_ma', 'ma@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka', '娜', '马', '13800002007');

-- 为租户分配角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u
CROSS JOIN `role` r
WHERE u.username IN ('tenant_zhao', 'tenant_chen', 'tenant_liu', 'tenant_huang', 'tenant_xu', 'tenant_sun', 'tenant_ma')
AND r.name = 'ROLE_TENANT';

-- ============================================================
-- 3. 创建物业数据（20个，分布在不同城市）
-- ============================================================

-- 业主：王明 的物业（6个）
INSERT INTO `property` (`owner_id`, `address`, `city`, `state`, `zip_code`, `property_type`, `bedrooms`, `bathrooms`, `square_footage`, `rent_amount`, `status`) VALUES
((SELECT id FROM `user` WHERE username = 'owner_wang'), '金水区经三路15号1单元501', '郑州', '河南', '450000', 'APARTMENT', 2, 1.0, 85, 2500, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_wang'), '金水区经三路15号1单元502', '郑州', '河南', '450000', 'APARTMENT', 2, 1.0, 85, 2500, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_wang'), '金水区经三路15号2单元601', '郑州', '河南', '450000', 'APARTMENT', 3, 2.0, 120, 3500, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_wang'), '二七区大学路33号6栋301', '郑州', '河南', '450052', 'APARTMENT', 2, 1.0, 88, 2600, 'UNDER_MAINTENANCE'),
((SELECT id FROM `user` WHERE username = 'owner_wang'), '西工区中州中路188号2栋501', '洛阳', '河南', '471000', 'APARTMENT', 2, 1.0, 80, 2000, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_wang'), '鼓楼区大梁路68号1单元301', '开封', '河南', '475000', 'APARTMENT', 2, 1.0, 75, 1800, 'AVAILABLE');

-- 业主：李华 的物业（6个）
INSERT INTO `property` (`owner_id`, `address`, `city`, `state`, `zip_code`, `property_type`, `bedrooms`, `bathrooms`, `square_footage`, `rent_amount`, `status`) VALUES
((SELECT id FROM `user` WHERE username = 'owner_li'), '中原区桐柏路88号A栋301', '郑州', '河南', '450007', 'APARTMENT', 2, 1.0, 90, 2800, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_li'), '中原区桐柏路88号A栋302', '郑州', '河南', '450007', 'APARTMENT', 2, 1.0, 90, 2800, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_li'), '中原区桐柏路88号B栋401', '郑州', '河南', '450007', 'APARTMENT', 3, 2.0, 115, 3200, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_li'), '管城区紫荆山路66号3单元501', '郑州', '河南', '450004', 'APARTMENT', 2, 1.0, 82, 2400, 'UNDER_MAINTENANCE'),
((SELECT id FROM `user` WHERE username = 'owner_li'), '涧西区南昌路25号3单元301', '洛阳', '河南', '471003', 'APARTMENT', 3, 2.0, 110, 2600, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_li'), '龙亭区迎宾路18号2栋601', '开封', '河南', '475001', 'APARTMENT', 3, 2.0, 105, 2300, 'LEASED');

-- 业主：张伟 的物业（8个，包含别墅和商业物业）
INSERT INTO `property` (`owner_id`, `address`, `city`, `state`, `zip_code`, `property_type`, `bedrooms`, `bathrooms`, `square_footage`, `rent_amount`, `status`) VALUES
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '高新区科学大道100号5栋2单元1201', '郑州', '河南', '450001', 'APARTMENT', 3, 2.0, 130, 4000, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '高新区科学大道100号5栋2单元1202', '郑州', '河南', '450001', 'APARTMENT', 2, 1.0, 95, 3000, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '惠济区江山路别墅区A区8号', '郑州', '河南', '450044', 'HOUSE', 4, 3.0, 280, 8000, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '惠济区江山路别墅区A区9号', '郑州', '河南', '450044', 'HOUSE', 5, 3.0, 320, 9500, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '洛龙区开元大道88号A座1801', '洛阳', '河南', '471023', 'APARTMENT', 3, 2.0, 135, 3500, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '金水区花园路商业街125号1层', '郑州', '河南', '450003', 'COMMERCIAL', 0, 2.0, 180, 12000, 'LEASED'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '金水区花园路商业街127号1层', '郑州', '河南', '450003', 'COMMERCIAL', 0, 2.0, 150, 10000, 'AVAILABLE'),
((SELECT id FROM `user` WHERE username = 'owner_zhang'), '中原区建设路商业广场A区201', '郑州', '河南', '450007', 'COMMERCIAL', 0, 1.0, 100, 8000, 'LEASED');

-- ============================================================
-- 4. 创建租约数据（15个）
-- ============================================================

-- 当前活跃租约（11个）
INSERT INTO `lease` (`property_id`, `tenant_id`, `start_date`, `end_date`, `rent_amount`, `status`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '2024-06-01', '2025-05-31', 2500, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '金水区经三路15号2单元601'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '2024-08-01', '2025-07-31', 3500, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号A栋301'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '2024-09-01', '2025-08-31', 2800, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号B栋401'), (SELECT id FROM `user` WHERE username = 'tenant_huang'), '2024-07-15', '2025-07-14', 3200, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '高新区科学大道100号5栋2单元1201'), (SELECT id FROM `user` WHERE username = 'tenant_xu'), '2024-10-01', '2025-09-30', 4000, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '惠济区江山路别墅区A区8号'), (SELECT id FROM `user` WHERE username = 'tenant_sun'), '2024-05-01', '2025-04-30', 8000, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '西工区中州中路188号2栋501'), (SELECT id FROM `user` WHERE username = 'tenant_ma'), '2024-11-01', '2025-10-31', 2000, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '龙亭区迎宾路18号2栋601'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '2024-09-15', '2025-09-14', 2300, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '金水区花园路商业街125号1层'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '2024-12-01', '2025-11-30', 12000, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '中原区建设路商业广场A区201'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '2024-10-15', '2025-10-14', 8000, 'ACTIVE'),
((SELECT id FROM `property` WHERE address = '洛龙区开元大道88号A座1801'), (SELECT id FROM `user` WHERE username = 'tenant_huang'), '2024-06-15', '2025-06-14', 3500, 'ACTIVE');

-- 已过期租约（2个）
INSERT INTO `lease` (`property_id`, `tenant_id`, `start_date`, `end_date`, `rent_amount`, `status`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501'), (SELECT id FROM `user` WHERE username = 'tenant_xu'), '2023-01-01', '2023-12-31', 2300, 'EXPIRED'),
((SELECT id FROM `property` WHERE address = '金水区经三路15号2单元601'), (SELECT id FROM `user` WHERE username = 'tenant_sun'), '2023-03-01', '2024-02-29', 3200, 'EXPIRED');

-- 已终止租约（2个）
INSERT INTO `lease` (`property_id`, `tenant_id`, `start_date`, `end_date`, `rent_amount`, `status`) VALUES
((SELECT id FROM `property` WHERE address = '惠济区江山路别墅区A区8号'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '2023-08-01', '2024-07-31', 7500, 'TERMINATED'),
((SELECT id FROM `property` WHERE address = '西工区中州中路188号2栋501'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '2023-09-01', '2024-08-31', 1900, 'TERMINATED');

-- ============================================================
-- 5. 创建支付记录数据（80+条，覆盖2024年6月-2025年1月）
-- ============================================================

-- 使用变量存储租约ID
SET @lease1 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501') AND status = 'ACTIVE');
SET @lease2 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '金水区经三路15号2单元601') AND status = 'ACTIVE');
SET @lease3 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '中原区桐柏路88号A栋301') AND status = 'ACTIVE');
SET @lease4 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '中原区桐柏路88号B栋401') AND status = 'ACTIVE');
SET @lease5 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '高新区科学大道100号5栋2单元1201') AND status = 'ACTIVE');
SET @lease6 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '惠济区江山路别墅区A区8号') AND status = 'ACTIVE');
SET @lease7 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '西工区中州中路188号2栋501') AND status = 'ACTIVE');
SET @lease8 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '龙亭区迎宾路18号2栋601') AND status = 'ACTIVE');
SET @lease9 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '金水区花园路商业街125号1层') AND status = 'ACTIVE');
SET @lease10 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '中原区建设路商业广场A区201') AND status = 'ACTIVE');
SET @lease11 = (SELECT id FROM `lease` WHERE property_id = (SELECT id FROM `property` WHERE address = '洛龙区开元大道88号A座1801') AND status = 'ACTIVE');

-- 2024年6月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-06-05', '银行转账'),
(@lease2, 3500, '2024-06-10', '支付宝'),
(@lease6, 8000, '2024-06-15', '银行转账'),
(@lease11, 3500, '2024-06-20', '微信支付');

-- 2024年7月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-07-05', '银行转账'),
(@lease2, 3500, '2024-07-12', '支付宝'),
(@lease4, 3200, '2024-07-15', '银行转账'),
(@lease6, 8000, '2024-07-18', '银行转账'),
(@lease11, 3500, '2024-07-20', '微信支付');

-- 2024年8月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-08-05', '银行转账'),
(@lease2, 3500, '2024-08-10', '支付宝'),
(@lease4, 3200, '2024-08-15', '银行转账'),
(@lease6, 8000, '2024-08-20', '银行转账'),
(@lease11, 3500, '2024-08-22', '微信支付');

-- 2024年9月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-09-05', '银行转账'),
(@lease2, 3500, '2024-09-10', '支付宝'),
(@lease3, 2800, '2024-09-08', '微信支付'),
(@lease4, 3200, '2024-09-16', '银行转账'),
(@lease6, 8000, '2024-09-20', '银行转账'),
(@lease8, 2300, '2024-09-25', '支付宝'),
(@lease11, 3500, '2024-09-28', '微信支付');

-- 2024年10月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-10-05', '银行转账'),
(@lease2, 3500, '2024-10-10', '支付宝'),
(@lease3, 2800, '2024-10-08', '微信支付'),
(@lease4, 3200, '2024-10-15', '银行转账'),
(@lease5, 4000, '2024-10-20', '银行转账'),
(@lease6, 8000, '2024-10-25', '银行转账'),
(@lease7, 2000, '2024-10-28', '微信支付'),
(@lease8, 2300, '2024-10-30', '支付宝'),
(@lease10, 8000, '2024-10-22', '银行转账'),
(@lease11, 3500, '2024-10-26', '微信支付');

-- 2024年11月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-11-05', '银行转账'),
(@lease2, 3500, '2024-11-10', '支付宝'),
(@lease3, 2800, '2024-11-08', '微信支付'),
(@lease4, 3200, '2024-11-15', '银行转账'),
(@lease5, 4000, '2024-11-20', '银行转账'),
(@lease6, 8000, '2024-11-25', '银行转账'),
(@lease7, 2000, '2024-11-28', '微信支付'),
(@lease8, 2300, '2024-11-30', '支付宝'),
(@lease10, 8000, '2024-11-22', '银行转账'),
(@lease11, 3500, '2024-11-26', '微信支付');

-- 2024年12月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2024-12-05', '银行转账'),
(@lease2, 3500, '2024-12-10', '支付宝'),
(@lease3, 2800, '2024-12-08', '微信支付'),
(@lease4, 3200, '2024-12-15', '银行转账'),
(@lease5, 4000, '2024-12-20', '银行转账'),
(@lease6, 8000, '2024-12-25', '银行转账'),
(@lease7, 2000, '2024-12-28', '微信支付'),
(@lease8, 2300, '2024-12-30', '支付宝'),
(@lease9, 12000, '2024-12-31', '银行转账'),
(@lease10, 8000, '2024-12-22', '银行转账'),
(@lease11, 3500, '2024-12-26', '微信支付');

-- 2025年1月
INSERT INTO `payment` (`lease_id`, `amount`, `payment_date`, `payment_method`) VALUES
(@lease1, 2500, '2025-01-05', '银行转账'),
(@lease2, 3500, '2025-01-10', '支付宝'),
(@lease3, 2800, '2025-01-08', '微信支付'),
(@lease4, 3200, '2025-01-12', '银行转账'),
(@lease5, 4000, '2025-01-18', '银行转账'),
(@lease6, 8000, '2025-01-22', '银行转账'),
(@lease7, 2000, '2025-01-25', '微信支付'),
(@lease8, 2300, '2025-01-28', '支付宝'),
(@lease9, 12000, '2025-01-30', '银行转账'),
(@lease10, 8000, '2025-01-20', '银行转账'),
(@lease11, 3500, '2025-01-24', '微信支付');

-- ============================================================
-- 6. 创建维修请求数据（20+条，不同状态）
-- ============================================================

-- 待处理（4个）
INSERT INTO `maintenance_request` (`property_id`, `tenant_id`, `description`, `status`, `reported_at`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '客厅空调不制冷，需要检修', 'PENDING', '2025-01-10 09:30:00'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号A栋301'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '厨房水龙头漏水，需要更换', 'PENDING', '2025-01-11 14:20:00'),
((SELECT id FROM `property` WHERE address = '高新区科学大道100号5栋2单元1201'), (SELECT id FROM `user` WHERE username = 'tenant_xu'), '卫生间下水道堵塞', 'PENDING', '2025-01-12 10:15:00'),
((SELECT id FROM `property` WHERE address = '惠济区江山路别墅区A区8号'), (SELECT id FROM `user` WHERE username = 'tenant_sun'), '花园草坪需要修剪', 'PENDING', '2025-01-13 16:00:00');

-- 处理中（4个）
INSERT INTO `maintenance_request` (`property_id`, `tenant_id`, `description`, `status`, `reported_at`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号2单元601'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '卧室窗户玻璃破裂，需更换', 'IN_PROGRESS', '2025-01-05 11:30:00'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号B栋401'), (SELECT id FROM `user` WHERE username = 'tenant_huang'), '电梯按钮失灵', 'IN_PROGRESS', '2025-01-06 09:45:00'),
((SELECT id FROM `property` WHERE address = '西工区中州中路188号2栋501'), (SELECT id FROM `user` WHERE username = 'tenant_ma'), '热水器不工作', 'IN_PROGRESS', '2025-01-07 15:20:00'),
((SELECT id FROM `property` WHERE address = '金水区花园路商业街125号1层'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '店面门锁损坏', 'IN_PROGRESS', '2025-01-08 10:00:00');

-- 已完成（10个）
INSERT INTO `maintenance_request` (`property_id`, `tenant_id`, `description`, `status`, `reported_at`, `completed_at`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '客厅灯泡需要更换', 'COMPLETED', '2024-12-15 10:30:00', '2024-12-15 14:00:00'),
((SELECT id FROM `property` WHERE address = '金水区经三路15号2单元601'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '阳台纱窗破损', 'COMPLETED', '2024-12-18 14:20:00', '2024-12-19 10:00:00'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号A栋301'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '门铃失灵', 'COMPLETED', '2024-12-20 09:15:00', '2024-12-20 16:30:00'),
((SELECT id FROM `property` WHERE address = '高新区科学大道100号5栋2单元1201'), (SELECT id FROM `user` WHERE username = 'tenant_xu'), '冰箱制冷效果差', 'COMPLETED', '2024-12-22 11:00:00', '2024-12-23 15:00:00'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号B栋401'), (SELECT id FROM `user` WHERE username = 'tenant_huang'), '洗衣机排水不畅', 'COMPLETED', '2024-12-25 15:30:00', '2024-12-26 09:00:00'),
((SELECT id FROM `property` WHERE address = '惠济区江山路别墅区A区8号'), (SELECT id FROM `user` WHERE username = 'tenant_sun'), '车库门遥控器失效', 'COMPLETED', '2024-12-28 16:20:00', '2024-12-29 10:30:00'),
((SELECT id FROM `property` WHERE address = '西工区中州中路188号2栋501'), (SELECT id FROM `user` WHERE username = 'tenant_ma'), '厨房抽油烟机噪音大', 'COMPLETED', '2024-12-30 10:45:00', '2024-12-31 14:00:00'),
((SELECT id FROM `property` WHERE address = '龙亭区迎宾路18号2栋601'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '墙面有裂缝需要修补', 'COMPLETED', '2025-01-02 14:30:00', '2025-01-03 16:00:00'),
((SELECT id FROM `property` WHERE address = '金水区花园路商业街125号1层'), (SELECT id FROM `user` WHERE username = 'tenant_chen'), '天花板漏水', 'COMPLETED', '2025-01-03 09:00:00', '2025-01-04 11:30:00'),
((SELECT id FROM `property` WHERE address = '中原区建设路商业广场A区201'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '空调外机噪音过大', 'COMPLETED', '2025-01-04 16:15:00', '2025-01-05 10:00:00');

-- 已取消（2个）
INSERT INTO `maintenance_request` (`property_id`, `tenant_id`, `description`, `status`, `reported_at`) VALUES
((SELECT id FROM `property` WHERE address = '金水区经三路15号1单元501'), (SELECT id FROM `user` WHERE username = 'tenant_zhao'), '需要更换浴室镜子（已自行处理）', 'CANCELLED', '2024-12-10 11:20:00'),
((SELECT id FROM `property` WHERE address = '中原区桐柏路88号A栋301'), (SELECT id FROM `user` WHERE username = 'tenant_liu'), '需要安装晾衣架（已自行购买）', 'CANCELLED', '2024-12-12 15:40:00');

-- ============================================================
-- 执行完成提示
-- ============================================================
SELECT '模拟数据插入成功！请继续执行 04_db_verify.sql 验证数据' as message;
