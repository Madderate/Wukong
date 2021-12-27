package com.madderate.customiconhelperx

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.madderate.customiconhelperx.base.BaseActivity

class IconSelectActivity : BaseActivity() {
    companion object {
        @JvmStatic
        fun launch(activity: BaseActivity) {
            val intent = Intent(activity, IconSelectActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { }
    }

}