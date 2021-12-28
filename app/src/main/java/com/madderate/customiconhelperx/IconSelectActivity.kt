package com.madderate.customiconhelperx

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import com.madderate.customiconhelperx.base.BaseActivity
import com.madderate.customiconhelperx.model.InstalledAppInfo
import com.madderate.customiconhelperx.ui.theme.CustomIconHelperXBasicTheme
import com.madderate.customiconhelperx.ui.views.BasicFAB
import com.madderate.customiconhelperx.ui.views.BasicTopAppBars
import com.madderate.customiconhelperx.ui.views.ErrorIcon
import com.madderate.customiconhelperx.viewmodel.IconSelectViewModel

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
            CustomIconHelperXBasicTheme { MainContent(mViewModel) }
        }
    }

    @Composable
    private fun MainContent(vm: IconSelectViewModel) {
        val uiState by vm.uiState.collectAsState()
        val infos = uiState.result
        if (uiState.isLoading) {
            // loading
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            return
        }
        if (!uiState.isLoading && !infos.isNullOrEmpty()) {
            // successfully loaded
            MainContentInner(infos, vm::onUiAction)
            return
        }
        // load error
        val errMsg = uiState.error?.message
            ?: stringResource(id = R.string.default_error_msg)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ErrorIcon(modifier = Modifier.size(48.dp))
            Text(
                text = errMsg,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
            )
        }
    }

    //region MainContent
    @Composable
    private fun MainContentInner(
        infos: List<InstalledAppInfo>,
        onUiAction: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        Scaffold(
            topBar = {
                BasicTopAppBars(stringRes = R.string.icon_select_page_title) { onBackPressed() }
            },
            floatingActionButton = { BasicFAB() }
        ) { PackageInfoLazyColumn(infos, onUiAction) }
    }

    @Composable
    private fun PackageInfoLazyColumn(
        infos: List<InstalledAppInfo>,
        onUiAction: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(infos) { i, info -> PackageInfoRow(i, info, onUiAction) }
        }
    }

    @Composable
    private fun PackageInfoRow(
        index: Int,
        info: InstalledAppInfo,
        onClick: (IconSelectViewModel.IconSelectUiAction) -> Unit
    ) {
        val painter = rememberImagePainter(
            data = info.iconDrawable,
            builder = {
                placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
            }
        )
        val appName: String = info.name
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clickable {
                    onClick(IconSelectViewModel.Select(index, !info.isSelected))
                },
        ) {
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
            Checkbox(
                checked = info.isSelected,
                onCheckedChange = { checked ->
                    onClick(IconSelectViewModel.Select(index, checked))
                },
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
        CustomIconHelperXBasicTheme {
            val list = listOf(InstalledAppInfo(null, "madderate", true))
            MainContentInner(list) {}
        }
    }
}
