package com.madderate.wukongdemo.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.madderate.wukong.Wukong
import com.madderate.wukong.model.CustomShortcutInfo
import com.madderate.wukong.utils.WukongLog
import com.madderate.wukongdemo.base.BaseViewModel
import com.madderate.wukongdemo.model.InstalledAppInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class IconSelectViewModel(
    application: Application
) : BaseViewModel<InstalledAppInfo>(application) {
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
            val packageManager = application.packageManager ?: return@launch
            getInstalledPackages(packageManager)
        }
    }

    override fun onCleared() {
        mLoadingLocalJob.cancel()
        mDownloadJob.cancel()
    }

    private suspend fun getInstalledPackages(packageManager: PackageManager) =
        withContext(mLoadingLocalJob) {
            kotlin.runCatching {
                val queryIntent = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                val flag =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PackageManager.MATCH_ALL
                    else 0
                packageManager.queryIntentActivities(queryIntent, flag)
                    .mapNotNull { mapToCustomShortcutInfo(packageManager, it) }
            }
        }.onSuccess { customShortcutInfos ->
            val result = InstalledAppInfo(customShortcutInfos)
            mutableUiState.value = mutableUiState.value.copy(false, result, null)
        }.onFailure {
            WukongLog.e("Error occured when try to get packages...", it)
            mutableUiState.value = mutableUiState.value.copy(false, null, it)
        }

    private fun mapToCustomShortcutInfo(
        packageManager: PackageManager,
        resolveInfo: ResolveInfo?
    ): CustomShortcutInfo? = try {
        val activityInfo = resolveInfo?.activityInfo!!
        val activityPackageName = activityInfo.packageName!!
        val activityClzName = activityInfo.name!!
        val appInfo = activityInfo.applicationInfo!!
        val appName = appInfo.loadLabel(packageManager)
        val appPackageName = appInfo.packageName!!
        val appIconDrawable = appInfo.loadIcon(packageManager)!!
        CustomShortcutInfo(
            appShortcutIconDrawable = appIconDrawable,
            targetPackageName = appPackageName,
            targetActivityPackageName = activityPackageName,
            targetActivityClzName = activityClzName
        ).apply {
            customShortcutName = appName.toString()
        }
    } catch (e: Exception) {
        WukongLog.e("Error occured when try to map resolveInfo to customShortcutInfo.", e)
        null
    }

    fun onUiAction(action: IconSelectUiAction) {
        when (action) {
            CreateCustomIcon ->
                createCustomIcon(selectedIndex.value)
            is Select ->
                updateSelectItem(action.position, action.shouldSelect)
            ResetPinShortcutState ->
                (uiState.value.current as? UiState.Success)?.result?.pinShortCutState?.value =
                    InstalledAppInfo.PinShortcutState.Idle
        }
    }

    private fun createCustomIcon(index: Int) {
        val current = uiState.value.current
        if (current !is UiState.Success) return
        current.result.pinShortCutState.value = InstalledAppInfo.PinShortcutState.Loading
        viewModelScope.launch(mDownloadJob) {
            current.runCatching {
                val infos = result.customShortcuts
                val info = infos[index]
                // TODO: 2021/12/30 This is just a test icon image...
                val context = getApplication() as Context
                val request = ImageRequest.Builder(context).data(mIconImgUrl).build()
                val bitmap = context.imageLoader.execute(request).drawable?.toBitmap()!!
                info.apply {
                    customIconType = CustomShortcutInfo.BitmapIcon(bitmap)
                    duplicatable = true
                }
                if (withContext(Dispatchers.Main) { !Wukong.requestPinShortcut(context, info) })
                    throw IllegalStateException("Can't pin shortcut to Launcher...")
            }.onSuccess {
                current.result.pinShortCutState.value =
                    InstalledAppInfo.PinShortcutState.Success("设置成功！")
            }.onFailure {
                // Maybe IOBE...
                val clzName = it::class.simpleName
                val exceptionMsg = it.message
                current.result.pinShortCutState.value =
                    InstalledAppInfo.PinShortcutState.Error(it, "出现错误 $clzName: $exceptionMsg")
                WukongLog.e("Error occured when try to get InstalledAppInfo...", it)
            }
        }
    }

    private fun updateSelectItem(position: Int, shouldSelect: Boolean) {
        _selectedIndex.value = if (shouldSelect) position else DEFAULT_INDEX
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction
    object CreateCustomIcon : IconSelectUiAction
    object ResetPinShortcutState : IconSelectUiAction
}