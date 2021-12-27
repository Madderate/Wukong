package com.madderate.customiconhelperx

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madderate.customiconhelperx.ui.theme.CustomIconHelperXTheme
import com.madderate.customiconhelperx.viewmodel.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val GROUP_NAME = "MainActivity"
    }

    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ContentEntry() }
    }

    @Composable
    private fun ContentEntry() {
        CustomIconHelperXTheme {
            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colors.background) {
                MainContent(mViewModel)
            }
        }
    }

    @Composable
    private fun MainContent(vm: MainViewModel = viewModel()) {
        val response by vm.response.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        UserInterfaceArea(
            userInput = response.result.searchKeywork,
            index = response.result.index,
            bitmap = response.result.bitmap,
            onUiAction = vm::onUiAction,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.fillMaxSize()
        )
        SnackbarArea(snackbarHostState = snackbarHostState)
    }

    @Composable
    private fun UserInterfaceArea(
        userInput: String,
        index: Int,
        bitmap: Bitmap?,
        onUiAction: (MainViewModel.UiAction) -> Unit,
        snackbarHostState: SnackbarHostState,
        modifier: Modifier = Modifier,
        scope: CoroutineScope = rememberCoroutineScope(),
        parentArrangement: Arrangement.Vertical = Arrangement.Center,
        parentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = parentArrangement,
            horizontalAlignment = parentAlignment
        ) {
            UserInput(
                userInput,
                onUserInputChanged = onUiAction,
                onSearch = { content ->
                    scope.launch { snackbarHostState.showSnackbar(content) }
                }
            )
            TextAndButton(index, onIndexChange = onUiAction)
            BitmapArea(bitmap, onUiAction)
        }
    }

    @Composable
    private fun UserInput(
        input: String,
        onUserInputChanged: (MainViewModel.Search) -> Unit,
        onSearch: (String) -> Unit
    ) {
        TextField(
            value = input,
            onValueChange = { onUserInputChanged(MainViewModel.Search(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
            label = { Text(stringResource(R.string.user_input)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch(input)
            })
        )
    }

    @Composable
    private fun TextAndButton(index: Int, onIndexChange: (MainViewModel.Increase) -> Unit) {
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.value_is, index),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(onClick = { onIndexChange(MainViewModel.Increase(index + 1)) }) {
            Text(stringResource(id = R.string.add))
        }
    }

    @Composable
    private fun BitmapArea(bitmap: Bitmap?, onBitmapChanged: (MainViewModel.UpdateImage) -> Unit) {
        Button(
            onClick = { onBitmapChanged(MainViewModel.UpdateImage) },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.image_button))
        }
        if (bitmap == null || bitmap.isRecycled) return
        Image(
            modifier = Modifier
                .size(100.dp, 150.dp)
                .padding(vertical = 16.dp)
                .border(3.dp, Color.Cyan),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(id = R.string.image_content_description)
        )
    }

    @Composable
    private fun SnackbarArea(snackbarHostState: SnackbarHostState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) { SnackbarHost(hostState = snackbarHostState) }
    }

    //region Previews
    @Preview(
        name = "Total Preview",
        group = GROUP_NAME,
        showSystemUi = true,
        device = Devices.PIXEL_2
    )
    @Composable
    private fun TotalPreview() {
        CustomIconHelperXTheme {
            Surface(color = MaterialTheme.colors.background) {
                val snackbarHostState = remember { SnackbarHostState() }
                UserInterfaceArea(
                    userInput = "你好",
                    index = 32,
                    bitmap = null,
                    onUiAction = {},
                    snackbarHostState = snackbarHostState
                )
                SnackbarArea(snackbarHostState = snackbarHostState)
            }
        }
    }
    //endregion
}
