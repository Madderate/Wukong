package com.madderate.wukong

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.ContextCompat

object Wukong {
    private const val ACTION_INSTALL_SHORTCUT =
        "com.android.launcher.action.INSTALL_SHORTCUT"
    private const val INSTALL_SHORTCUT_PERMISSION =
        "com.android.launcher.permission.INSTALL_SHORTCUT"

    @JvmStatic
    fun isRequestPinShortcutSupported(context: Context): Boolean {
        // when system upper than
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return context.getSystemService(ShortcutManager::class.java)
                ?.isRequestPinShortcutSupported == true
        val permissionState =
            ContextCompat.checkSelfPermission(context, INSTALL_SHORTCUT_PERMISSION)
        if (permissionState != PackageManager.PERMISSION_GRANTED)
            return false
        return true
    }

    fun isPinShortcutWithManagerAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as? ShortcutManager
            ?: return false
        if (!shortcutManager.isRequestPinShortcutSupported)
            return false
        return true
    }

    class Builder {
        private var mIconBitmap: Bitmap? = null
        private var mPackageName: String? = null
        private var mShortLabel: String? = null

        private var mNeedBadge: Boolean = false

        /**
         * Set shortcut icon bitmap
         */
        fun setIconBitmap(iconBitmap: Bitmap?) = apply { mIconBitmap = iconBitmap }

        /**
         * Set target app's package name
         */
        fun setPackageName(packageName: String?) = apply { mPackageName = packageName }

        /**
         * Set target app's name, for example: TikTok
         */
        fun setShortLabel(shortLabel: String?) = apply { mShortLabel = shortLabel }

        /**
         * Sometimes the dynamic custom shortcut has badge
         * at the right-bottom of the icon,
         *
         * You can try to hide it by set this `true`
         */
        fun needBadge(needBadge: Boolean) = apply { mNeedBadge = needBadge }

        fun build() {

        }
    }
}