# 项目描述：Mission SDK Couchbase Migration

---

## 0. 团队

Reward SDK Team：Xia Rongxin（Tech lead and Architecture）、Wang Yi-Eric（Core developer）、Song Chuanbo（Developer）、Zhang Weiguang（Developer）、 Kit Loong Liow(CNTD Matrix manager)

---

## 1. 项目简介

Mission SDK 用于存储用户活动记录和奖励积分发放信息，每天处理大量终端用户数据。用户通过做任务（如签到）换取乐天市场积分，积分可在购买商品时抵扣。目前采用 Couchbase 企业版存储数据，成本较高；项目目标为迁移到 **Google Spanner** 以降低成本。
启动时间： 2026-02

---

## 2. 核心动因

- **成本驱动**：Couchbase 企业版许可成本是主要迁移原因。
- **运维简化**：减少运维开销，利用云数据库的弹性伸缩和托管服务。
- **迁移策略**：渐进式、低风险的分阶段迁移。
- **附带收益**：除数据库更换外，构建新微服务组件，提升可扩展性、兼容性、功能迭代速度与数据一致性。

### Cost Reduction


#### Googler Spanner Cost VS Couchbase Cost

| 成本项 | Couchbase Enterprise（当前） | Google Cloud Spanner（目标） |
|--------|------------------------------|-----------------------------|
| **许可 / 基础设施** | 年许可费：**High six figures USD**（数十万美金级）<br>58 节点集群（34 KV + 24 Index/Query）：算力、存储（约 1.2 TiB 数据+索引）、网络 | 按节点 + 存储计费<br>约 5 节点 × $650/月 ≈ $3,250/月<br>存储 834 GiB × $0.30/GiB/月 ≈ $250/月 |
| **月均基础设施** | 包含在许可与 58 节点运维中（未单独拆算） | **约 $3,500–5,000 USD/月** |
| **年基础设施** | 与许可一起计入「High six figures」 | **约 $42,000–60,000 USD/年** |
| **运维人力（DevOps）** | **1.0 FTE** 专职：集群管理、容量规划、升级、监控、on-call<br>约 **$150K–200K/年**（含福利等完全成本） | **0.1–0.2 FTE**：监控与容量规划为主，无需专职 DBA<br>约 **$15K–40K/年** |
| **年总成本（估算）** | **High six figures + 基础设施 + $150K–200K DevOps**<br>（百万美金量级） | **约 $57K–100K/年**<br>（基础设施 $42K–60K + DevOps $15K–40K） |
| **相对当前节省** | 基线 | **成本显著下降**（迁移工作量较大：关系模型重构与查询改造） |


####  GCP Cost VS Rakuten One Cloud Cost
|  | Main Container Platform | Total |
| --- | --- | --- |
| **OneCloud（2026 年前）** | 30,000+ USD/月（约 11,000 USD 基础设施 + 20,000+ USD 许可） | **30,000 USD/月** |
| **OneCloud（2026 年后，仅估算）** | 46,000 USD/月（约 11,000 USD 基础设施 + 35,000+ USD 许可） | **46,000 USD/月** |
| **GCP（迁移后）** | 5,900 USD/月 | **5,900 USD/月** |

---

## 3. 技术栈

**Google Spanner 简介：** Google Cloud Spanner 是谷歌推出的全托管、全球分布式关系型数据库，具备强一致性与水平扩展能力，支持标准 SQL，无需分库分表即可跨区域扩展，适合高可用、高并发的在线业务。

- **Backend:** JDK17 + Spring Boot 3.5.x + Spring Cloud for GCP Spanner(ORM)
- **Database:** **Google Spanner** + Google原生SQL
- **CICD:** Jenkins → 未来 Google Cloud Build； Rakuten one cloud K8s → 未来 Google K8s Engine

---

## 4. 项目涉及模块（按 Confluence Task Breakdown 归纳）

| 分类 | 模块/组件 | 说明 |
|------|------------|------|
| **Backend** | Database Schema / Persistence | Spanner 表结构、DO、Repository、DAO |
| | Commons | 公共组件（如 Country enum） |
| | galaxy core-service | 核心业务（Redeem、Mission、Flow、Core Service） |
| | galaxy web | 对外 API（Actions/Claims/Auth/Points/Events 等） |
| | cupid api | Cupid 服务，先于 Galaxy 迁移 |
| | rpg-api | 代理/网关层，请求转发与流量切换 |
| | rpg-admin | 配置与任务管理，MongoDB→Spanner 同步、Mission 发布 |
| | rpg-ad | 次要应用迁移（CPI 积分转换、app driver 等） |
| | rpg-batch-point / Batches | 积分/上传/KPI 等批处理 |
| | Flow Engine | RENDER_MISSION_LIST / RENDER_MISSION_DETAIL / RENDER_LITE_MISSION_LIST 等流程 |
| | Data Migration | 数据迁移脚本与校验 |
| **Dev Ops** | Infrastructure | GCP/Spanner/GKE、网络、ACL |
| | Deployment / Operations | 部署、负载均衡、流量切换、监控 |
| | Monitoring / Configuration | 监控大盘、Feature Flags |
| **QA** | Test Planning / Testing | 测试策略、回归/集成/性能/安全测试 |

