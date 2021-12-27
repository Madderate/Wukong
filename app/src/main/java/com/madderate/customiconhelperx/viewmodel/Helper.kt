package com.madderate.customiconhelperx.viewmodel

data class ViewState<T>(
    val isLoading: Boolean = true,
    val result: T? = null
)

