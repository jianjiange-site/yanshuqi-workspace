import logging
import sys


def configure_logging(service_name: str) -> None:
    logging.basicConfig(
        level=logging.INFO,
        format=(
            "%(asctime)s [%(levelname)s] "
            + service_name
            + " traceId=%(traceId)s - %(message)s"
        ),
        datefmt="%Y-%m-%dT%H:%M:%S%z",
        stream=sys.stdout,
    )
    logging.getLogger().handlers[0].addFilter(_TraceIdFilter())


class _TraceIdFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        if not hasattr(record, "traceId"):
            record.traceId = "-"
        return True
