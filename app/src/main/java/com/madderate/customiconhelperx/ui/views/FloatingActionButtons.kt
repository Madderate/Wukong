package com.madderate.customiconhelperx.ui.views

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BasicFAB(available: Boolean, onClick: () -> Unit = {}) {
    FloatingActionButton(
        onClick = { if (available) onClick() },
        backgroundColor = if (available) {
            MaterialTheme.colors.secondary
        } else MaterialTheme.colors.secondaryVariant
    ) {
        DoneIcon()
    }
}
