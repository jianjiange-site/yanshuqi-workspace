# MatchService 阶段 8：外部服务联调前检查清单

## 1. 当前 mock 外部依赖清单

| 接口 | Mock 实现 | 职责 |
|------|-----------|------|
| `CandidateClient` | `MockCandidateClient` | D0/D1 候选召回、batch profile |
| `TargetUserTypeResolver` | `MockTargetUserTypeResolver` | 解析 target BH/DH |
| `SubscriptionClient` | `MockSubscriptionClient` | 查询订阅档位 |
| `PaymentClient` | `MockPaymentClient` | SuperHi 金币扣减 |
| `ImClient` | `MockImClient` | match 后 IM outbox 副作用 |

业务 service **只依赖接口**，通过 Spring `@ConditionalOnProperty` 切换 mock / grpc 实现。

## 2. mock / grpc 配置切换

`application-dev.yml` / `application-test.yml`：

```yaml
app:
  match:
    external:
      user-client-mode: mock      # mock | grpc
      payment-client-mode: mock
      im-client-mode: mock

grpc:
  client:
    user-service:
      address: static://127.0.0.1:9091
      negotiation-type: plaintext
    payment-service:
      address: static://127.0.0.1:9095
      negotiation-type: plaintext
    im-service:
      address: static://127.0.0.1:9093
      negotiation-type: plaintext
```

切换为 grpc 时，将对应 `*-client-mode` 改为 `grpc`，并确保目标服务已启动。

## 3. user-service 需要补的 RPC

**已有可复用：**

- `BatchGetRecommendProfiles` → `CandidateClient.batchGetProfiles`
- `BatchGetRecommendProfiles.user_type` → `TargetUserTypeResolver`

**仍缺（D0/D1 召回）：**

- `listDhCandidates(callerUserId, limit)`
- `listBhCandidates` / `nearbyUsers`
- 推荐字段：`distanceKm`、`race`、`beautyScore`、`lastActiveAt` 等需 proto 扩展或专用 RPC

## 4. payment-service 需要补的 RPC

当前 `proto/payment` **不存在**。match-service 占位 client 会抛 `EXTERNAL_RPC_NOT_IMPLEMENTED`：

- `GetSubscription(userId)` → `SubscriptionClient`
- `ConsumeCoins(userId, amount, reason, clientRequestId)` → `PaymentClient`

## 5. im-service 需要补的 RPC

当前 `proto/im` **不存在**。grpc 模式下 `ImGrpcClient.execute` 返回 **false**（触发 outbox 重试，不静默成功）：

- `EnsureConversation(userIdA, userIdB)`
- `SendSystemMessage(toUserId, payload)`
- `TriggerDhOpening(dhUserId, targetUserId, matchBizId)`

## 6. 本地联调启动顺序

1. user-service（gRPC 9091）
2. payment-service（gRPC 9095，待实现）
3. im-service（gRPC 9093，待实现）
4. match-service（REST 8082 / gRPC 9092）
5. mobile-gateway

## 7. 联调验证接口

- `GET /api/v1/match/feed`
- `POST /api/v1/match/swipe`
- `POST /api/v1/match/super-hi`
- `GET /api/v1/match/matches`

## 8. 当前不能真实联调的原因

1. payment / im proto 尚未定义，grpc client 为占位实现。
2. user-service 无候选召回 RPC，D0/D1 `list*Candidates` 在 grpc 模式会失败。
3. `BatchGetRecommendProfiles` 字段不足以完整支撑 D1 打分（distance、race、活跃度等需扩展）。
4. 本阶段默认 **mock 模式**，保证 dev/test 无外部依赖即可跑通。

## 9. 验收脚本

```bash
bash scripts/verify-match-stage8.sh
# 或
powershell -File scripts/verify-match-stage8.ps1
```
