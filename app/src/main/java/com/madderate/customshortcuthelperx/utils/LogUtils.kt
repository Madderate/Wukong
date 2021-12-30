package com.madderate.customshortcuthelperx.utils

import android.util.Log

object LogUtils {
    private const val TAG = "CustomShortcutHelperX"

    @JvmStatic
    fun d(msg: String) {
        Log.d(TAG, msg)
    }

    @JvmStatic
    fun w(msg: String, e: Throwable) {
        Log.w(TAG, msg, e)
    }

    @JvmStatic
    fun e(msg: String, e: Throwable) {
        Log.e(TAG, msg, e)
    }
}
