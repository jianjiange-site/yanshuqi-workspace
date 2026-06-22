package com.dating.post.grpc;

import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostGrpcStatusMapper;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

/**
 * Post gRPC 全局异常转换。
 */
@GrpcAdvice
@Profile("!test")
public class PostGrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(PostGrpcExceptionAdvice.class);

    @GrpcExceptionHandler(PostBusinessException.class)
    public StatusRuntimeException handlePostBusinessException(PostBusinessException exception) {
        log.warn("gRPC业务异常, errorCode={}", exception.getErrorCode().getCode());
        return PostGrpcStatusMapper.toRuntimeException(exception);
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleUnexpectedException(Exception exception) {
        if (exception instanceof PostBusinessException postBusinessException) {
            return handlePostBusinessException(postBusinessException);
        }
        log.error("gRPC未知异常, type={}", exception.getClass().getSimpleName());
        return PostGrpcStatusMapper.toInternalException();
    }
}
