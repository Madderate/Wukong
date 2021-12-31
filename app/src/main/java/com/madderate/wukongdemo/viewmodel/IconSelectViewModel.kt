package com.madderate.wukongdemo.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.bumptech.glide.load.engine.GlideException
import com.madderate.wukongdemo.base.BaseViewModel
import com.madderate.wukongdemo.model.InstalledAppInfo
import com.madderate.wukong.utils.WukongLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

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
                    .mapNotNull { mapResolveInfoToInstalledAppInfo(packageManager, it) }
            }
        }.onSuccess { infos ->
            val result = infos.toMutableStateList()
            mutableUiState.value = mutableUiState.value.copy(false, result, null)
        }.onFailure {
            WukongLog.e("Error occured when try to get packages...", it)
            mutableUiState.value = mutableUiState.value.copy(false, null, it)
        }

    private fun mapResolveInfoToInstalledAppInfo(
        packageManager: PackageManager,
        resolveInfo: ResolveInfo?
    ): InstalledAppInfo? {
        val activityInfo = resolveInfo?.activityInfo ?: return null
        val activityPackageName = activityInfo.packageName ?: return null
        val activityClzName = activityInfo.name ?: return null
        val appName = activityInfo.applicationInfo?.loadLabel(packageManager)
        val appIconDrawable = activityInfo.applicationInfo?.loadIcon(packageManager)
        val launchIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(activityPackageName, activityClzName))
            .setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            .addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
        return InstalledAppInfo(appIconDrawable).apply {
            if (!appName.isNullOrBlank()) this.name = appName.toString()
            this.launchIntent = launchIntent
        }
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
                val info = result[index]
                val context = getApplication() as Context
                // TODO: 2021/12/30 This is just a test icon image...
                val request = ImageRequest.Builder(context)
                    .data(mIconImgUrl)
                    .build()
                val bitmap = context.imageLoader.execute(request).drawable?.toBitmap()!!
                Triple(context, bitmap, info)
            }.onSuccess { (context, bitmap, info) ->
                // use shortcutManagerCompat to set shortcut
                requestPinShortcuts(context, bitmap, info)
            }.onFailure {
                // Maybe IOBE...
                // Or GlideException
                if (it is GlideException)
                    it.logRootCauses(WukongLog.TAG)
                else WukongLog.e("Error occured when try to get InstalledAppInfo...", it)
            }
        }
    }

    private suspend fun requestPinShortcuts(
        context: Context,
        iconBmp: Bitmap?,
        info: InstalledAppInfo
    ) = withContext(Dispatchers.Main) {
        try {
            val bundle = bundleOf(
                "type" to IconCompat.TYPE_BITMAP,
                "obj" to iconBmp
            )
            val icon = IconCompat.createFromBundle(bundle)
            val shortcutId = UUID.randomUUID().toString()
            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(info.name)
                .setIcon(icon)
                .setLongLived(false)
                .setIntent(info.launchIntent!!)
                .build()
            if (!ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null))
                throw IllegalStateException("Can't pin shortcut to Launcher...")
        } catch (e: Exception) {
            WukongLog.e("Error occured when try to pin shortcut...", e)
        }
    }

    private fun updateSelectItem(position: Int, shouldSelect: Boolean) {
        val last = uiState.value.current
        if (last !is UiState.Success) return
        val lastI = selectedIndex.value
        val lastL = last.result.takeIf { it.isNotEmpty() } ?: return
        // revert last selected
        kotlin.runCatching {
            WukongLog.d("last selected: $lastI")
            if (lastI != position)
                lastL[lastI] = lastL[lastI].apply { this.isSelected = !this.isSelected }
        }.onFailure {
            WukongLog.e("Error occured when revert last selected.", it)
        }
        // update current selected && selectedIndex
        kotlin.runCatching {
            lastL[position] = lastL[position].apply { isSelected = shouldSelect }
            if (shouldSelect) {
                _selectedIndex.value = position
            } else _selectedIndex.value = DEFAULT_INDEX
            WukongLog.d("current pos: $position, shouldSelect: $shouldSelect")
        }
    }

    sealed interface IconSelectUiAction
    class Select(val position: Int, val shouldSelect: Boolean) : IconSelectUiAction
    object CreateCustomIcon : IconSelectUiAction
}