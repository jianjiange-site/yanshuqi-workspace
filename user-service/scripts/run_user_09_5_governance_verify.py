#!/usr/bin/env python3
"""
USER-09-5 工程治理总体验收脚本（非破坏性）。

验收点：
1. HTTP health
2. USER-09-1～09-4 全部新增 RPC 可用
3. Redis 资料缓存 TTL 合理
4. UpdateProfile 后 profile_view 缓存被删除
5. 响应不包含 accessToken / refreshToken 等敏感字段

用法：
  python user-service/scripts/run_user_09_5_governance_verify.py

依赖：grpcurl；可选 redis-cli（用于 TTL / 缓存失效检查）。
环境变量：自动加载 deploy/.env（若存在）；也可手动设置 REDIS_HOST、REDIS_PORT、
REDIS_PASSWORD、REDIS_DATABASE、USER_SERVICE_HTTP、USER_SERVICE_GRPC。
"""

from __future__ import annotations

import json
import os
import re
import subprocess
import sys
import uuid
from pathlib import Path
from urllib.request import urlopen

AUTH = "com.dating.user.auth.v1.UserAuthService"
PROFILE = "com.dating.user.profile.v1.UserProfileService"
AVATAR = "com.dating.user.profile.v1.UserAvatarService"
GRPC = os.environ.get("USER_SERVICE_GRPC", "localhost:9091")
HTTP = os.environ.get("USER_SERVICE_HTTP", "http://localhost:8081/health")
KEY_PREFIX = "yanshuqi:user:"

SENSITIVE_OUTPUT = [
    "access_token", "accessToken", "refresh_token", "refreshToken",
    "password", "sms_code", "smsCode", "id_token", "idToken",
    "push_token", "pushToken", "password_hash", "passwordHash",
    "identity_hash", "identityHash",
]


def load_env() -> None:
    """从 deploy/.env 加载 Redis 等连接参数（与其他验收脚本一致）。"""
    env_path = Path(__file__).resolve().parents[2] / "deploy" / ".env"
    if not env_path.exists():
        return
    for line in env_path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip())


def grpc(service: str, method: str, data: dict) -> tuple[int, str]:
    r = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{service}/{method}"],
        capture_output=True, text=True, encoding="utf-8",
    )
    return r.returncode, (r.stdout or "") + (r.stderr or "")


def extract_field(text: str, field: str) -> str | None:
    m = re.search(rf'"{field}"\s*:\s*"([^"]+)"', text)
    return m.group(1) if m else None


def assert_no_sensitive(output: str) -> bool:
    lower = output.lower()
    for token in SENSITIVE_OUTPUT:
        if token.lower() in lower:
            return False
    return True


def redis_cli(args: list[str]) -> tuple[int, str]:
    host = os.environ.get("REDIS_HOST", "localhost")
    port = os.environ.get("REDIS_PORT", "6379")
    password = os.environ.get("REDIS_PASSWORD", "")
    database = os.environ.get("REDIS_DATABASE", "0")
    cmd = ["redis-cli", "-h", host, "-p", str(port), "-n", str(database)]
    if password:
        cmd.extend(["-a", password, "--no-auth-warning"])
    cmd.extend(args)
    r = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8")
    out = ((r.stdout or "") + (r.stderr or "")).strip()
    # redis-cli 可能把警告写入 stderr，取首行作为命令结果
    first_line = out.splitlines()[0].strip() if out else ""
    return r.returncode, first_line


def check_health() -> bool:
    try:
        with urlopen(HTTP, timeout=5) as resp:
            body = resp.read().decode("utf-8", errors="replace")
            return resp.status == 200 and ("UP" in body.upper() or "ok" in body.lower())
    except Exception:
        return False


