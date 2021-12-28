package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import com.madderate.customiconhelperx.base.BaseViewModel
import com.madderate.customiconhelperx.model.InstalledAppInfo
import com.madderate.customiconhelperx.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconSelectViewModel(application: Application) :
    BaseViewModel<List<InstalledAppInfo>>(application) {
    init {
        viewModelScope.launch {
            val packageManager = application.packageManager
            if (packageManager != null) {
                getInstalledPackages(packageManager)
            }
        }
    }

    private suspend fun getInstalledPackages(packageManager: PackageManager) =
        withContext(Dispatchers.Default) {
            kotlin.runCatching {
                packageManager.getInstalledPackages(0).mapNotNull {
                    val iconDrawable = it?.applicationInfo?.loadIcon(packageManager)
                    val appName = it?.applicationInfo?.loadLabel(packageManager)?.toString()
                    InstalledAppInfo(iconDrawable = iconDrawable).apply {
                        if (!appName.isNullOrBlank()) name = appName
                    }
                }
            }
        }.onSuccess { infos ->
            mutableUiState.value = mutableUiState.value.copy(isLoading = false, result = infos)
        }.onFailure {
            LogUtils.e("Error occured when try to get packages...", it)
            mutableUiState.value = mutableUiState.value.copy(isLoading = false, result = null, error = it)
        }

    fun onUiAction(action: IconSelectUiAction) {
        when (action) {
            is Select -> {

            }
        }
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int) : IconSelectUiAction

    sealed interface IconSelectUiNav
}