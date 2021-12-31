package com.madderate.wukongdemo.model

import android.content.Intent
import android.graphics.drawable.Drawable
import com.madderate.wukongdemo.R
import com.madderate.wukongdemo.base.BaseApplication

data class InstalledAppInfo(
    val iconDrawable: Drawable?,
) {
    var isSelected: Boolean = false
    var name: String = BaseApplication.appContext.getString(R.string.default_installed_app_name)
    var launchIntent: Intent? = null
}
