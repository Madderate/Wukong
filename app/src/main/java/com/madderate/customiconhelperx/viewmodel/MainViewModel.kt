package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.madderate.customiconhelperx.IconSelectActivity
import com.madderate.customiconhelperx.base.BaseActivity
import com.madderate.customiconhelperx.model.MainResult
import com.madderate.customiconhelperx.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val imageUrls = listOf(
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023858_db819.jpg",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023859_1cda2.thumb.1000_0.jpg_webp",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023900_2326a.thumb.1000_0.jpg_webp"
    )

    private val _response = MutableStateFlow<ViewState<MainResult>>(ViewState())
    val response: StateFlow<ViewState<MainResult>> = _response

    fun onUiAction(uiAction: UiAction) {
        when (uiAction) {
            is Increase -> {
                val count = uiAction.count
                val newValue: MainResult = _response.value.result.copyEvenNull(index = count)
                _response.value = _response.value.copy(isLoading = false, newValue)
            }
            is Search -> {
                val keyword = uiAction.keyword
                val newValue = _response.value.result.copyEvenNull(searchKeyword = keyword)
                _response.value = _response.value.copy(isLoading = false, newValue)
            }
            is UpdateImage -> viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val context = getApplication() as Context
                    Glide.with(context).asBitmap().load(uiAction.url).submit().get()!!
                }.onSuccess {
                    val newValue = _response.value.result.copyEvenNull(bitmap = it)
                    _response.value = _response.value.copy(isLoading = false, newValue)
                }.onFailure {
                    LogUtils.e("onUiAction: load image failed", it)
                    _response.value = _response.value.copy(isLoading = false, result = null)
                }
            }
        }
    }

    fun onUiNav(activity: BaseActivity, uiNav: UiNav) {
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
            ?: this?.searchKeyword
            ?: ""
        return MainResult(actualIndex, actualBitmap, actualSearchKeyword)
    }

    //region From UI
    //region Action from UI
    class Search(val keyword: String) : UiAction
    class Increase(val count: Int) : UiAction
    class UpdateImage(val url: String) : UiAction
    //endregion

    //region Navigation from UI
    object ToIconSelect : UiNav
    //endregion
    //endregion
}