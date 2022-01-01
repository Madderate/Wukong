package com.madderate.wukong.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.madderate.wukong.R
import java.util.*

data class CustomShortcutInfo(
    val appShortcutIconDrawable: Drawable?,
    /**
     * The package name of target app
     */
    val targetPackageName: String,

    val targetActivityPackageName: String,
    val targetActivityClzName: String,

    val targetAction: String = Intent.ACTION_MAIN,
    val targetCategory: String = Intent.CATEGORY_DEFAULT,

    /**
     * Badge in shortcut, often display at the right-bottom corner of the shortcut.
     */
    val badgeType: BadgeType? = null
) {

    /**
     * Main shortcut icon, display at the center of the shortcut.
     */
    var customIconType: IconType = EmptyIcon

    /**
     * The shortcut name of target app,
     * which means you can change the visible name of the app's shortcut.
     *
     * If you just want the default app name,
     * access [packageItemInfo.loadLabel()][android.content.pm.PackageItemInfo.loadLabel]
     *
     */
    var customShortcutName: String = ""

    /**
     * Whether shortcut can be created repeatly.
     */
    var duplicatable: Boolean = false

    /**
     * Flags for activity's launchMode when user click the custom shortcut.
     */
    var flags: Int =
        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY

    /**
     * [Icon] created with [customIconType].
     */
    @RequiresApi(25)
    private fun getIcon(context: Context): Icon {
        return when (customIconType) {
            is BitmapIcon ->
                Icon.createWithBitmap((customIconType as BitmapIcon).bitmap)
            is DrawableIcon ->
                Icon.createWithBitmap((customIconType as DrawableIcon).drawable.toBitmap())
            EmptyIcon -> Icon.createWithResource(context, R.drawable.wukong_placeholder)
        }
    }

    /**
     * [intents] for launch activities.
     */
    var intents: MutableList<Intent>? = null
        private set
        get() {
            if (field == null) {
                val defaultLaunchIntent = Intent(targetAction)
                    .addCategory(targetCategory)
                    .setComponent(ComponentName(targetPackageName, targetActivityClzName))
                    .setFlags(flags)
                field = mutableListOf(defaultLaunchIntent)
            }
            return field
        }

    fun setIntent(intent: Intent) {
        if (intents.isNullOrEmpty()) {
            intents = mutableListOf(intent)
        } else {
            intents?.clear()
            intents?.add(intent)
        }
    }

    fun addIntent(intent: Intent) {
        if (intents == null) {
            setIntent(intent)
            return
        }
        intents?.add(intent)
    }

    @RequiresApi(25)
    fun toShortcutInfo(context: Context): ShortcutInfo {
        val id = UUID.randomUUID().toString()
        return ShortcutInfo.Builder(context, id)
            .setShortLabel(customShortcutName)
            .setIntents(intents!!.toTypedArray())
            .setIcon(getIcon(context))
            .build()
    }

    sealed interface IconType
    object EmptyIcon : IconType
    class BitmapIcon(var bitmap: Bitmap) : IconType
    class DrawableIcon(var drawable: Drawable) : IconType

    sealed interface BadgeType
    class BitmapBadge(var bitmap: Bitmap) : BadgeType
    class DrawableBadge(var drawable: Drawable) : BadgeType
}
