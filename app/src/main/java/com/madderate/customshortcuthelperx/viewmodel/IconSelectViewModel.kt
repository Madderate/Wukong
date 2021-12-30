package com.madderate.customshortcuthelperx.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.madderate.customshortcuthelperx.base.BaseViewModel
import com.madderate.customshortcuthelperx.model.InstalledAppInfo
import com.madderate.customshortcuthelperx.utils.LogUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IconSelectViewModel(
    application: Application
) : BaseViewModel<SnapshotStateList<InstalledAppInfo>>(application) {
    companion object {
        const val DEFAULT_INDEX = -1
    }

    private val _selectedIndex = MutableStateFlow(DEFAULT_INDEX)
    val selectedIndex: StateFlow<Int> = _selectedIndex

    private val mDownloadJob = Job() + Dispatchers.IO
    private val mLoadingLocalJob = Job() + Dispatchers.Default

    private val mIconImgUrl =
        "https://c-ssl.duitang.com/uploads/ops/202110/22/20211022191052_99918.thumb.100_100_c.png_webp"

    init {
        viewModelScope.launch {
            val packageManager = application.packageManager
            if (packageManager != null) {
                getInstalledPackages(packageManager)
            }
        }
    }

    override fun onCleared() {
        mLoadingLocalJob.cancel()
        mDownloadJob.cancel()
    }

    private suspend fun getInstalledPackages(packageManager: PackageManager) =
        withContext(mLoadingLocalJob) {
            kotlin.runCatching {
                packageManager.getInstalledPackages(0).mapNotNull {
                    val iconDrawable = it?.applicationInfo?.loadIcon(packageManager)
                    val appName = it?.applicationInfo?.loadLabel(packageManager)?.toString()
                    val packageName = it?.packageName
                    InstalledAppInfo(iconDrawable).apply {
                        if (!appName.isNullOrBlank()) name = appName
                        if (!packageName.isNullOrBlank()) this.packageName = packageName
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
        when (action) {
            CreateCustomIcon ->
                createCustomIcon(selectedIndex.value)
            is Select ->
                updateSelectItem(action.position, action.shouldSelect)
        }
    }

    private fun createCustomIcon(index: Int) {
        val current = uiState.value.current
        if (current !is UiState.Success) return
        viewModelScope.launch(mDownloadJob) {
            current.runCatching {
                val item = result[index]
            }.onSuccess {
                // use shortcutManager
            }.onFailure {
                // send broadcast
            }
        }
    }

    private fun updateSelectItem(position: Int, shouldSelect: Boolean) {
        val last = uiState.value.current
        if (last !is UiState.Success) return
        val lastI = selectedIndex.value
        val lastL = last.result.takeIf { it.isNotEmpty() } ?: return
        // revert last selected
        kotlin.runCatching {
            LogUtils.d("last selected: $lastI")
            if (lastI != position)
                lastL[lastI] = lastL[lastI].apply { this.isSelected = !this.isSelected }
        }.onFailure {
            LogUtils.e("Error occured when revert last selected.", it)
        }
        // update current selected && selectedIndex
        kotlin.runCatching {
            lastL[position] = lastL[position].apply { isSelected = shouldSelect }
            if (shouldSelect) {
                _selectedIndex.value = position
            } else _selectedIndex.value = DEFAULT_INDEX
            LogUtils.d("current pos: $position, shouldSelect: $shouldSelect")
        }
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction
    object CreateCustomIcon : IconSelectUiAction

    sealed interface IconSelectUiNav
}