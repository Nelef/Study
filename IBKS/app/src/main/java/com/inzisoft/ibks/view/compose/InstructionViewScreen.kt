@file:OptIn(ExperimentalPagerApi::class)

package com.inzisoft.ibks.view.compose

import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.IBKSTheme

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewInstructionViewer() {
    IBKSTheme {
        InstructionViewer(title = "핵심설명서", visible = true, imagePaths = listOf(), onClose = { }, confirmBtnText = R.string.disclosure_information_check) {

        }
    }
}

@Composable
fun InstructionViewer(
    title: String,
    visible: Boolean,
    imagePaths: List<String>,
    confirmBtnText: Int?,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    var page by remember { mutableStateOf(0) }

    BasicBottomDialog(
        title = title,
        modifier = Modifier.fillMaxSize(0.95f),
        onClose = onClose,
        shape = RectangleShape,
        visible = visible
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp)
                .fillMaxWidth()
        ) {
            if (imagePaths.isNotEmpty()) {
                ZoomImageView(modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                    imagePath = imagePaths[page]
                )
            }

            IconButton(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterStart),
                onClick = { if (page > 0) page-- },
                shape = CircleShape,
                backgroundColor = Color(0x33000000),
                enabled = page > 0,
                icon = R.drawable.view_back,
                pressedIcon = R.drawable.view_back_on
            )

            IconButton(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterEnd),
                onClick = { if (page < (imagePaths.size)) page++ },
                shape = CircleShape,
                backgroundColor = Color(0x33000000),
                enabled = page < (imagePaths.size - 1),
                icon = R.drawable.view_next,
                pressedIcon = R.drawable.view_next_on
            )
        }

        Row {
            GrayDialogButton(
                onClick = onClose,
                text = stringResource(id = R.string.close),
                modifier = Modifier.weight(1f),
                buttonStyle = ButtonStyle.Big
            )
            ColorDialogButton(
                onClick = onConfirm,
                text = if(confirmBtnText == null) "조회" else stringResource(id = confirmBtnText),
                modifier = Modifier.weight(1f),
                buttonStyle = ButtonStyle.Big
            )
        }
    }

}