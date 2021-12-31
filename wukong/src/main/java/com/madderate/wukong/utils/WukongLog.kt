package com.madderate.wukong.utils

import android.util.Log
import com.madderate.wukong.BuildConfig

object WukongLog {
    const val TAG = "Wukong"

    @JvmStatic
    fun d(msg: String, force: Boolean = false) {
        if (!BuildConfig.DEBUG && !force) return
        Log.d(TAG, msg)
    }

    @JvmStatic
    fun w(msg: String, e: Throwable, force: Boolean = false) {
        if (!BuildConfig.DEBUG && !force) return
        Log.w(TAG, msg, e)
    }

    @JvmStatic
    fun e(msg: String, e: Throwable) {
        Log.e(TAG, msg, e)
    }
}
