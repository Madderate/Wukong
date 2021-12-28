package com.madderate.customiconhelperx.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.madderate.customiconhelperx.base.BaseViewModel
import com.madderate.customiconhelperx.model.InstalledAppInfo
import com.madderate.customiconhelperx.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconSelectViewModel(application: Application) :
    BaseViewModel<SnapshotStateList<InstalledAppInfo>>(application) {
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
            mutableUiState.value =
                mutableUiState.value.copy(isLoading = false, result = infos.toMutableStateList())
        }.onFailure {
            LogUtils.e("Error occured when try to get packages...", it)
            mutableUiState.value = mutableUiState.value.copy(isLoading = false, result = null, error = it)
        }

    fun onUiAction(action: IconSelectUiAction) {
        when (action) {
            is Select -> {
                val list =  mutableUiState.value.result ?: return
                val i = action.position
                kotlin.runCatching {
                    list[i] = list[i].apply { isSelected = action.shouldSelect }
                }
            }
        }
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction

    sealed interface IconSelectUiNav
}