package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.madderate.customiconhelperx.IconSelectActivity
import com.madderate.customiconhelperx.base.BaseActivity
import com.madderate.customiconhelperx.base.BaseViewModel
import com.madderate.customiconhelperx.model.MainResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class MainViewModel(application: Application) : BaseViewModel<MainResult>(application) {
    private val mImageUrls = listOf(
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023858_db819.jpg",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023859_1cda2.thumb.1000_0.jpg_webp",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023900_2326a.thumb.1000_0.jpg_webp"
    )

    fun onUiAction(uiAction: MainUiAction) {
        when (uiAction) {
            is Increase -> {
                val count = uiAction.count
                val value = mutableUiState.value.result.copyEvenNull(index = count)
                mutableUiState.value = mutableUiState.value.copy(isLoading = false, value)
            }
            is Search -> {
                val keyword = uiAction.keyword
                val value = mutableUiState.value.result.copyEvenNull(searchKeyword = keyword)
                mutableUiState.value = mutableUiState.value.copy(isLoading = false, value)
            }
            is UpdateImage -> viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val context = getApplication() as Context
                    val url = mImageUrls.random()
                    Glide.with(context).asBitmap().load(url).submit().get()!!
                }.onSuccess {
                    val value = mutableUiState.value.result.copyEvenNull(bitmap = it)
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