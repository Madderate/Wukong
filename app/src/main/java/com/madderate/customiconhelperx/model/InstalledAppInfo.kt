package com.madderate.customiconhelperx.model

import android.graphics.drawable.Drawable
import com.madderate.customiconhelperx.R
import com.madderate.customiconhelperx.base.BaseApplication

data class InstalledAppInfo(
    val iconDrawable: Drawable?,
    var name: String = BaseApplication.appContext.getString(R.string.default_installed_app_name),
    var isSelected: Boolean = false
)
