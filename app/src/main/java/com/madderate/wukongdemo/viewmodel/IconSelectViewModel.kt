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
import java.util.*

class IconSelectViewModel(
    application: Application
) : BaseViewModel<InstalledAppInfo>(application) {
    companion object {
        const val DEFAULT_INDEX = -1
    }

    private val mDownloadJob = Job() + Dispatchers.IO
    private val mLoadingLocalJob = Job() + Dispatchers.Default

    // TODO: 2021/12/30 This is just a test icon image...
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
            originAppIconDrawable = appIconDrawable,
            originAppName = appName,
            packageName = appPackageName,
            activityPkgName = activityPackageName,
            activityClzName = activityClzName
        )
    } catch (e: Exception) {
        WukongLog.e("Error occured when try to map resolveInfo to customShortcutInfo.", e)
        null
    }


    //region UiAction
    fun onUiAction(action: IconSelectUiAction) {
        when (action) {
            CreateCustomIcon ->
                createCustomIcon()
            is Select ->
                updateSelectItem(action.position, action.shouldSelect)
            ResetPinShortcutState ->
                (uiState.value.current as? UiState.Success)?.result?.pinShortCutState?.value =
                    InstalledAppInfo.PinShortcutState.Idle
        }
    }

    private fun createCustomIcon() {
        val current = uiState.value.current
        if (current !is UiState.Success) return
        current.result.pinShortCutState.value = InstalledAppInfo.PinShortcutState.Loading

        viewModelScope.launch(mDownloadJob) {
            val context = getApplication() as Context
            current.runCatching {
                val index = result.selectedIndex.value
                val imageRequest = ImageRequest.Builder(context)
                    .data(mIconImgUrl)
                    .build()
                val info = result.customShortcuts[index].apply {
                    customAppIconBmp =
                        context.imageLoader.execute(imageRequest).drawable?.toBitmap()
                    duplicatable = true
                }
                val isPinned = Wukong.requestPinShortcut(context, info)
                if (!isPinned)
                    throw RuntimeException("Pin shortcut failed.")
            }.onSuccess {
                current.result.pinShortCutState.value =
                    InstalledAppInfo.PinShortcutState.Success("快捷方式已固定")
            }.onFailure { e ->
                val eClzName = e.javaClass.simpleName
                val eMsg = e.message
                current.result.pinShortCutState.value =
                    InstalledAppInfo.PinShortcutState.Error(e, "固定快捷方式时发生错误 $eClzName: $eMsg")
            }
        }
    }

    private fun updateSelectItem(position: Int, shouldSelect: Boolean) {
        val current = uiState.value.current
        if (current !is UiState.Success) return
        current.result.selectedIndex.value = if (shouldSelect) position else DEFAULT_INDEX
    }
    //endregion

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction
    object CreateCustomIcon : IconSelectUiAction
    object ResetPinShortcutState : IconSelectUiAction
}