package com.madderate.wukongdemo

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
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
                    selectedIndex = info.selectedIndex.value,
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
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()
        Scaffold(
            scaffoldState = scaffoldState,
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
        ) {
            PackageInfoLazyColumn(shortcuts, selectedIndex, onUiAction)
            if (pinShortcutState == InstalledAppInfo.PinShortcutState.Loading) {
                LoadingUi(modifier = Modifier.background(LoadingBackground))
            }
            val msg: String? = when (pinShortcutState) {
                is InstalledAppInfo.PinShortcutState.Success -> pinShortcutState.msg
                is InstalledAppInfo.PinShortcutState.Error -> pinShortcutState.msg
                else -> null
            }
            if (msg != null) {
                scope.launch {
                    onUiAction(IconSelectViewModel.ResetPinShortcutState)
                    scaffoldState.snackbarHostState.showSnackbar(msg, null, SnackbarDuration.Short)
                }
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
                val name = shortcut.originAppName
                val isSelected = i == selectedIndex
                PackageInfoRow(i, name, shortcut.originAppIconDrawable, isSelected, onUiAction)
            }
        }
    }

    @Composable
    private fun PackageInfoRow(
        index: Int,
        name: CharSequence,
        appShortcutDrawable: Drawable?,
        isSelected: Boolean,
        onClick: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        val painter = rememberImagePainter(
            data = appShortcutDrawable,
            builder = {
                placeholder(R.drawable.wukong_placeholder)
                    .error(R.drawable.wukong_placeholder)
            }
        )
        PackageInfoRowInner(
            appName = name,
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
        appName: CharSequence,
        isSelected: Boolean,
        painter: ImagePainter,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        ConstraintLayout(modifier = modifier.clickable(onClick = onClick)) {
            val (icon, name, check) = createRefs()
            Image(
                painter = painter,
                contentDescription = appName.toString(),
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
                text = appName.toString(),
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
                originAppIconDrawable = null,
                originAppName = stringResource(id = R.string.app_name),
                packageName = "com.madderate.wukongdemo",
                activityPkgName = "com.madderate.wukongdemo",
                activityClzName = "IconSelectActivity"
            )
            val list = listOf(customShortcut)
            MainContentInner(list, InstalledAppInfo.PinShortcutState.Success("成了！"), 0) {}
        }
    }
}
