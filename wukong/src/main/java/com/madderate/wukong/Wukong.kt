package com.madderate.wukong

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.madderate.wukong.model.CustomShortcutInfo
import com.madderate.wukong.utils.WukongLog

object Wukong {
    private const val ACTION_INSTALL_SHORTCUT =
        "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val INSTALL_SHORTCUT_PERMISSION =
        "com.android.launcher.permission.INSTALL_SHORTCUT"

    @JvmStatic
    fun isRequestPinShortcutSupported(context: Context): Boolean {
        // when system upper than Oreo, use ShortcutManager directly to check
        // whether system can pin shortcut to Launcher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return context.getSystemService(ShortcutManager::class.java)
                ?.isRequestPinShortcutSupported == true

        // else we need to check whether shortcut permission has granted
        val permissionState =
            ContextCompat.checkSelfPermission(context, INSTALL_SHORTCUT_PERMISSION)
        if (permissionState != PackageManager.PERMISSION_GRANTED)
            return false
        return true
    }

    @JvmStatic
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
            val launchIntent = shortcut.intents?.last() ?: return false
            val shortcutBmp: Bitmap? = when (val iconType = shortcut.iconType) {
                is CustomShortcutInfo.BitmapIcon -> iconType.bitmap
                is CustomShortcutInfo.DrawableIcon -> iconType.drawable.toBitmap()
                CustomShortcutInfo.EmptyIcon -> null
            }
            val broadcastIntent = Intent(ACTION_INSTALL_SHORTCUT)
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.targetShortcutName)
            if (shortcutBmp != null && !shortcutBmp.isRecycled) {
                broadcastIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, shortcutBmp)
            }
            if (callback == null) {
                context.sendBroadcast(broadcastIntent)
                return true
            }
            context.sendOrderedBroadcast(broadcastIntent, null, object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    kotlin.runCatching { callback.sendIntent(p0, 0, null, null, null) }
                }
            }, null, Activity.RESULT_OK, null, null)
            return true
        } catch (e: Exception) {
            WukongLog.e("Error occred when try to pin shortcut", e)
            return false
        }
    }
}
