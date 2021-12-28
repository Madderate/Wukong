package com.madderate.customiconhelperx

import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.madderate.customiconhelperx.base.BaseActivity
import com.madderate.customiconhelperx.ui.theme.CustomIconHelperXBasicTheme
import com.madderate.customiconhelperx.ui.theme.Placeholder
import com.madderate.customiconhelperx.ui.views.BasicFAB
import com.madderate.customiconhelperx.ui.views.BasicTopAppBars
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
        val response by vm.response.collectAsState()
        val packageInfos = response.result
        if (response.isLoading) {
            // loading
            return
        }
        if (!response.isLoading && !packageInfos.isNullOrEmpty()) {
            // successfully loaded
            MainContentInner(packageInfos)
            return
        }
        // load error
    }

    @Composable
    private fun MainContentInner(packageInfos: List<PackageInfo>) {
        Scaffold(
            topBar = {
                BasicTopAppBars(stringRes = R.string.icon_select_page_title) { onBackPressed() }
            },
            floatingActionButton = { BasicFAB() }
        ) { PackageInfoRecyclerView(packageInfos = packageInfos) }
    }

    @Composable
    private fun PackageInfoRecyclerView(packageInfos: List<PackageInfo>) {
        AndroidView(
            factory = { context ->
                RecyclerView(context).apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = Adapter(packageInfos) { pos, info ->
                        Toast.makeText(
                            context,
                            "info name: ${info.packageName}, pos: $pos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    addItemDecoration(ItemDecor())
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Deprecated("Performance is not good as expected...Use RecyclerView instead.")
    @Composable
    private fun PackageInfoLazyColumn(packageInfos: List<PackageInfo>) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(packageInfos) { packageInfo ->
                PackageInfoRow(packageInfo = packageInfo)
            }
        }
    }

    @Composable
    private fun PackageInfoRow(packageInfo: PackageInfo) {
        val iconBmp: Bitmap? = packageInfo.applicationInfo?.loadIcon(packageManager)?.toBitmap()
        val appName: String = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString()
            ?: ""
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 16.dp)
        ) {
            val (icon, name) = createRefs()
            if (iconBmp == null) {
                Image(
                    painter = ColorPainter(Placeholder),
                    contentDescription = appName,
                    modifier = Modifier
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.value((80 - 16 * 2).dp)
                            height = Dimension.fillToConstraints
                        }
                        .padding(start = 16.dp)
                        .aspectRatio(1f, true),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    bitmap = iconBmp.asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        }
                        .padding(start = 16.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = appName,
                modifier = Modifier
                    .constrainAs(name) {
                        start.linkTo(icon.end, 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .padding(end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }
    }

    @Preview
    @Composable
    private fun IconSelectActivityPreview() {
        CustomIconHelperXBasicTheme {
            MainContentInner(emptyList())
        }
    }

    //region For RecyclerView
    class Adapter(
        private val data: List<PackageInfo>,
        private val onItemClick: (Int, PackageInfo) -> Unit = { _, _ -> }
    ) : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_package_info, parent, false)
            return VH(itemView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val itemView = holder.itemView
            val pm = itemView.context?.packageManager ?: return
            val item = data[position]
            val imageView = itemView.findViewById<ImageView>(R.id.item_view_package_icon)
            val textView = itemView.findViewById<TextView>(R.id.item_view_application_name)
            Glide.with(imageView)
                .load(item.applicationInfo?.loadIcon(pm))
                .placeholder(R.drawable.placeholder)
                .into(imageView)
            val appName = item.applicationInfo?.loadLabel(pm) ?: "未知应用"
            textView.text = appName
            itemView.setOnClickListener { onItemClick(position, item) }
        }

        override fun getItemCount(): Int = data.size
    }

    class VH(view: View) : RecyclerView.ViewHolder(view)

    class ItemDecor : RecyclerView.ItemDecoration() {
        private val mNormalGap: Int = 8.dp.value.toInt()
        private val mSpecialGap: Int = 16.dp.value.toInt()
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val childPos = parent.getChildLayoutPosition(view)
            val total = parent.adapter?.itemCount ?: 0
            if (total == 0) {
                super.getItemOffsets(outRect, view, parent, state)
                return
            }
            if (childPos == 0) {
                outRect.top = mSpecialGap
                outRect.bottom = mNormalGap
                return
            }
            if (childPos == total - 1) {
                outRect.top = mNormalGap
                outRect.bottom = mSpecialGap
                return
            }
            outRect.top = mNormalGap
            outRect.bottom = mNormalGap
        }
    }
    //endregion
}
