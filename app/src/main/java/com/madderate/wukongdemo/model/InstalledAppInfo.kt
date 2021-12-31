package com.madderate.wukongdemo.model

import androidx.compose.runtime.mutableStateOf
import com.madderate.wukong.model.CustomShortcutInfo

data class InstalledAppInfo(val customShortcut: CustomShortcutInfo) {
    var isSelected = mutableStateOf(false)
}
