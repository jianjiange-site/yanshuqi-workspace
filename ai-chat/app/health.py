from datetime import datetime, timezone

from fastapi import APIRouter

from app.config import settings
from app.infra_health import check_all

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {
        "status": "UP",
        "service": settings.service_name,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "stage": "00-B",
    }


@router.get("/health/infra")
def infra_health() -> dict:
    body = check_all()
    body["timestamp"] = datetime.now(timezone.utc).isoformat()
    return body
