package com.madderate.customshortcuthelperx.ui.views

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable

@Composable
fun BackButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) { BackIcon() }
}
