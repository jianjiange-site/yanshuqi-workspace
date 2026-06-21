#!/usr/bin/env python3
"""USER-09-3 Avatar Upload gRPC 非破坏性冒烟脚本。"""

from __future__ import annotations

import json
import re
import subprocess
import sys
import uuid

AUTH = "com.dating.user.auth.v1.UserAuthService"
PROFILE = "com.dating.user.profile.v1.UserProfileService"
AVATAR = "com.dating.user.profile.v1.UserAvatarService"
GRPC = "localhost:9091"


def grpc(service: str, method: str, data: dict) -> tuple[int, str]:
    r = subprocess.run(
        ["grpcurl", "-plaintext", "-d", json.dumps(data), GRPC, f"{service}/{method}"],
        capture_output=True, text=True, encoding="utf-8",
    )
    return r.returncode, (r.stdout or "") + (r.stderr or "")


def extract_field(text: str, field: str) -> str | None:
    m = re.search(rf'"{field}"\s*:\s*"([^"]+)"', text)
    return m.group(1) if m else None


def main() -> int:
    suffix = uuid.uuid4().hex[:8]
    device_id = f"u093dev-{suffix}-0012345678"

    code, out = grpc(AUTH, "ResolveOrCreateDeviceUser", {
        "device_id": device_id, "platform": "IOS", "app_version": "1.0.0",
    })
    if code != 0 or "user_id" not in out:
        print(f"FAIL: 创建设备用户失败\n{out}")
        return 1
    user_id = out.split('"user_id":')[1].split('"')[1]
    print(f"OK: device user_id={user_id}")

    code, out = grpc(AVATAR, "PresignAvatarUpload", {
        "user_id": int(user_id),
        "ext": "jpg",
        "expected_size_bytes": 2048,
    })
    if code != 0:
        print(f"FAIL: PresignAvatarUpload\n{out}")
        return 1
    object_key = extract_field(out, "object_key")
    if not object_key:
        print(f"FAIL: PresignAvatarUpload 缺少 object_key\n{out}")
        return 1
    if "presigned_url" in out and "http://" in out.lower():
        print("WARN: presigned_url 已返回（日志/输出请勿泄露完整 URL）")
    print(f"OK: PresignAvatarUpload object_key={object_key[-24:]}")

    code, out = grpc(AVATAR, "ConfirmAvatarUpload", {
        "user_id": int(user_id),
        "object_key": object_key,
    })
    if code != 0 or "original_key" not in out:
        print(f"FAIL: ConfirmAvatarUpload\n{out}")
        return 1
    print("OK: ConfirmAvatarUpload")

    code, out = grpc(PROFILE, "GetUserProfileView", {"user_id": int(user_id)})
    if code != 0 or object_key not in out:
        print(f"FAIL: GetUserProfileView 未返回新 avatar\n{out}")
        return 1
    print("OK: GetUserProfileView 返回新 AvatarVO")

    print("PASS: USER-09-3 Avatar / Upload 冒烟通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
