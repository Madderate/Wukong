package com.madderate.customiconhelperx.ui.views

import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable

@Composable
fun BasicFAB(onClick: () -> Unit = {}) {
    FloatingActionButton(onClick = onClick) { DoneIcon() }
}
