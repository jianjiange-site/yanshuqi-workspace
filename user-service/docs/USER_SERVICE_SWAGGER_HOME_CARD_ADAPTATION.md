# USER-09-4：ProfileView / HomeCard 支撑

> 模块：`user-service`  
> 范围：gRPC GetHomeCardProfile，不含 gateway、match、visit、Post/IM 聚合

## 1. 本阶段实现了什么

| gRPC RPC | 对应 Swagger REST |
|---|---|
| `GetHomeCardProfile` | `GET /api/v1/home/card?targetId=xxx` |

返回 `self_user_id` + `target_profile`（复用 `UserProfileView`）。

## 2. GetHomeCardProfile vs GetUserProfileView

| 项 | GetUserProfileView | GetHomeCardProfile |
|---|---|---|
| 场景 | 单用户资料视图 | 主页卡片（self 查看 target） |
| 入参 | user_id | self_user_id + target_user_id |
| self 校验 | 无 | 必须 ACTIVE |
| target 可见性 | 无 | 基础账号 + regulation 校验 |
| 出参 | UserProfileView | self_user_id + target UserProfileView |

## 3. HomeCardVO 字段映射

| Swagger 字段 | user-service 字段 | 处理方式 |
|---|---|---|
| selfUserId | self_user_id | 请求 self_user_id 回显 |
| target | target_profile | 复用 UserProfileView |
| target.* | ProfileViewConverter.toView | 含 AvatarVO / pending / regulationStatus / lastOpenAtMs |

## 4. target 可见性规则

1. `users.account_status = ACTIVE`（经 `UserAvailabilityEvaluator`）
2. `users.deleted = 0`
3. `user_profiles` 存在
4. `regulation_status >= 0`（负值禁止展示）
5. profile 未完成：仍返回，`pending=true`，不阻止查询

不可展示时返回 `TARGET_USER_UNAVAILABLE`（gRPC PERMISSION_DENIED），不泄露封禁细节。

## 5. 为什么不做 match 关系

Match 关系属于 match-service；user-service 只提供 target 资料快照，不做匹配判断。

## 6. 为什么不做 visit 记录

访问计数属于行为/推荐域，本阶段只做资料查询支撑。

## 7. 为什么不做 Post / IM / Payment 聚合

HomeCard REST 由 gateway BFF 聚合；user-service 仅返回 UserProfileView。

## 8. 缓存策略（方案 A）

**不新增** `yanshuqi:user:home_card:{self}:{target}` 组合缓存。

原因：资料更新时无法高效失效所有 self-target 组合。当前直查 DB + `ProfileViewConverter`；已有 `profile_view` key 在资料更新时仍会失效。

## 9. 测试与验收

```bash
cd user-service
mvn clean test
mvn clean package -DskipTests
python scripts/run_user_09_4_home_card_verify.py
```
