package com.madderate.customiconhelperx.viewmodel.main

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val mImageUrls = listOf(
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023858_db819.jpg",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023859_1cda2.thumb.1000_0.jpg_webp",
        "https://c-ssl.duitang.com/uploads/blog/202110/30/20211030023900_2326a.thumb.1000_0.jpg_webp"
    )

    private val _response = MutableStateFlow(ViewState())
    val response: StateFlow<ViewState> = _response

    fun onUiAction(uiAction: UiAction) {
        when (uiAction) {
            is Increase -> {
                val newValue = _response.value.result.copy(index = uiAction.count)
                _response.value = _response.value.copy(result = newValue)
            }
            is Search -> {
                val newValue = response.value.result.copy(searchKeywork = uiAction.keyword)
                _response.value = _response.value.copy(result = newValue)
            }
            UpdateImage -> viewModelScope.launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val context = getApplication() as Context
                    Glide.with(context).asBitmap().load(mImageUrls.random()).submit().get()!!
                }.onSuccess {
                    val newValue = _response.value.result.copy(bitmap = it)
                    _response.value = _response.value.copy(result = newValue)
                }.onFailure {
                    Log.e(TAG, "onUiAction: load image failed", it)
                }
            }
        }
    }

    data class ViewState(
        val isLoading: Boolean = true,
        val result: MainResponseModel = MainResponseModel()
    )

    data class MainResponseModel(
        val index: Int = 0,
        val bitmap: Bitmap? = null,
        val searchKeywork: String = ""
    )

    sealed interface UiAction
    class Search(val keyword: String) : UiAction
    class Increase(val count: Int) : UiAction
    object UpdateImage : UiAction
}