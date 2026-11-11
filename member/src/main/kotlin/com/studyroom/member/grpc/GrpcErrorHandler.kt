package com.studyroom.member.grpc

import com.google.rpc.ErrorInfo
import com.studyroom.member.ApplicationException
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.exception.GrpcExceptionHandler
import org.springframework.stereotype.Component
import com.google.protobuf.Any as ProtoAny
import com.google.rpc.Status as RpcStatus

// 이름이 겹쳐서 별칭을 준다. Any 는 Kotlin 의 Any 와, rpc Status 는 io.grpc.Status 와 부딪힌다.

@Component
class GrpcErrorHandler : GrpcExceptionHandler {
    override fun handleException(exception: Throwable): StatusException =
        when (exception) {
            is ApplicationException -> exception.toStatusException()
            is StatusException -> exception
            is StatusRuntimeException -> StatusException(exception.status, exception.trailers)
            else -> unexpected(exception)
        }

    private fun unexpected(exception: Throwable): StatusException {
        log.error("처리하지 못한 예외", exception)
        return Status.INTERNAL.withDescription("서버에서 요청을 처리하지 못했다").asException()
    }

    private fun ApplicationException.toStatusException(): StatusException {
        val grpcCode =
            when (code.substringAfterLast('-')) {
                "400" -> Status.Code.INVALID_ARGUMENT
                "404" -> Status.Code.NOT_FOUND
                "409" -> Status.Code.FAILED_PRECONDITION
                else -> Status.Code.INTERNAL
            }

        val status =
            RpcStatus
                .newBuilder()
                .setCode(grpcCode.value())
                .setMessage(message.orEmpty())
                .addDetails(ProtoAny.pack(ErrorInfo.newBuilder().setReason(code).build()))
                .build()
        // rpc Status 를 trailer 에 실은 StatusException 으로 바꾼다. 받는 쪽은 StatusProto.fromThrowable 로 다시 꺼낸다.
        return StatusProto.toStatusException(status)
    }

    companion object {
        // 로거는 companion object 에 클래스를 명시해 둔다. 근거: 컨벤션 04 「8.」 LOGGER_IS_STATIC
        private val log = LoggerFactory.getLogger(GrpcErrorHandler::class.java)
    }
}
