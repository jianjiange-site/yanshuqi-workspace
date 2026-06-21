#!/usr/bin/env python3
"""
USER-09-6 最终验收脚本（非破坏性）。

复用 USER-09-5 governance 验收能力，覆盖 Auth / Profile / Avatar / HomeCard / 治理项。
不打印敏感字段，不执行 FLUSHDB / FLUSHALL。

用法：
  python user-service/scripts/run_user_09_final_verify.py

依赖：grpcurl；user-service 已启动（8081/9091）；可选 redis-cli（读 deploy/.env）。
"""

from __future__ import annotations

import runpy
import sys
from pathlib import Path

SCRIPT = Path(__file__).resolve().parent / "run_user_09_5_governance_verify.py"


def main() -> int:
    print("=== USER-09-6 最终验收（复用 USER-09-5 governance） ===")
    if not SCRIPT.exists():
        print(f"FAIL: 缺少 {SCRIPT}")
        return 1
    result = runpy.run_path(str(SCRIPT), run_name="__verify__")
    main_fn = result.get("main")
    if callable(main_fn):
        return int(main_fn())
    print("FAIL: governance 脚本缺少 main()")
    return 1


if __name__ == "__main__":
    sys.exit(main())
