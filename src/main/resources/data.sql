-- 公安证据与枪械管理平台 - 种子数据
-- 使用 INSERT IGNORE 保证多次启动不报主键冲突（schema.sql 用了 IF NOT EXISTS，表不会重建）

INSERT IGNORE INTO officers (id, police_no, name, department, rank_title, status) VALUES
  (1, '030001', '王建国', '刑侦支队', '一级警督', 'ACTIVE'),
  (2, '030002', '李志强', '刑侦支队', '二级警司', 'ACTIVE'),
  (3, '030003', '赵晓敏', '物证管理科', '三级警长', 'ACTIVE'),
  (4, '030004', '陈海涛', '特巡警支队', '一级警司', 'ACTIVE');

INSERT IGNORE INTO evidence (id, evidence_no, case_no, name, category, description, status, location, registered_by) VALUES
  (1, 'WZ-2026-0001', 'AJ-2026-0101', '作案匕首一把', 'WEAPON', '案发现场提取的折叠匕首', 'IN_STORAGE', '物证仓A-03', 3),
  (2, 'WZ-2026-0002', 'AJ-2026-0101', '血迹样本', 'BIOLOGICAL', '现场地面血迹棉签提取', 'IN_STORAGE', '物证仓A-04', 3),
  (3, 'WZ-2026-0003', 'AJ-2026-0205', '涉案手机一部', 'ELECTRONIC', '黑色智能手机，待数据勘验', 'REGISTERED', '暂存柜B-11', 2);

INSERT IGNORE INTO custody_records (id, evidence_id, action, from_officer, to_officer, remark, occurred_at) VALUES
  (1, 1, 'REGISTER', NULL, 3, '入库登记', '2026-05-20 09:00:00.000'),
  (2, 2, 'REGISTER', NULL, 3, '入库登记', '2026-05-20 09:10:00.000'),
  (3, 3, 'REGISTER', NULL, 2, '暂存登记', '2026-05-22 14:30:00.000');

INSERT IGNORE INTO firearms (id, serial_no, model, type, caliber, status) VALUES
  (1, 'QX-77-100231', '92式手枪', 'PISTOL', '9mm', 'IN_STORE'),
  (2, 'QX-77-100232', '92式手枪', 'PISTOL', '9mm', 'ISSUED'),
  (3, 'QX-79-200145', '79式微冲', 'SUBMACHINE_GUN', '7.62mm', 'IN_STORE');

INSERT IGNORE INTO firearm_issuances (id, firearm_id, officer_id, purpose, ammo_issued, issued_at, due_at, status) VALUES
  (1, 2, 4, '武装巡逻执勤', 15, '2026-06-01 08:00:00.000', '2026-06-01 20:00:00.000', 'ISSUED');
