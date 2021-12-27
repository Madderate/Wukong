package com.madderate.customiconhelperx

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madderate.customiconhelperx.base.BaseActivity
import com.madderate.customiconhelperx.ui.theme.CustomIconHelperXBasicTheme
import com.madderate.customiconhelperx.viewmodel.IconSelectViewModel

class IconSelectActivity : BaseActivity() {
    companion object {
        @JvmStatic
        fun launch(activity: BaseActivity) {
            val intent = Intent(activity, IconSelectActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val mViewModel by viewModels<IconSelectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomIconHelperXBasicTheme {
                MainContent(mViewModel)
            }
        }
    }

    @Composable
    private fun MainContent(vm: IconSelectViewModel = viewModel()) {

    }
}