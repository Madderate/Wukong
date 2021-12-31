package com.madderate.wukongdemo.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.madderate.wukongdemo.IconSelectActivity
import com.madderate.wukongdemo.base.BaseActivity
import com.madderate.wukongdemo.base.BaseViewModel
import com.madderate.wukongdemo.model.MainResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class MainViewModel(
    application: Application
) : BaseViewModel<MainResult>(application) {
    private val mImageUrls = listOf(
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023858_db819.jpg",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023859_1cda2.thumb.1000_0.jpg_webp",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023900_2326a.thumb.1000_0.jpg_webp"
    )

    init {
        mutableUiState.value = UiState(isLoading = false, result = MainResult(), error = null)
    }

    fun onUiAction(uiAction: MainUiAction) {
        val latest = mutableUiState.value.current
        if (latest !is UiState.Success) return
        when (uiAction) {
            is Increase -> {
                val count = uiAction.count
                val value = latest.result.copyEvenNull(index = count)
                mutableUiState.value = mutableUiState.value.copy(isLoading = false, value)
            }
            is Search -> {
                val keyword = uiAction.keyword
                val value = latest.result.copyEvenNull(searchKeyword = keyword)
                mutableUiState.value = mutableUiState.value.copy(isLoading = false, value)
            }
            is UpdateImage -> viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val context = getApplication() as Context
                    val url = mImageUrls.random()
                    Glide.with(context).asBitmap().load(url).submit().get()!!
                }.onSuccess {
                    val value = latest.result.copyEvenNull(bitmap = it)
                    mutableUiState.value = mutableUiState.value.copy(isLoading = false, value)
                }
            }
        }
    }

    fun onUiNav(activity: BaseActivity, uiNav: MainUiNav) {
        when (uiNav) {
            ToIconSelect -> IconSelectActivity.launch(activity)
        }
    }

    private fun MainResult?.copyEvenNull(
        index: Int = 0,
        bitmap: Bitmap? = null,
        searchKeyword: String = ""
    ): MainResult {
        val actualIndex = if (this != null) max(this.index, index) else index
        val actualBitmap = bitmap?.takeIf { !it.isRecycled }
            ?: this?.bitmap?.takeIf { !it.isRecycled }
        val actualSearchKeyword = searchKeyword.takeIf { it.isNotBlank() }
            ?: this?.searchKeyword ?: ""
        return MainResult(actualIndex, actualBitmap, actualSearchKeyword)
    }

    //region From UI
    //region Action from UI
    sealed interface MainUiAction
    class Search(val keyword: String) : MainUiAction
    class Increase(val count: Int) : MainUiAction
    object UpdateImage : MainUiAction
    //endregion

    //region Navigation from UI
    sealed interface MainUiNav
    object ToIconSelect : MainUiNav
    //endregion
    //endregion
}