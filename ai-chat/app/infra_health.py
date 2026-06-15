"""Infrastructure health checks for Stage 00-B."""

from __future__ import annotations

import logging
from typing import Any

import psycopg2
import redis
from minio import Minio
from minio.error import S3Error

from app.config import settings

logger = logging.getLogger(__name__)


def _result(component: str, status: str, **extra: Any) -> dict[str, Any]:
    payload: dict[str, Any] = {"component": component, "status": status}
    payload.update(extra)
    return payload


def check_postgresql() -> dict[str, Any]:
    try:
        conn = psycopg2.connect(
            host=settings.postgres_host,
            port=settings.postgres_port,
            dbname=settings.postgres_database,
            user=settings.postgres_username,
            password=settings.postgres_password,
            options=f"-c search_path={settings.postgres_schema}",
            connect_timeout=5,
        )
        try:
            with conn.cursor() as cursor:
                cursor.execute("SELECT current_database(), current_schema()")
                database_name, current_schema = cursor.fetchone()
            return _result(
                "postgresql",
                "UP",
                database=database_name,
                schema=current_schema,
            )
        finally:
            conn.close()
    except Exception as exc:  # noqa: BLE001 - health endpoint reports error type
        return _result("postgresql", "DOWN", error=f"{exc.__class__.__name__}: {exc}")


def check_redis() -> dict[str, Any]:
    test_key = settings.redis_test_key
    try:
        client = redis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            password=settings.redis_password or None,
            db=settings.redis_database,
            socket_timeout=3,
            decode_responses=True,
        )
        client.set(test_key, "ping", ex=settings.redis_test_ttl_seconds)
        value = client.get(test_key)
        if value != "ping":
            raise RuntimeError("Redis read/write mismatch")
        client.delete(test_key)
        return _result("redis", "UP", testKey=test_key)
    except Exception as exc:  # noqa: BLE001
        return _result("redis", "DOWN", testKey=test_key, error=f"{exc.__class__.__name__}: {exc}")


def check_nacos() -> dict[str, Any]:
    data_id = f"{settings.service_name}-dev.yaml"
    try:
        import nacos

        client = nacos.NacosClient(
            settings.nacos_server_addr,
            namespace=settings.nacos_namespace,
            username=settings.nacos_username or None,
            password=settings.nacos_password or None,
        )
        config_status = client.get_server_status()
        return _result(
            "nacos",
            "UP" if str(config_status).upper() == "UP" else "DOWN",
            namespace=settings.nacos_namespace,
            group=settings.nacos_group,
            dataId=data_id,
            serverStatus=config_status,
        )
    except Exception as exc:  # noqa: BLE001
        return _result(
            "nacos",
            "DOWN",
            namespace=settings.nacos_namespace,
            group=settings.nacos_group,
            dataId=data_id,
            error=f"{exc.__class__.__name__}: {exc}",
        )


def check_minio() -> dict[str, Any]:
    try:
        client = Minio(
            settings.minio_endpoint.replace("https://", "").replace("http://", ""),
            access_key=settings.minio_access_key,
            secret_key=settings.minio_secret_key,
            secure=settings.minio_endpoint.startswith("https"),
            region=settings.minio_region,
        )
        exists = client.bucket_exists(settings.minio_bucket)
        return _result(
            "minio",
            "UP" if exists else "DOWN",
            bucket=settings.minio_bucket,
            pathStyleAccess=settings.minio_path_style_access,
            bucketExists=exists,
            error=None if exists else f"Bucket not found: {settings.minio_bucket}",
        )
    except S3Error as exc:
        return _result("minio", "DOWN", bucket=settings.minio_bucket, error=str(exc))
    except Exception as exc:  # noqa: BLE001
        return _result("minio", "DOWN", bucket=settings.minio_bucket, error=f"{exc.__class__.__name__}: {exc}")


def check_all() -> dict[str, Any]:
    checks = {
        "postgresql": check_postgresql(),
        "redis": check_redis(),
        "nacos": check_nacos(),
        "minio": check_minio(),
    }
    overall = "UP" if all(item.get("status") == "UP" for item in checks.values()) else "DOWN"
    return {
        "status": overall,
        "service": settings.service_name,
        "stage": "00-B",
        "checks": checks,
    }
