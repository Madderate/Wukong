package com.madderate.customiconhelperx.model

import android.graphics.Bitmap

data class MainResult(
    val index: Int = 0,
    val bitmap: Bitmap? = null,
    val searchKeyword: String = ""
)
