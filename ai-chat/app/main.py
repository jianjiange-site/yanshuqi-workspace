import logging
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from app.config import settings
from app.grpc_server import start_grpc_server
from app.health import router as health_router
from app.logging_config import configure_logging

logger = logging.getLogger(__name__)
_grpc_server = None


@asynccontextmanager
async def lifespan(_: FastAPI):
    global _grpc_server
    configure_logging(settings.service_name)
    logger.info("Starting ai-chat stage=00-B env=%s", settings.app_env)
    _grpc_server = start_grpc_server()
    yield
    if _grpc_server is not None:
        _grpc_server.stop(grace=5)
        logger.info("gRPC server stopped")


app = FastAPI(title="ai-chat", version="0.1.0", lifespan=lifespan)
app.include_router(health_router)


def main() -> None:
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=settings.ai_chat_http_port,
        reload=False,
    )


if __name__ == "__main__":
    main()
