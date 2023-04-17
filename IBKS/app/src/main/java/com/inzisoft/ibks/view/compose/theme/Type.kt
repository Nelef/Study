package com.inzisoft.ibks.view.compose.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.android.material.internal.ViewUtils.dpToPx

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = FontFamily.SansSerif,

    h1 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        textAlign = TextAlign.Start
    ),

    h2 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        textAlign = TextAlign.Start
    ),

    h3 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        textAlign = TextAlign.Start
    ),

    h4 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        textAlign = TextAlign.Start
    ),

    h5 = TextStyle(
        color = TextColor,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        textAlign = TextAlign.Start
    ),

    h6 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        textAlign = TextAlign.Start
    ),

    subtitle1 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        textAlign = TextAlign.Start
    ),

    body1 = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        textAlign = TextAlign.Start
    ),

    body2 = TextStyle(
        color = TextColor,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Start
    ),

    button = TextStyle(
        color = Color.White,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
)

val Typography.h7: TextStyle
    get() = TextStyle(
        color = TextColor,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        textAlign = TextAlign.Start
    )