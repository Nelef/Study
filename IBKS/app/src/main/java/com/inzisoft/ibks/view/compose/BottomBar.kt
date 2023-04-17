package com.inzisoft.ibks.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.background3Color
import com.inzisoft.ibks.view.compose.theme.sub1Color

@Preview(
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0x000000
)
@Composable
fun BottomBarScreen() {

    IBKSTheme {
        Scaffold(bottomBar = {
            Column {
                PreviewDocBottomBar()
            }
        }) {
        }
    }
}

@Composable
fun PreviewDocBottomBar(

) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(color = MaterialTheme.colors.background3Color, shape = RectangleShape),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 24.dp)
                .size(80.dp, 48.dp)
                .background(Color(0xFFEFEFEF), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "2/3",
                color = MaterialTheme.colors.sub1Color,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle1
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            /*
            MainToggleButton(
                selected = true,
                onClick = { },
                icon = R.drawable.micon_plus,
                selectedIcon = R.drawable.micon_plus_on,
                text = stringResource(id = R.string.add_masking)
            )

            Spacer(modifier = Modifier.width(36.dp))

            MainToggleButton(
                selected = false,
                onClick = { },
                icon = R.drawable.micon_minus,
                selectedIcon = R.drawable.micon_minus_on,
                text = stringResource(id = R.string.remove_masking)
            )
             */
            ColorButton(
                onClick = { } ,
                text = stringResource(id = com.inzisoft.ibks.R.string.remove_masking),
                modifier = Modifier
                    .padding(start = 24.dp)
                    .width(240.dp),
                buttonStyle = ButtonStyle.Basic
            )
        }

        Row(modifier = Modifier
            .fillMaxSize()
            .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End) {

            TopBarDivider()

            IconButton(
                onClick = {  },
                modifier = Modifier.padding(horizontal = 24.dp),
                icon = com.inzisoft.ibks.R.drawable.icon_trash,
                pressedIcon = com.inzisoft.ibks.R.drawable.icon_trash_on
            )
        }
    }
}