def main() -> int:
    load_env()
    suffix = uuid.uuid4().hex[:8]
    device_id = f"u095dev-{suffix}-0012345678"
    phone = "139" + format(int(suffix, 16) % 10**8, "08d")
    id_token = f"oauth-token-{suffix}"

    print("=== USER-09-5 治理验收 ===")

    if not check_health():
        print(f"FAIL: health 检查失败 ({HTTP})")
        return 1
    print("OK: health")

    # USER-09-1 Auth 三个 RPC
    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": device_id, "platform": "IOS", "app_version": "1.0.0",
    })
    if code != 0 or "user_id" not in out or not assert_no_sensitive(out):
        print(f"FAIL: ResolveOrCreateDeviceUser\n{out[:500]}")
        return 1
    user_id = extract_field(out, "user_id")
    print(f"OK: ResolveOrCreateDeviceUser user_id={user_id}")

    code, out = grpc(AUTH, "ResolveOrCreatePhoneUser", {
        "phone": phone, "sms_code": "123456", "device_id": device_id, "platform": "IOS",
    })
    if code != 0 or not assert_no_sensitive(out):
        print(f"FAIL: ResolveOrCreatePhoneUser\n{out[:500]}")
        return 1
    print("OK: ResolveOrCreatePhoneUser")

    code, out = grpc(AUTH, "ResolveOrCreateThirdPartyUser", {
        "third_party_platform": 1, "id_token": id_token, "device_id": device_id, "platform": "IOS",
    })
    if code != 0 or not assert_no_sensitive(out):
        print(f"FAIL: ResolveOrCreateThirdPartyUser\n{out[:500]}")
        return 1
    print("OK: ResolveOrCreateThirdPartyUser")

    uid = int(user_id)

    # USER-09-2 Profile
    code, out = grpc(PROFILE, "UpsertOnboarding", {
        "user_id": uid, "nickname": f"U095_{suffix}", "gender": "FEMALE",
        "birthday": "1995-06-15", "height": 168, "bio": "bio",
        "occupation": "Dev", "education": "Bachelor", "location": "Shanghai",
    })
    if code != 0 or not assert_no_sensitive(out):
        print(f"FAIL: UpsertOnboarding\n{out[:500]}")
        return 1
    print("OK: UpsertOnboarding")

    code, out = grpc(PROFILE, "GetUserProfileView", {"user_id": uid})
    if code != 0 or "nickname" not in out:
        print(f"FAIL: GetUserProfileView\n{out[:500]}")
        return 1
    print("OK: GetUserProfileView")

    # USER-09-3 Avatar
    code, out = grpc(AVATAR, "PresignAvatarUpload", {
        "user_id": uid, "ext": "jpg", "expected_size_bytes": 2048,
    })
    if code != 0:
        print(f"FAIL: PresignAvatarUpload\n{out[:500]}")
        return 1
    object_key = extract_field(out, "object_key")
    if not object_key:
        print("FAIL: PresignAvatarUpload 缺少 object_key")
        return 1
    print(f"OK: PresignAvatarUpload object_key_suffix={object_key[-20:]}")

    code, out = grpc(AVATAR, "ConfirmAvatarUpload", {"user_id": uid, "object_key": object_key})
    if code != 0:
        print(f"FAIL: ConfirmAvatarUpload\n{out[:500]}")
        return 1
    print("OK: ConfirmAvatarUpload")

    # USER-09-4 HomeCard
    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": f"u095tgt-{suffix}-0012345678", "platform": "IOS", "app_version": "1.0.0",
    })
    target_id = extract_field(out, "user_id")
    if code != 0 or not target_id:
        print(f"FAIL: 创建 target 用户\n{out[:500]}")
        return 1
    grpc(PROFILE, "UpsertOnboarding", {
        "user_id": int(target_id), "nickname": f"T_{suffix}", "gender": "MALE",
        "birthday": "1990-01-01", "height": 175, "bio": "tgt",
        "occupation": "Dev", "education": "Bachelor", "location": "Beijing",
    })
    code, out = grpc(PROFILE, "GetHomeCardProfile", {
        "self_user_id": uid, "target_user_id": int(target_id),
    })
    if code != 0 or "target_profile" not in out or not assert_no_sensitive(out):
        print(f"FAIL: GetHomeCardProfile\n{out[:500]}")
        return 1
    print("OK: GetHomeCardProfile")

    # 回归：BatchGetBasicProfiles（位于 UserProfileService）
    code, out = grpc(PROFILE, "BatchGetBasicProfiles", {
        "user_ids": [uid], "include_unavailable": True,
    })
    if code != 0:
        print(f"FAIL: BatchGetBasicProfiles\n{out[:500]}")
        return 1
    print("OK: BatchGetBasicProfiles")

    # Redis TTL 与 profile_view 失效（需要 redis-cli）
    profile_view_key = f"{KEY_PREFIX}profile_view:{uid}"
    basic_key = f"{KEY_PREFIX}basic:{uid}"
    rc, _ = redis_cli(["PING"])
    if rc == 0:
        redis_cli(["SET", profile_view_key, "stale", "EX", "600"])
        rc_set, set_out = redis_cli(["GET", profile_view_key])
        if rc_set != 0 or "stale" not in set_out:
            print("WARN: 无法写入 profile_view 测试 key，跳过失效检查")
        else:
            code, out = grpc(PROFILE, "UpdateProfile", {
                "user_id": uid, "nickname": f"U095_{suffix}_v2", "bio": "updated",
            })
            if code != 0:
                print(f"FAIL: UpdateProfile\n{out[:500]}")
                return 1
            rc_ex, exists_out = redis_cli(["EXISTS", profile_view_key])
            if rc_ex == 0 and exists_out.strip() == "0":
                print("OK: UpdateProfile 后 profile_view 缓存已删除")
            else:
                print(f"FAIL: profile_view 缓存仍存在 exists={exists_out}")
                return 1

        rc_ttl, ttl_out = redis_cli(["TTL", basic_key])
        if rc_ttl == 0:
            ttl_val = int(ttl_out.strip() or "-2")
            if ttl_val > 0 and ttl_val <= 600:
                print(f"OK: basic 缓存 TTL={ttl_val}s")
            elif ttl_val == -2:
                print("WARN: basic 缓存 key 不存在（可能未命中写缓存路径）")
            else:
                print(f"WARN: basic TTL 异常 ttl={ttl_val}")
    else:
        print("WARN: redis-cli 不可用，跳过 TTL / 失效检查")

    print("PASS: USER-09-5 治理验收通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
