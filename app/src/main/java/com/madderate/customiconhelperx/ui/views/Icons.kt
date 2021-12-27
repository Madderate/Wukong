package com.madderate.customiconhelperx.ui.views

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.madderate.customiconhelperx.R

@Composable
fun DoneIcon() {
    Icon(
        imageVector = Icons.Outlined.Done,
        contentDescription = stringResource(id = R.string.done)
    )
}

@Composable
fun BackIcon() {
    Icon(
        imageVector = Icons.Filled.ArrowBack,
        contentDescription = stringResource(id = R.string.back)
    )
}
