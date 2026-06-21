#!/usr/bin/env python3
"""USER-09-2 Profile / Onboarding gRPC 非破坏性冒烟脚本。"""

from __future__ import annotations

import json
import subprocess
import sys
import uuid

PROFILE = "com.dating.user.profile.v1.UserProfileService"
AUTH = "com.dating.user.auth.v1.UserAuthService"
GRPC = "localhost:9091"


def grpc(service: str, method: str, data: dict) -> tuple[int, str]:
    r = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{service}/{method}"],
        capture_output=True, text=True, encoding="utf-8",
    )
    return r.returncode, (r.stdout or "") + (r.stderr or "")


def main() -> int:
    suffix = uuid.uuid4().hex[:8]
    device_id = f"u092dev-{suffix}-0012345678"

    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": device_id, "platform": "IOS", "app_version": "1.0.0",
    })
    if code != 0 or "user_id" not in out:
        print(f"FAIL: 创建设备用户失败\n{out}")
        return 1
    user_id = out.split('"user_id":')[1].split('"')[1]
    print(f"OK: device user_id={user_id}")

    code, out = grpc(PROFILE, "UpsertOnboarding", {
        "user_id": int(user_id),
        "nickname": f"U09_{suffix}",
        "gender": "FEMALE",
        "birthday": "1995-06-15",
        "height": 168,
        "bio": "hello",
        "occupation": "Engineer",
        "education": "Bachelor",
        "location": "Shanghai",
    })
    if code != 0 or "profile" not in out:
        print(f"FAIL: UpsertOnboarding\n{out}")
        return 1
    print("OK: UpsertOnboarding")

    code, out = grpc(PROFILE, "GetUserProfileView", {"user_id": int(user_id)})
    if code != 0 or "nickname" not in out:
        print(f"FAIL: GetUserProfileView\n{out}")
        return 1
    print("OK: GetUserProfileView")

    code, out = grpc(PROFILE, "UpdateProfile", {
        "user_id": int(user_id),
        "nickname": f"U09_{suffix}_v2",
        "bio": "updated",
        "location": "Beijing",
    })
    if code != 0 or "success" not in out:
        print(f"FAIL: UpdateProfile\n{out}")
        return 1
    print("OK: UpdateProfile")

    print("PASS: USER-09-2 Profile / Onboarding 冒烟通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
