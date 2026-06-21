#!/usr/bin/env python3
"""USER-09-4 HomeCard gRPC 非破坏性冒烟脚本。"""

from __future__ import annotations

import json
import subprocess
import sys
import uuid

AUTH = "com.dating.user.auth.v1.UserAuthService"
PROFILE = "com.dating.user.profile.v1.UserProfileService"
GRPC = "localhost:9091"


def grpc(service: str, method: str, data: dict) -> tuple[int, str]:
    r = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{service}/{method}"],
        capture_output=True, text=True, encoding="utf-8",
    )
    return r.returncode, (r.stdout or "") + (r.stderr or "")


def main() -> int:
    suffix = uuid.uuid4().hex[:8]

    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": f"u094self-{suffix}-0012345678", "platform": "IOS", "app_version": "1.0.0",
    })
    if code != 0 or "user_id" not in out:
        print(f"FAIL: 创建 self 用户失败\n{out}")
        return 1
    self_id = out.split('"user_id":')[1].split('"')[1]

    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": f"u094tgt-{suffix}-0012345678", "platform": "IOS", "app_version": "1.0.0",
    })
    if code != 0 or "user_id" not in out:
        print(f"FAIL: 创建 target 用户失败\n{out}")
        return 1
    target_id = out.split('"user_id":')[1].split('"')[1]

    code, out = grpc(PROFILE, "UpsertOnboarding", {
        "user_id": int(target_id),
        "nickname": f"T_{suffix}",
        "gender": "FEMALE",
        "birthday": "1995-06-15",
        "height": 168,
        "bio": "bio",
        "occupation": "Dev",
        "education": "Bachelor",
        "location": "Shanghai",
    })
    if code != 0:
        print(f"FAIL: target onboarding\n{out}")
        return 1
    print(f"OK: self={self_id}, target={target_id}")

    code, out = grpc(PROFILE, "GetHomeCardProfile", {
        "self_user_id": int(self_id),
        "target_user_id": int(target_id),
    })
    if code != 0 or "target_profile" not in out or "nickname" not in out:
        print(f"FAIL: GetHomeCardProfile\n{out}")
        return 1
    if f"T_{suffix}" not in out:
        print(f"FAIL: target nickname 不匹配\n{out}")
        return 1
    print("OK: GetHomeCardProfile")

    print("PASS: USER-09-4 HomeCard 冒烟通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
