package com.madderate.wukong.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

data class CustomShortcutInfo(
    val originAppIconDrawable: Drawable?,
    val originAppName: CharSequence,
    /**
     * The package name of target app
     */
    val packageName: String,

    val activityPkgName: String,
    val activityClzName: String,

    val action: String = Intent.ACTION_MAIN,
    val category: String = Intent.CATEGORY_DEFAULT
) {

    /**
     * Main shortcut icon, display at the center of the shortcut.
     */
    var customAppIconBmp: Bitmap? = null

    /**
     * The shortcut name of target app,
     * which means you can change the visible name of the app's shortcut.
     *
     * If you just want the default app name,
     * access [packageItemInfo.loadLabel()][android.content.pm.PackageItemInfo.loadLabel]
     *
     */
    var customAppName: String = ""
        get() {
            if (field.isBlank()) {
                field = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    "$originAppName"
                } else "\u2061$originAppName\u2061"
            }
            return field
        }

    /**
     * Whether shortcut can be created repeatly.
     */
    var duplicatable: Boolean = true

    /**
     * Flags for activity's launchMode when user click the custom shortcut.
     */
    var flags: Int =
        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY

    val intent: Intent
        get() = Intent(action)
            .addCategory(category)
            .setComponent(ComponentName(packageName, activityClzName))
            .setFlags(flags)

    @RequiresApi(25)
    fun toShortcutInfo(context: Context): ShortcutInfo {
        val id = UUID.randomUUID().toString()
        return ShortcutInfo.Builder(context, id)
            .setShortLabel(originAppName)
            .setIntents(arrayOf(intent))
            .setIcon(getIcon())
            .build()
    }

    /**
     * [Icon] created with [customAppIconBmp].
     */
    @RequiresApi(25)
    private fun getIcon(): Icon? = Icon.createWithBitmap(customAppIconBmp)
}
