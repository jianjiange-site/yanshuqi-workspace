# MatchService 调用链

按真实代码路径整理（gateway → match-service → 存储/外部）。

---

## 1. GetTodayFeed

```text
MatchController.getFeed
→ CallerUserResolver.resolveCallerUserId
→ MatchGrpcClient.getTodayFeed
→ MatchGrpcService.getTodayFeed
→ FeedService.getTodayFeed
  → QuotaService.isCardsExhausted / getRemainingCards
  → FeedQueueRepository.leftPop          (Redis LIST LPOP)
  → SwipedSetRepository.contains         (Redis SET 过滤)
  → ColdStartService.buildAndPush        (队列空时 D0)
    → MockCandidateClient / CandidateClient
    → FeedMergeService
    → FeedQueueRepository.rightPushAll
  → CandidateClient.batchGetProfiles     (补卡片资料)
→ MatchProtoAdapter.toMatchFeedVO
```

---

## 2. Swipe

```text
MatchController.swipe
→ MatchGrpcClient.swipe
→ MatchGrpcService.swipe
→ SwipeService.swipe
→ SwipeLockExecutor.executeWithLock      (Redisson yanshuqi:lock:match:swipe:*)
  → SwipeHistoryManager.findByUserIdAndTargetUserId  (PG 幂等)
  → QuotaService.consumeLeftSwipe / consumeRightSwipe (Redis Hash)
  → SwipeHistoryManager.insert           (PG user_swipe_history)
  → SwipedSetRepository.add              (Redis SET)
  → [RIGHT] resolveRightMatchId
→ MatchProtoAdapter.toSwipeResultVO
```

---

## 3. SuperHi

```text
MatchController.superHi
→ MatchGrpcClient.superHi
→ MatchGrpcService.superHi
→ SwipeService.superHi
→ SuperHiIdempotencyStore.find           (Redis superhi:req key)
→ SwipeLockExecutor.executeWithLock
  → QuotaService.consumeSuperHi / PaymentClient.deductCoins (mock)
  → SwipeHistoryManager.insert SUPER_HI
  → MatchCreationService.createImmediateMatch
  → SuperHiIdempotencyStore.save
```

---

## 4. BH 互划 Match

```text
SwipeService.resolveRightMatchId
→ TargetUserTypeResolver.resolve         (mock/grpc)
→ MatchCreationService.tryMutualSwipeMatch
  → SwipeHistoryManager.hasMutualRight   (PG 查 target→caller RIGHT/SUPER_HI)
  → MatchManager.insertIfAbsent            (PG match, uk pair)
  → MatchOutboxManager.createPending       (PG outbox)
  → ImClient.execute (异步由 Outbox 调度，创建时写 PENDING)
```

---

## 5. RIGHT DH 延迟 Match

```text
SwipeService.resolveRightMatchId
→ DhDelayedMatchService.scheduleDelayedMatch
→ TaskScheduler.schedule (15s–120s 随机)
  → [延迟回调] MatchCreationService.tryDelayedDhMatch
    → SwipeHistoryManager 再次检查互划
    → MatchManager.insertIfAbsent
    → MatchOutboxManager.createPending
```

---

## 6. Outbox retry

```text
MatchOutboxRetryScheduler (cron 30s)
→ MatchOutboxRetryService.processPending
  → MatchOutboxManager.listDuePending      (PG)
  → ImClient.execute                       (mock/grpc)
  → MatchOutboxManager.markDone / scheduleRetry / markDead
```

---

## 7. D1Generator

```text
D1QueueScheduler (UTC 07:00)
→ RedissonLock on yanshuqi:lock:match:d1:<yyyyMMdd>
→ D1Generator.generateForDate
  → SwipeHistoryManager.listUsersSwipedOnDate
  → [per user] PreferenceBuilder.build     (读 swipe + mock profile)
  → CandidateRecaller.recall               (MockCandidateClient)
  → D1Ranker.rank
  → D1FeedMergeService.merge (bhRatio=0.40)
  → FeedQueueRepository.deleteAndPushAll   (Redis DEL + RPUSH)
  → PreferenceRepository.save              (Redis pref key)
```

---

## 8. ListMatches

```text
MatchController.listMatches
→ MatchGrpcClient.listMatches
→ MatchGrpcService.listMatches
→ MatchQueryService.listMatches
  → MatchManager.listByUserId              (PG match 单表)
  → CandidateClient.batchGetProfiles       (mock 补 partner 资料)
→ MatchProtoAdapter.toMatchListVO
```

---

## 9. RecordVisit

```text
MatchController.recordVisit
→ MatchGrpcClient.recordVisit
→ MatchGrpcService.recordVisit
→ ProfileVisitService.recordVisit
  → ProfileVisitManager.upsertVisit        (PG profile_visit UPSERT)
```

---

## 10. ListVisits

```text
MatchController.listVisits
→ MatchGrpcClient.listVisits
→ MatchGrpcService.listVisits
→ ProfileVisitQueryService.listVisits
  → ProfileVisitManager.listVisitors       (PG profile_visit)
→ MatchProtoAdapter.toVisitListVO
```

---

## GetQuota（补充）

```text
MatchController.getQuota
→ MatchGrpcService.getQuota
→ QuotaService.buildQuotaResponse
  → SubscriptionClient.getTier           (mock/grpc)
  → QuotaRepository.getOrInit              (Redis Hash)
```
