package com.madderate.customshortcuthelperx.ui.views

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun BasicTopAppBars(@StringRes stringRes: Int, onNavClick: () -> Unit = {}) {
    TopAppBar(
        title = { Text(text = stringResource(id = stringRes)) },
        navigationIcon = { BackButton(onNavClick) }
    )
}
