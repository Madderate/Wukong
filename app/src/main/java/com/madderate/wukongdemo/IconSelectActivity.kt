package com.madderate.wukongdemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.madderate.wukong.model.CustomShortcutInfo
import com.madderate.wukongdemo.base.BaseActivity
import com.madderate.wukongdemo.model.InstalledAppInfo
import com.madderate.wukongdemo.ui.theme.LoadingBackground
import com.madderate.wukongdemo.ui.theme.WukongBasicTheme
import com.madderate.wukongdemo.ui.views.BasicFAB
import com.madderate.wukongdemo.ui.views.BasicTopAppBars
import com.madderate.wukongdemo.ui.views.ErrorUi
import com.madderate.wukongdemo.ui.views.LoadingUi
import com.madderate.wukongdemo.viewmodel.IconSelectViewModel
import com.madderate.wukongdemo.viewmodel.UiState
import kotlinx.coroutines.launch

class IconSelectActivity : BaseActivity() {
    companion object {
        @JvmStatic
        fun launch(activity: BaseActivity) {
            val intent = Intent(activity, IconSelectActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val mViewModel by viewModels<IconSelectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WukongBasicTheme { MainContent(mViewModel) }
        }
    }

    @Composable
    private fun MainContent(vm: IconSelectViewModel) {
        val uiState by vm.uiState.collectAsState()
        val selectedIndex by vm.selectedIndex.collectAsState()
        when (val current = uiState.current) {
            is UiState.Error ->
                ErrorUi(errMsg = current.errMsg)
            is UiState.Loading ->
                LoadingUi()
            is UiState.Success -> {
                val info = current.result
                MainContentInner(
                    shortcuts = info.customShortcuts,
                    pinShortcutState = info.pinShortCutState.value,
                    selectedIndex = selectedIndex,
                    onUiAction = vm::onUiAction
                )
            }
        }
    }

    //region MainContent
    @Composable
    private fun MainContentInner(
        shortcuts: List<CustomShortcutInfo>,
        pinShortcutState: InstalledAppInfo.PinShortcutState,
        selectedIndex: Int,
        onUiAction: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        val available = selectedIndex != IconSelectViewModel.DEFAULT_INDEX
        Scaffold(
            topBar = {
                BasicTopAppBars(stringRes = R.string.icon_select_page_title) {
                    onBackPressed()
                }
            },
            floatingActionButton = {
                BasicFAB(available) {
                    onUiAction(IconSelectViewModel.CreateCustomIcon)
                }
            },
            snackbarHost = { state ->
                val msg: String = when (pinShortcutState) {
                    is InstalledAppInfo.PinShortcutState.Success -> pinShortcutState.msg
                    is InstalledAppInfo.PinShortcutState.Error -> pinShortcutState.msg
                    else -> null
                } ?: return@Scaffold
                val scope = rememberCoroutineScope()
                scope.launch {
                    state.showSnackbar(msg, null, SnackbarDuration.Short)
                }
            }
        ) {
            PackageInfoLazyColumn(shortcuts, selectedIndex, onUiAction)
            if (pinShortcutState == InstalledAppInfo.PinShortcutState.Loading) {
                LoadingUi(modifier = Modifier.background(LoadingBackground))
            }
        }
    }

    @Composable
    private fun PackageInfoLazyColumn(
        shortcuts: List<CustomShortcutInfo>,
        selectedIndex: Int,
        onUiAction: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(shortcuts) { i, shortcut ->
                val name = shortcut.targetShortcutName
                val icon: Any? = when (val iconType = shortcut.iconType) {
                    is CustomShortcutInfo.BitmapIcon -> iconType.bitmap
                    is CustomShortcutInfo.DrawableIcon -> iconType.drawable
                    CustomShortcutInfo.EmptyIcon -> null
                }
                val isSelected = i == selectedIndex
                PackageInfoRow(i, name, icon, isSelected, onUiAction)
            }
        }
    }

    @Composable
    private fun PackageInfoRow(
        index: Int,
        name: String,
        icon: Any?,
        isSelected: Boolean,
        onClick: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        val painter = rememberImagePainter(
            data = icon,
            builder = {
                placeholder(R.drawable.wukong_placeholder)
                    .error(R.drawable.wukong_placeholder)
            }
        )
        val appName: String = name
        PackageInfoRowInner(
            appName = appName,
            isSelected = isSelected,
            painter = painter,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            onClick(IconSelectViewModel.Select(index, !isSelected))
        }
    }

    @Composable
    private fun PackageInfoRowInner(
        appName: String,
        isSelected: Boolean,
        painter: ImagePainter,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        ConstraintLayout(modifier = modifier.clickable(onClick = onClick)) {
            val (icon, name, check) = createRefs()
            Image(
                painter = painter,
                contentDescription = appName,
                modifier = Modifier
                    .constrainAs(icon) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
                    .padding(vertical = 16.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = appName,
                modifier = Modifier
                    .constrainAs(name) {
                        start.linkTo(icon.end, 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(check.start, 16.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .padding(end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(check) {
                        end.linkTo(parent.end, 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
    }
    //endregion

    @Preview
    @Composable
    private fun IconSelectActivityPreview() {
        WukongBasicTheme {
            val customShortcut = CustomShortcutInfo(
                iconType = CustomShortcutInfo.EmptyIcon,
                targetPackageName = "com.madderate.wukongdemo",
                targetShortcutName = stringResource(id = R.string.app_name),
                targetActivityPackageName = "com.madderate.wukongdemo",
                targetActivityClzName = "IconSelectActivity"
            )
            val list = listOf(customShortcut)
            MainContentInner(list, InstalledAppInfo.PinShortcutState.Idle, 0) {}
        }
    }
}