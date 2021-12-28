package com.madderate.customiconhelperx.viewmodel

data class UiState<T>(
    val isLoading: Boolean = true,
    val result: T? = null,
    val error: Throwable? = null
)
