package com.bytecause.domain.model


sealed class ApiResult<T>(
    val data: T? = null,
    val progress: Int? = null,
    val exception: Throwable? = null
) {
    class Success<T>(data: T?) : ApiResult<T>(data = data)
    class Progress<T>(progress: Int, exception: Throwable? = null, data: T? = null) :
        ApiResult<T>(data = data, progress = progress, exception = exception)
    class Failure<T>(exception: Throwable, data: T? = null) :
        ApiResult<T>(data = data, exception = exception)
}
