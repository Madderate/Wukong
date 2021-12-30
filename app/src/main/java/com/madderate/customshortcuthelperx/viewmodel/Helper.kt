package com.madderate.customshortcuthelperx.viewmodel

import com.madderate.customshortcuthelperx.R
import com.madderate.customshortcuthelperx.base.BaseApplication

data class UiState<T>(
    private val isLoading: Boolean = true,
    private val result: T? = null,
    private val error: Throwable? = null
) {
    val current: State<T>
        get() = if (isLoading) {
            Loading()
        } else if (!isLoading && result != null) {
            Success(result)
        } else {
            val errorNotNull = error ?: RuntimeException("未知错误")
            Error(errorNotNull)
        }

    sealed interface State<T>
    class Loading<T> : State<T>
    class Success<T>(val result: T) : State<T>
    class Error<T>(val e: Throwable) : State<T> {
        val errMsg: String
            get() = e.message
                ?: BaseApplication.appContext.getString(R.string.default_error_msg)
    }
}
