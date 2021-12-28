package com.madderate.customiconhelperx.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madderate.customiconhelperx.viewmodel.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel<T>(application: Application) : AndroidViewModel(application) {
    protected val mutableUiState = MutableStateFlow<UiState<T>>(UiState())
    val uiState: StateFlow<UiState<T>> = mutableUiState
}