---

## 5. 各阶段开发计划（Man Day + 涉及模块）

### Phase 0：Planning & Design（当前阶段）

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Database Schema | Review & 确定 Spanner schema 设计 | 2 | Done |
| Backend | Architecture | 设计 rpg-api proxy/gateway 架构 | 3 | Done |
| Backend | Planning | 按优先级与依赖对任务分类、确定时间线与里程碑 | 2+1 | Done |
| Dev Ops | Infrastructure | Review GCP 基础设施需求 | 1 | Done |
| QA | Test Planning | 定义测试策略（集成/负载/契约测试） | 2 | Done |
| **小计** | | | **11** | |

---

### Phase 1：Database Schema & Infrastructure

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Persistence | 在 Spanner 建表、唯一约束与索引 | 1+2 | In Prog |
| Backend | Commons | Country enum 实现 | 1 | In Prog |
| Backend | Persistence | Spanner DO/Repository/DAO：client, tenant, app | 3 | In Prog |
| Backend | Persistence | Spanner DO/Repository/DAO：supplier, reward, tenantRewardMapping, redeemLog | 3 | In Prog |
| Backend | Persistence | Spanner DO/Repository/DAO：action, mission, mission_period_action_log, daily_achievement_log | 2 | In Prog |
| Backend | galaxy core-service | Refactor Redeem Service（新数据模型） | 3 | Not Started |
| Backend | galaxy web | 现有 Redeem API 适配（不改变契约） | 2 | Not Started |
| Backend | galaxy web | 新 Redeem API（新数据模型） | 2 | Not Started |
| Dev Ops | Infrastructure | 创建 Google Spanner 实例 | 1 | Not Started |
| QA | Testing | Redeem API 回归测试 | 3 | Not Started |
| **小计** | | | **20** | |

*注：GKE/CICD、ACL 等任务在 Confluence 中已划掉，未计入。*

---

### Phase 1.5：Migrate Cupid-API

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | cupid api | Couchbase 替换为 Spanner | 3 | Not Started |
| Backend | data migration | ad_mapping 数据迁移 | 3 | Not Started |
| Backend | operation | PROD 流量切换 | 1 | Not Started |
| **小计** | | | **7** | |

*注：cupid 部署到 GCP、rpg-api 切换 cupid 客户端在 Confluence 中已划掉。*

---

### Phase 1.5：rpg-admin Sync Integration

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | rpg-admin | Spanner 配置、MongoDB mapping 集合与 CRUD | 2+2 | Not Started |
| Backend | rpg-admin | OrganizationService 同步（MongoDB→Spanner client/tenant→mapping） | 3 | Not Started |
| Backend | rpg-admin | ApplicationService 同步（MongoDB→Spanner app） | 3 | Not Started |
| Backend | rpg-admin | MissionService.publish() 同步（MongoDB→Spanner mission/action） | 5 | Not Started |
| Backend | rpg-admin | 同步错误处理、初始全量同步脚本、可选 sync 状态看板 | 2+5+3 | Not Started |
| QA | Testing | Organization/Application/Mission 同步及 bootstrap 脚本测试 | 2+2+3+3 | Not Started |
| **小计** | | | **35** | |

---

### Phase 2：Mission/Action Core Logic（POC 优先）

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Mission Service | 设计并实现 log action、mission achievement、积分计算、多周期 cap、recurring 结转、mission 列表/详情、log action 流程 | 5+5+8+3+5+3+8 | Not Started |
| Backend | Flow Engine | RENDER_MISSION_LIST / RENDER_MISSION_DETAIL / RENDER_LITE_MISSION_LIST | 5+3+3 | Not Started |
| Backend | Core Service | 反欺诈校验、用户 consent、目标 mission（含批量迁移）、claim 校验 | 5+3+5 | Not Started |
| **小计** | | | **55** | |

*注：point stats、target mission batch 等部分任务未标 man days，已按可汇总项统计。*

---

### Phase 3：Core Foundation Migration

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Core Service | Native SDK auth 迁移、test mode、hybrid SDK auth、基础校验、Caffeine 缓存、Prometheus 监控 | 1+2+10+3+2+2 | Not Started |
| QA | Testing | 认证流程、租户配置加载、app 校验测试 | 3+2+2 | Not Started |
| **小计** | | | **25** | |

---

### Phase 4：API Migration

按 API 组拆分，仅列 Man Days 合计与涉及模块。

