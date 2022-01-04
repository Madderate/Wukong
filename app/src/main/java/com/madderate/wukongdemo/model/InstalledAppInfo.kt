package com.madderate.wukongdemo.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.madderate.wukong.model.CustomShortcutInfo

data class InstalledAppInfo(val customShortcuts: List<CustomShortcutInfo>) {
    val pinShortCutState: MutableState<PinShortcutState> = mutableStateOf(PinShortcutState.Idle)
    val selectedIndex: MutableState<Int> = mutableStateOf(-1)
    
    sealed interface PinShortcutState {
        object Idle : PinShortcutState
        object Loading : PinShortcutState
        class Success(val msg: String) : PinShortcutState
        class Error(val e: Throwable, val msg: String) : PinShortcutState
    }
}
