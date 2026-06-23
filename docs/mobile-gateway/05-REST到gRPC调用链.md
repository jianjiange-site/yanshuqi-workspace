# mobile-gateway REST → gRPC 调用链

## 1. Auth → user-service

```text
AuthController
  → AuthServiceImpl
    → UserAuthGrpcClient (9091)
      ResolveOrCreateDeviceUser / PhoneUser / ThirdPartyUser
    → AuthDeviceManager / AuthRefreshTokenManager (gateway DB)
    → JwtIssuer / TokenBlacklistService
```

登录成功后 gateway **本地签发 JWT**，不依赖 user-service 发 token。

## 2. Profile / Upload / Home → user-service

```text
ProfileController / UploadController / HomeController
  → *BffServiceImpl
    → UserProfileGrpcClient (9091)
      UpsertOnboarding / UpdateProfile / GetHomeCardProfile
      PresignAvatarUpload / ConfirmAvatarUpload
```

callerUserId：Controller 经 `JwtCallerUserResolver` 从 JWT 解析，写入 proto request 的 userId 字段。

## 3. Match → match-service

```text
MatchController
  → MatchGrpcClient (9092)
    GetTodayFeed / Swipe / SuperHi / GetQuota / ListMatches / RecordVisit / ListVisits
```

**callerUserId 在 proto request 字段**（如 `SwipeReq.caller_user_id`），非 metadata。

SuperHi：`clientRequestId` 由 App 传入，gateway **原样透传**，不做 UUID 重写。

## 4. Post → post-service

```text
PostController
  → PostBffServiceImpl
    → PostGrpcClient (9094)
      CreatePost / GetPostDetail / DeletePost / GetRecommendFeed
      ListUserPosts / ActionLike / CreateComment / ListComments / DeleteComment
```

**callerUserId 通过 gRPC metadata**：

```text
MetadataUtils.newAttachHeadersInterceptor(
  GatewayGrpcMetadataSupport.buildMetadata(callerUserId))
```

metadata keys：`x-user-id`、`x-trace-id`（MDC）、`x-device-id`（JWT claims）。

`PostGrpcMetadataSupport` 已委托 `GatewayGrpcMetadataSupport`（@Deprecated 兼容）。

## 5. Payment → 当前 not ready，未来 payment-service

```text
PaymentController
  → PaymentBffServiceImpl (@Profile !mock & !test)
    → throw PAYMENT_SERVICE_NOT_READY (10701)
  → MockPaymentBffServiceImpl (@Profile mock | test)
    → 返回 mock VO（禁止 prod）
```

**未来**：新增 `PaymentGrpcClient` → payment-service:9095，替换 not ready 分支。

## 6. IM → 当前 not ready，未来 im-service

```text
ImTokenController
  → ImBffServiceImpl → IM_SERVICE_NOT_READY (10801) / CALL (10802)

OpenImCallbackController
  → ImBffServiceImpl.handleOpenImCallback → CALLBACK (10803)
```

**未来**：im-service 提供 token + callback gRPC；OpenIM/LiveKit secret 仅在 im-service。

## 7. gRPC 异常映射

所有 GrpcClient 捕获 `StatusRuntimeException` → `GatewayGrpcExceptionMapper.toGatewayException()`：

| gRPC Code | GatewayErrorCode |
| --- | --- |
| NOT_FOUND | USER_NOT_FOUND (10510) 等 |
| PERMISSION_DENIED | PERMISSION_DENIED (10511) |
| RESOURCE_EXHAUSTED | QUOTA_EXHAUSTED (10520) |
| FAILED_PRECONDITION | INSUFFICIENT_COINS (10521) |
| INVALID_ARGUMENT | INVALID_ARGUMENT (10400) |
| UNAUTHENTICATED | TOKEN_INVALID (10501) |
| UNAVAILABLE / DEADLINE_EXCEEDED | UPSTREAM_UNAVAILABLE (10901) |

Controller **禁止**直接捕获 gRPC 异常；由 `GlobalExceptionHandler` 转 `Result`（HTTP 200 + code）。

## 8. deadline

`gateway.grpc.deadline-seconds: 5`（application-dev.yml）。
