package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import com.madderate.customiconhelperx.utils.LogUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IconSelectViewModel(application: Application) : AndroidViewModel(application) {
    private val _response = MutableStateFlow<ViewState<List<PackageInfo>>>(ViewState())
    val response: StateFlow<ViewState<List<PackageInfo>>> = _response

    init {
        val packageManager = application.packageManager
        if (packageManager != null) {
            getInstalledPackages(packageManager)
        }
    }

    private fun getInstalledPackages(packageManager: PackageManager) {
        kotlin.runCatching {
            packageManager.getInstalledPackages(0).mapNotNull { it }
        }.onSuccess { packages ->
            LogUtils.d("count: ${packages.size}")
            _response.value = _response.value.copy(isLoading = false, result = packages)
        }.onFailure {
            LogUtils.e("Error occured when try to get packages...", it)
        }
    }

    fun onUiAction(action: IconSelectUiAction) {
        when (action) {
            is Select -> {

            }
        }
    }

    sealed interface IconSelectUiAction
    class Select(val packageInfo: PackageInfo) : IconSelectUiAction

    sealed interface IconSelectUiNav
}