| 分组 | 涉及模块 | 主要端点示例 | Man Days 合计（约） | 状态 |
|------|----------|----------------|----------------------|------|
| Group 1: Action/Mission | Core Web | GET/POST /actions/v2|v3, lite, basic-info | 3+3+3+3+2+8+8 = 30 | Not Started |
| Group 2: Claim | Core Web | GET unclaimed, POST claim | 3+3+5+5 = 16 | Not Started |
| Group 3: Auth & Session | Core Web | init-info, member-info, signin/signout, basic-info | 3+2+2+1+2 = 10 | Not Started |
| Group 4: Points | Core Web | points/current, history, stats | 3+3+2 = 8 | Not Started |
| Group 4: Events | Core Web | present-ui, app-time, missionmapping | 2+2+2+2 = 8 | Not Started |
| Group 4: Consent & Health | Core Web | consent get, healthcheck | 1+0.5 = 1.5 | Not Started |
| **Phase 4 小计** | | | **~73.5** | |

*生产流量 Top 接口：POST /auth/v3/init-info、GET /claims/v3/.../unclaimed、POST /events/sdk/v3/app-time、GET /members/v2/basic-info、GET /members/v2/points/stats 等，迁移时优先保障。*

---

### Phase 5：Data Migration

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Data Migration | 迁移策略设计、action log 迁移脚本、redeem log 迁移、校验脚本、Staging/Prod 执行 | 3+10+5+5+3+5 | Not Started |
| QA | Testing | 配置数据与行为/奖励日志迁移校验 | 3+8 | Not Started |
| **小计** | | | **42** | |

---

### Phase 6：Testing & Validation

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | Testing | 集成测试、Spanner 负载测试、向后兼容测试、rpg-api vs reward-galaxy 契约测试、配置与数据一致性 | 10+5+5+5+3+5 | Not Started |
| QA | Testing | 全量回归、性能、安全测试 | 10+5+3 | Not Started |
| **小计** | | | **51** | |

---

### Phase 7：rpg-api Proxy 与流量切换

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | rpg-api | 代理架构设计、请求转发、响应转换、Feature Flags、监控、Mock 测试 | 3+5+3+2+2+3 | Not Started |
| Dev Ops | Deployment/Infra | reward-galaxy 部署 dev/staging/prod、负载均衡、监控大盘、Feature Flags 配置 | 2+2+2+2+3+2 | Not Started |
| Dev Ops | Operations | 1%→10%→20%→50%→100% 流量切换 | 1×5 | Not Started |
| QA | Testing | 代理转发、流量切换与回滚、数据一致性 | 3+5+3 | Not Started |
| **小计** | | | **49** | |

---

### Phase 8：Secondary Application Migrations（rpg-ad, rpg-admin）

| 角色 | 模块 | 任务概要 | 预估 | 状态 |
|------|------|----------|------|------|
| Backend | rpg-ad | CPI Point Conversion、app driver 迁移 | — | TODO |
| Backend | rpg-admin | user activities search、easy id download（过渡期需同时查 Spanner + Couchbase） | — | TODO |
| **预估** | | | **5–8 周** | |

---

### Phase 9：Batches Update（TBD）

| 角色 | 模块 | 任务概要 | Man Days | 状态 |
|------|------|----------|----------|------|
| Backend | rpg-batch-point 等 | point batch、upload batch、KPI batch（过渡期可能双写/双读） | 待估 | Not Started |
| Dev Ops | Infrastructure/Operations | 成本优化、备份与灾备 | 2+3 | Not Started |
| **小计** | | | **5+** | |

---

## 6. 阶段 Man Day 汇总（约）

| Phase | 说明 | Backend | Dev Ops | QA | 合计（约） |
|-------|------|---------|---------|-----|------------|
| 0 | Planning & Design | 8 | 1 | 2 | **11** |
| 1 | Schema & Infrastructure | 19 | 1 | 3 | **23** |
| 1.5 | Cupid-API 迁移 | 7 | — | — | **7** |
| 1.5 | rpg-admin Sync | 25 | — | 10 | **35** |
| 2 | Mission/Action Core | 55 | — | — | **55** |
| 3 | Core Foundation | 20 | — | 7 | **27** |
| 4 | API Migration | ~73.5 | — | — | **~74** |
| 5 | Data Migration | 31 | — | 11 | **42** |
| 6 | Testing & Validation | 33 | — | 18 | **51** |
| 7 | rpg-api Proxy & 流量切换 | 18 | 20 | 11 | **49** |
| 8 | 次要应用迁移 | — | — | — | **5–8 周** |
| 9 | Batches Update | TBD | 5 | — | **5+** |
| | **合计（不含 Phase 8/9 未估部分）** | **~289.5** | **~27** | **~72** | **~389** |

---

## 7. 生产 API 用量参考（Confluence 统计摘要）

- **数据来源：** access_log 单 pod 采样 × 70 推算，约 **~1.12 亿 requests/天**。
- **优先迁移：** 前 15 大接口约占约 90% 流量，包括：`POST /auth/v3/init-info`、`GET /claims/v3/.../unclaimed`、`POST /events/sdk/v3/app-time`、`GET /members/v2/basic-info`、`GET /members/v2/points/stats`、各类 `/actions/v2|v3` 等。
- **已移除/低用量：** 如 `/missions/v3`、`/status/member-merge`、部分 subscriptions/points v3 等，迁移可不覆盖或仅做兼容返回。

---