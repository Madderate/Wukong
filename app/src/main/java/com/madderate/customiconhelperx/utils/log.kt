package com.madderate.customiconhelperx.utils

import android.util.Log

private const val TAG = "CustomIconHelperX"

fun Log.d(msg: String) {
    Log.d(TAG, msg)
}

fun Log.w(msg: String, e: Throwable) {
    Log.w(TAG, msg, e)
}

fun Log.e(msg: String, e: Throwable) {
    Log.e(TAG, msg, e)
}