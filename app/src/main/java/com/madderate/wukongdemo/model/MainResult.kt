package com.madderate.wukongdemo.model

import android.graphics.Bitmap

data class MainResult(
    val index: Int = 0,
    val bitmap: Bitmap? = null,
    val searchKeyword: String = ""
)
