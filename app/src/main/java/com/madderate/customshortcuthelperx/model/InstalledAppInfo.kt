package com.madderate.customshortcuthelperx.model

import android.graphics.drawable.Drawable
import com.madderate.customshortcuthelperx.R
import com.madderate.customshortcuthelperx.base.BaseApplication

data class InstalledAppInfo(
    val iconDrawable: Drawable?,
) {
    var isSelected: Boolean = false
    var name: String = BaseApplication.appContext.getString(R.string.default_installed_app_name)
    var packageName: String = ""
}
