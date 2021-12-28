package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madderate.customiconhelperx.model.InstalledAppInfo
import com.madderate.customiconhelperx.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconSelectViewModel(application: Application) : AndroidViewModel(application) {
    private val _response = MutableStateFlow<UiState<List<InstalledAppInfo>>>(UiState())
    val response: StateFlow<UiState<List<InstalledAppInfo>>> = _response

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
        }.onSuccess { packages ->
            _response.value = _response.value.copy(isLoading = false, result = packages)
        }.onFailure {
            LogUtils.e("Error occured when try to get packages...", it)
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