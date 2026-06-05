# 公安证据与枪械管理平台 - 后端 API

一个纯后端的 REST API 服务，覆盖刑事物证的登记与保管链流转、警用枪械的登记与领用归还。
本项目作为「功能迭代」类评测题目的基础工程：Spring Boot + MySQL，docker compose 一键编排，结构清晰、留有充分扩展点。

## 技术栈

- Java 17 + Spring Boot 3.2（spring-web / spring-data-jpa / validation）
- 数据库：MySQL 8
- 编排：Docker Compose
- 测试：JUnit 5 + Spring MockMvc（测试用 H2 内存库，无需起容器）

## 快速开始

### 方式一：docker compose（推荐）

```bash
docker compose up --build
```

- API 暴露在 `http://localhost:8615`
- MySQL 暴露在宿主机 `13316` 端口
- 应用启动时自动执行 `schema.sql` 建表、`data.sql` 写入种子数据

### 方式二：本地运行（自带一个 MySQL）

```bash
# 先准备 MySQL，并通过环境变量提供连接信息（默认 127.0.0.1:13316 / eom / eompass / police_eom）
mvn spring-boot:run
```

### 运行测试

```bash
mvn test
```

测试使用 H2（MySQL 兼容模式）内存库，不依赖外部 MySQL，每个用例在事务中执行、结束回滚以保证隔离。

## 环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8615` | API 监听端口 |
| `DB_HOST` | `127.0.0.1` | MySQL 主机 |
| `DB_PORT` | `13316` | MySQL 端口 |
| `DB_USER` | `eom` | MySQL 用户 |
| `DB_PASSWORD` | `eompass` | MySQL 密码 |
| `DB_NAME` | `police_eom` | 数据库名 |

## 目录结构

```
src/main/java/com/police/eom/
├── EomApplication.java          # 入口
├── domain/                      # 实体：Officer / Evidence / CustodyRecord / Firearm / FirearmIssuance
├── repo/                        # Spring Data JPA 仓储
├── service/                     # 业务：EvidenceService / FirearmService
└── web/                         # 控制器、DTO、统一异常处理
src/main/resources/
├── application.yml
├── schema.sql                   # MySQL 建表
└── data.sql                     # 种子数据
src/test/                        # MockMvc 集成测试 + H2 schema
```

## 数据模型

- **officers 民警**：`id, police_no(唯一), name, department, rank_title, status`
- **evidence 物证**：`id, evidence_no(唯一), case_no, name, category, status, location, registered_by(FK)`
  - 状态流转：`REGISTERED → IN_STORAGE ⇄ CHECKED_OUT`，终态 `DESTROYED / RELEASED`
- **custody_records 保管链**：`id, evidence_id(FK), action(REGISTER/CHECK_OUT/CHECK_IN/...), from_officer, to_officer, remark, occurred_at`
- **firearms 枪械**：`id, serial_no(唯一), model, type, caliber, status(IN_STORE/ISSUED/MAINTENANCE/RETIRED)`
- **firearm_issuances 领用记录**：`id, firearm_id(FK), officer_id(FK), purpose, ammo_issued, ammo_returned, issued_at, due_at, returned_at, status`

## API 一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/health` | 健康检查 |
| GET | `/api/officers` | 民警列表 |
| GET | `/api/officers/{id}` | 民警详情 |
| POST | `/api/officers` | 新增民警 |
| GET | `/api/evidence` | 物证列表（支持 `caseNo`/`status` 筛选） |
| GET | `/api/evidence/{id}` | 物证详情 |
| POST | `/api/evidence` | 登记物证（自动生成 REGISTER 保管链记录） |
| POST | `/api/evidence/{id}/checkout` | 借出物证（状态校验 + 留痕） |
| POST | `/api/evidence/{id}/checkin` | 归还物证（状态校验 + 留痕） |
| GET | `/api/evidence/{id}/custody` | 查询物证完整保管链 |
| GET | `/api/firearms` | 枪械列表（支持 `status` 筛选） |
| GET | `/api/firearms/{id}` | 枪械详情 |
| POST | `/api/firearms` | 登记枪械 |
| POST | `/api/firearms/{id}/issue` | 领用枪械（在库校验、应还时间校验） |
| POST | `/api/firearms/{id}/return` | 归还枪械（弹药数校验） |
| GET | `/api/firearms/{id}/issuances` | 某枪械的领用记录 |

## 响应约定

- 成功：返回资源对象或数组
- 失败：`{ "error": { "message": "..." } }`，配合对应 HTTP 状态码（400/404/409/500）
