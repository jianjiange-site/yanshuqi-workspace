#!/usr/bin/env python3
"""
USER-09-1 登录来源 gRPC 非破坏性冒烟脚本。

用法：
  python user-service/scripts/run_user_09_1_auth_login_source_verify.py

依赖：grpcurl；user-service gRPC 已启动（默认 localhost:9091）。
"""

from __future__ import annotations

import json
import re
import subprocess
import sys
import uuid

AUTH = "com.dating.user.auth.v1.UserAuthService"
GRPC = "localhost:9091"
BLACKLIST = ["password", "sms_code", "smsCode", "id_token", "idToken", "push_token", "pushToken"]


def extract_user_id(output: str) -> str | None:
    m = re.search(r'"user_id":\s*"(\d+)"', output) or re.search(r'"userId":\s*"(\d+)"', output)
    return m.group(1) if m else None


def grpc(method: str, data: dict) -> tuple[int, str]:
    result = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{AUTH}/{method}"],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    output = (result.stdout or "") + (result.stderr or "")
    return result.returncode, output


def assert_no_sensitive(output: str) -> bool:
    lower = output.lower()
    for token in BLACKLIST:
        if token.lower() in lower:
            return False
    return True


def main() -> int:
    suffix = uuid.uuid4().hex[:8]
    device_id = f"u09dev-{suffix}-0012345678"
    phone = "139" + format(int(suffix, 16) % 10**8, "08d")
    id_token = f"oauth-token-{suffix}"

    checks = [
        ("ResolveOrCreateDeviceUser", {
            "device_id": device_id,
            "platform": "IOS",
            "app_version": "1.0.0",
        }),
        ("ResolveOrCreateDeviceUser", {
            "device_id": device_id,
            "platform": "IOS",
            "app_version": "1.0.0",
        }),
        ("ResolveOrCreatePhoneUser", {
            "phone": phone,
            "sms_code": "123456",
            "device_id": device_id,
            "platform": "IOS",
        }),
        ("ResolveOrCreateThirdPartyUser", {
            "third_party_platform": 1,
            "id_token": id_token,
            "device_id": device_id,
            "platform": "IOS",
        }),
    ]

    device_user_ids: list[str] = []
    for method, payload in checks:
        code, output = grpc(method, payload)
        if code != 0:
            print(f"FAIL: {method} grpc 调用失败\n{output}")
            return 1
        if not assert_no_sensitive(output):
            print(f"FAIL: {method} 输出可能包含敏感字段")
            return 1
        user_id = extract_user_id(output)
        if not user_id:
            print(f"FAIL: {method} 未返回 user_id\n{output}")
            return 1
        if method == "ResolveOrCreateDeviceUser":
            device_user_ids.append(user_id)
        print(f"OK: {method} user_id={user_id}")

    if len(device_user_ids) >= 2 and device_user_ids[0] != device_user_ids[1]:
        print("FAIL: 同一 deviceId 复登 user_id 不一致")
        return 1

    print("PASS: USER-09-1 Auth 登录来源冒烟通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
