package com.bytecause.nautichart.domain.model

import java.lang.Exception


sealed class ApiResult<T>(val data: T? = null, val exception: Exception? = null) {
    class Success<T>(data: T?) : ApiResult<T>(data)
    class Failure<T>(exception: Exception, data: T? = null) : ApiResult<T>(data, exception)
}
