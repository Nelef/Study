package com.inzisoft.ibks.view.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IBKSColorPalette = lightColors(
    primary = Point1Color,
    surface = Point1Color,
)

@Composable
fun IBKSTheme(content: @Composable () -> Unit) {
    val colors = IBKSColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

val Colors.mainColor: Color
    get() = MainColor

val Colors.background1Color: Color
    get() = Background1Color

val Colors.background2Color: Color
    get() = Background2Color

val Colors.background3Color: Color
    get() = Background3Color

val Colors.background4Color: Color
    get() = Background4Color

val Colors.sub1Color: Color
    get() = Sub1Color

val Colors.sub1ColorDis: Color
    get() = Sub1ColorDis

val Colors.sub2Color: Color
    get() = Sub2Color

val Colors.textColor: Color
    get() = TextColor

val Colors.point1Color: Color
    get() = Point1Color

val Colors.point2Color: Color
    get() = Point2Color

val Colors.point3Color: Color
    get() = Point3Color

val Colors.point4Color: Color
    get() = Point4Color

val Colors.point5Color: Color
    get() = Point5Color

val Colors.point6Color: Color
    get() = Point6Color

val Colors.point7Color: Color
    get() = Point7Color

val Colors.point8Color: Color
    get() = Point8Color

val Colors.unfocusedColor: Color
    get() = UnfocusedColor

val Colors.disableColor: Color
    get() = DisableColor

val Colors.grayColor: Color
    get() = GrayColor

val Colors.grayPressedColor: Color
    get() = GrayPressedColor

val Colors.toastColor: Color
    get() = ToastColor

val Colors.divider1Color: Color
    get() = Divider1Color

val Colors.errorColor: Color
    get() = ErrorColor