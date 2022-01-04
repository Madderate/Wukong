package com.madderate.wukong

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.madderate.wukong.model.CustomShortcutInfo
import com.madderate.wukong.utils.WukongLog

object Wukong {
    private const val ACTION_INSTALL_SHORTCUT =
        "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val PERMISSION_INSTALL_SHORTCUT =
        "com.android.launcher.permission.INSTALL_SHORTCUT"
    private const val EXTRA_DUPLICATE =
        "duplicate"

    @JvmStatic
    fun isRequestPinShortcutSupported(context: Context): Boolean {
        // when system upper than Oreo, use ShortcutManager directly to check
        // whether system can pin shortcut to Launcher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return context.getSystemService(ShortcutManager::class.java)
                ?.isRequestPinShortcutSupported == true

        // else we need to check whether shortcut permission has granted
        val permissionState =
            ContextCompat.checkSelfPermission(context, PERMISSION_INSTALL_SHORTCUT)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    @JvmOverloads
    @JvmStatic
    @Suppress("DEPRECATION")
    fun requestPinShortcut(
        context: Context,
        shortcut: CustomShortcutInfo,
        callback: IntentSender? = null
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return try {
                context.getSystemService(ShortcutManager::class.java)
                    ?.requestPinShortcut(shortcut.toShortcutInfo(context), callback) == true
            } catch (e: Exception) {
                WukongLog.e("Error occured when try to pin shortcut via ShortcutManager", e)
                false
            }
        }
        if (!isRequestPinShortcutSupported(context)) return false
        try {
            val appName = shortcut.customAppName
            val duplicate = shortcut.duplicatable
            val launchIntent = shortcut.intent
            val shortcutBmp = shortcut.customAppIconBmp
                ?: throw NullPointerException("Icon bitmap shouldn't be null!")
            val broadcastIntent = Intent(ACTION_INSTALL_SHORTCUT)
                .putExtra(EXTRA_DUPLICATE, duplicate)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
                .putExtra(Intent.EXTRA_SHORTCUT_ICON, shortcutBmp)
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            if (callback == null) {
                context.sendBroadcast(broadcastIntent)
                return true
            }
            context.sendOrderedBroadcast(broadcastIntent, null, object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    kotlin.runCatching { callback.sendIntent(p0, 0, null, null, null) }
                }
            }, null, Activity.RESULT_OK, null, null)
        } catch (e: Exception) {
            WukongLog.e("Error occured when try to send broadcast...", e)
            return false
        }
        return true
    }
}
