package com.dating.user.grpc;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.exception.UserGrpcStatusMapper;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC 全局异常转换，将用户域业务异常映射为 gRPC Status。
 */
@GrpcAdvice
public class UserGrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcExceptionAdvice.class);

    /**
     * 将 UserBizException 转换为 gRPC StatusRuntimeException。
     *
     * @param exception 用户域业务异常
     * @return gRPC 运行时异常
     */
    @GrpcExceptionHandler(UserBizException.class)
    public StatusRuntimeException handleUserBizException(UserBizException exception) {
        UserErrorCode errorCode = exception.getErrorCode();
        log.warn("gRPC业务异常, errorCode={}", errorCode.getCode());
        return UserGrpcStatusMapper.toRuntimeException(exception);
    }

    /**
     * 将未识别异常映射为 INTERNAL，不向调用方泄露堆栈。
     *
     * @param exception 未知异常
     * @return gRPC 运行时异常
     */
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleUnexpectedException(Exception exception) {
        if (exception instanceof UserBizException userBizException) {
            return handleUserBizException(userBizException);
        }
        log.error("gRPC未知异常, type={}", exception.getClass().getSimpleName());
        return UserGrpcStatusMapper.toInternalException();
    }
}
