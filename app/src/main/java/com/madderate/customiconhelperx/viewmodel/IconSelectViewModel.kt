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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IconSelectViewModel(
    application: Application
) : BaseViewModel<SnapshotStateList<InstalledAppInfo>>(application) {
    private val _hasAppSelected = MutableStateFlow(false)
    val hasAppSelected: StateFlow<Boolean> = _hasAppSelected

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
            val result = infos.toMutableStateList()
            mutableUiState.value = mutableUiState.value.copy(false, result, null)
        }.onFailure {
            LogUtils.e("Error occured when try to get packages...", it)
            mutableUiState.value = mutableUiState.value.copy(false, null, it)
        }

    fun onUiAction(action: IconSelectUiAction) {
        if (action is Select) {
            updateSelectState(action.position, action.shouldSelect)
            return
        }
    }

    private fun updateSelectState(position: Int, shouldSelect: Boolean) {
        val oldUiState = mutableUiState.value.current
        if (oldUiState !is UiState.Success) return
        val list = oldUiState.result.takeIf { it.isNotEmpty() } ?: return
        kotlin.runCatching {
            list[position] = list[position].apply { isSelected = shouldSelect }
        }
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction

    sealed interface IconSelectUiNav
}