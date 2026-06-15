import logging
from concurrent import futures

import grpc
from grpc_health.v1 import health, health_pb2, health_pb2_grpc

from app.config import settings

logger = logging.getLogger(__name__)


def start_grpc_server() -> grpc.Server:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=4))
    health_servicer = health.HealthServicer(
        experimental_non_blocking=True,
        experimental_thread_pool=futures.ThreadPoolExecutor(max_workers=1),
    )
    health_servicer.set("", health_pb2.HealthCheckResponse.SERVING)
    health_servicer.set("ai-chat", health_pb2.HealthCheckResponse.SERVING)
    health_pb2_grpc.add_HealthServicer_to_server(health_servicer, server)
    server.add_insecure_port(f"[::]:{settings.ai_chat_grpc_port}")
    server.start()
    logger.info("gRPC health server listening on port %s", settings.ai_chat_grpc_port)
    return server
