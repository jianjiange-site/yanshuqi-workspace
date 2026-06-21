package com.dating.match.grpc;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchGrpcStatusMapper;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

/**
 * Match gRPC 全局异常转换。
 */
@GrpcAdvice
@Profile("!test")
public class MatchGrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(MatchGrpcExceptionAdvice.class);

    @GrpcExceptionHandler(MatchBizException.class)
    public StatusRuntimeException handleMatchBizException(MatchBizException exception) {
        log.warn("gRPC业务异常, errorCode={}", exception.getErrorCode().getCode());
        return MatchGrpcStatusMapper.toRuntimeException(exception);
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleUnexpectedException(Exception exception) {
        if (exception instanceof MatchBizException matchBizException) {
            return handleMatchBizException(matchBizException);
        }
        log.error("gRPC未知异常, type={}", exception.getClass().getSimpleName());
        return MatchGrpcStatusMapper.toInternalException();
    }
}
