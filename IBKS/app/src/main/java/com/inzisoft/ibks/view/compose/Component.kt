package com.inzisoft.ibks.view.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.*
import kotlin.math.max
import kotlin.math.roundToInt

@Preview
@Composable
fun PreviewComponent() {
    var text1 by remember { mutableStateOf("Normal") }
    var text2 by remember { mutableStateOf("Normal") }

    IBKSTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Column {
                Input(value = "", onValueChange = {}, placeholder = "GuidText")
                Input(value = text1, onValueChange = { text1 = it }, placeholder = "GuidText")
                Input(value = "Disabled", onValueChange = {}, enabled = false)
                Input(value = "Error", onValueChange = {}, errorMessage = "에러입니다.")
            }

            Column {
                SearchInput(value = "", onValueChange = {}, onClear = {}, placeholder = "GuidText")
                SearchInput(
                    value = text2,
                    onValueChange = { text2 = it },
                    onClear = {},
                    placeholder = "GuidText"
                )
                SearchInput(value = "Disabled", onValueChange = {}, onClear = {}, enabled = false)
            }
        }
    }
}

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.defaultMinSize(minWidth = 240.dp, minHeight = 40.dp),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String? = null,
    shape: Shape = RectangleShape,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    errorMessage: String? = null,
    errorMessageAlignment: TextAlign? = null
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val backgroundColor = if (enabled && textFieldValue.text.isNotEmpty()) {
        MaterialTheme.colors.background1Color
    } else {
        MaterialTheme.colors.background1Color
    }

    val textStyle = defaultTextStyle(enabled)
    val guideTextStyle = defaultGuideTextStyle()

    val indicatorWidth = if (isFocused) 2.dp else 1.dp
    val color = if (isFocused) MaterialTheme.colors.point1Color else MaterialTheme.colors.sub2Color
    val indicatorColor = animateColorAsState(color, tween(durationMillis = AnimationDuration))

    Column(modifier = Modifier.wrapContentSize()) {
        BasicInput(
            value = textFieldValue,
            onValueChange = {
                textFieldValueState = it
                if (value != it.text) {
                    onValueChange(it.text)
                }
            },
            modifier = Modifier
                .background(color = backgroundColor, shape = shape)
                .drawIndicatorLine(lineWidth = indicatorWidth, color = indicatorColor.value)
                .then(modifier),
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = textStyle,
            singleLine = true,
            maxLines = 1,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            placeholder = { Text(text = placeholder ?: "", style = guideTextStyle) },
            cursorColor = MaterialTheme.colors.point1Color
        )

        errorMessage?.let {
            if (enabled && it.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(top = 8.dp).then(modifier),
                    textAlign = errorMessageAlignment,
                    color = MaterialTheme.colors.errorColor,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun BasicInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable ((Modifier) -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    cursorColor: Color
) {
    val transformedText = remember(value.annotatedString, visualTransformation) {
        visualTransformation.filter(value.annotatedString)
    }.text

    val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
        if (placeholder != null && transformedText.isEmpty()) {
            @Composable { mo ->
                Box(mo) {
                    placeholder(mo)
                }
            }
        } else null

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(cursorColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = { coreTextField ->

            val measurePolicy = remember() {
                BasicInputMeasurePolicy()
            }

            Layout(
                content = {

                    if (leading != null) {
                        Box(
                            modifier = Modifier
                                .layoutId(LeadingId)
                                .then(IconDefaultSizeModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            leading()
                        }
                    }

                    if (trailing != null) {
                        Box(
                            modifier = Modifier
                                .layoutId(TrailingId)
                                .then(IconDefaultSizeModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            trailing()
                        }
                    }

                    val padding = Modifier.padding(
                        start = if (leading != null) 0.dp else 4.dp,
                        end = if (trailing != null) 0.dp else 4.dp
                    )

                    if (decoratedPlaceholder != null) {
                        decoratedPlaceholder(
                            Modifier
                                .layoutId(PlaceholderId)
                                .then(padding)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .layoutId(TextFieldId)
                            .then(padding),
                        propagateMinConstraints = true
                    ) {
                        coreTextField()
                    }
                },
                measurePolicy = measurePolicy
            )
        }
    )
}

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
    keyboardActions: KeyboardActions = KeyboardActions(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    val shape = RoundedCornerShape(4.dp)
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val isNotEmpty = textFieldValue.text.isNotEmpty()

    val backgroundColor = MaterialTheme.colors.background1Color

    val textStyle = defaultTextStyle(enabled)
    val guideTextStyle = defaultGuideTextStyle()

    val color = if (isFocused) MaterialTheme.colors.point1Color else MaterialTheme.colors.sub2Color
    val trailingIcon =
        if (isFocused && isNotEmpty) painterResource(id = R.drawable.sicon_close) else painterResource(
            id = R.drawable.sicon_search
        )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 240.dp, minHeight = 40.dp)
            .then(modifier)
            .then(Modifier.border(BorderStroke(1.dp, color), shape))
            .background(
                color = backgroundColor,
                shape = shape
            )
            .clip(shape)
            .then(Modifier.focusable(enabled, interactionSource)),
        propagateMinConstraints = true
    ) {
        BasicInput(
            value = textFieldValue,
            onValueChange = {
                textFieldValueState = it
                if (value != it.text) {
                    onValueChange(it.text)
                }
            },
            modifier = Modifier.background(color = backgroundColor, shape = shape),
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = textStyle,
            singleLine = true,
            maxLines = 1,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            placeholder = { Text(text = placeholder ?: "", style = guideTextStyle) },
            trailing = {
                Image(
                    painter = trailingIcon,
                    contentDescription = "",
                    modifier = if (isFocused && isNotEmpty)
                        Modifier.clickable {
                            onClear()
                        }
                    else Modifier
                )
            },
            cursorColor = MaterialTheme.colors.point1Color
        )
    }
}

@Composable
fun InputNoMessage(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String? = null,
    shape: Shape = RectangleShape,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }
    val textFieldValue = textFieldValueState.copy(text = value, selection = TextRange(value.length))
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val backgroundColor = MaterialTheme.colors.background1Color

    val textStyle = defaultTextStyle(enabled)
    val guideTextStyle = defaultGuideTextStyle()

    val indicatorWidth = if (isFocused) 2.dp else 1.dp
    val color = if (isFocused) MaterialTheme.colors.point1Color else MaterialTheme.colors.sub2Color
    val indicatorColor = animateColorAsState(color, tween(durationMillis = AnimationDuration))

    BasicInput(
        value = textFieldValue,
        onValueChange = {
            textFieldValueState = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        modifier = Modifier
            .defaultMinSize(minHeight = 40.dp)
            .background(color = backgroundColor, shape = shape)
            .drawIndicatorLine(lineWidth = indicatorWidth, color = indicatorColor.value)
            .then(modifier),
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = textStyle,
        singleLine = true,
        maxLines = 1,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        placeholder = { Text(text = placeholder ?: "", style = guideTextStyle) },
        cursorColor = MaterialTheme.colors.point1Color
    )
}

@Composable
fun defaultTextStyle(enabled: Boolean) = TextStyle(
    color = if (enabled) MaterialTheme.colors.mainColor else MaterialTheme.colors.disableColor,
    fontWeight = FontWeight.Normal,
    fontSize = if (enabled) 18.sp else 16.sp,
    textAlign = TextAlign.Start,
)

@Composable
fun defaultGuideTextStyle() = TextStyle(
    color = MaterialTheme.colors.sub2Color,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    textAlign = TextAlign.Start,
)

@Composable
fun Modifier.drawIndicatorLine(lineWidth: Dp, color: Color): Modifier {
    return drawBehind {
        val strokeWidth = lineWidth.value * density
        val y = size.height - strokeWidth / 2
        drawLine(
            color,
            Offset(0f, y),
            Offset(size.width, y),
            strokeWidth
        )
    }
}

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"
internal val IconDefaultSizeModifier = Modifier.defaultMinSize(30.dp, 30.dp)

internal const val AnimationDuration = 100

internal val TextFieldPadding = 16.dp
internal val TextFieldTopPadding = 7.dp

private class BasicInputMeasurePolicy() : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        var occupiedSpaceHorizontally = 0

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val leadingPlaceable =
            measurables.find { it.layoutId == LeadingId }?.measure(looseConstraints)

        occupiedSpaceHorizontally += widthOrZero(leadingPlaceable)

        // measure trailing icon
        val trailingPlaceable = measurables.find { it.layoutId == TrailingId }
            ?.measure(looseConstraints.offset(horizontal = -occupiedSpaceHorizontally))

        occupiedSpaceHorizontally += widthOrZero(trailingPlaceable)

        val textFieldConstraints = constraints
            .copy(minHeight = 0)
            .offset(
                vertical = 0,
                horizontal = -occupiedSpaceHorizontally
            )
        val textFieldPlaceable = measurables
            .first { it.layoutId == TextFieldId }
            .measure(textFieldConstraints)

        val placeholderConstraints = textFieldConstraints.copy(minWidth = 0)
        val placeholderPlaceable = measurables
            .find { it.layoutId == PlaceholderId }
            ?.measure(placeholderConstraints)

        val width = calculateWidth(
            widthOrZero(leadingPlaceable),
            widthOrZero(trailingPlaceable),
            textFieldPlaceable.width,
            widthOrZero(placeholderPlaceable),
            constraints
        )
        val height = calculateHeight(
            textFieldPlaceable.height,
            heightOrZero(leadingPlaceable),
            heightOrZero(trailingPlaceable),
            heightOrZero(placeholderPlaceable),
            constraints,
            density
        )

        return layout(width, height) {
            leadingPlaceable?.placeRelative(
                0,
                Alignment.CenterVertically.align(leadingPlaceable.height, height)
            )
            trailingPlaceable?.placeRelative(
                width - trailingPlaceable.width,
                Alignment.CenterVertically.align(trailingPlaceable.height, height)
            )

            val textVerticalPosition =
                Alignment.CenterVertically.align(textFieldPlaceable.height, height)

            textFieldPlaceable.placeRelative(
                widthOrZero(leadingPlaceable),
                textVerticalPosition
            )

            // placeholder is placed similar to the text input above
            placeholderPlaceable?.let {
                val placeholderVerticalPosition =
                    Alignment.CenterVertically.align(placeholderPlaceable.height, height)

                it.placeRelative(
                    widthOrZero(leadingPlaceable),
                    placeholderVerticalPosition
                )
            }
        }
    }

    fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0
    fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

    private fun calculateWidth(
        leadingWidth: Int,
        trailingWidth: Int,
        textFieldWidth: Int,
        placeholderWidth: Int,
        constraints: Constraints
    ): Int {
        val middleSection = maxOf(
            textFieldWidth,
            placeholderWidth
        )
        val wrappedWidth = leadingWidth + middleSection + trailingWidth
        return max(wrappedWidth, constraints.minWidth)
    }

    private fun calculateHeight(
        textFieldHeight: Int,
        leadingHeight: Int,
        trailingHeight: Int,
        placeholderHeight: Int,
        constraints: Constraints,
        density: Float
    ): Int {
        val topBottomPadding = TextFieldTopPadding.value * density

        val inputFieldHeight = max(textFieldHeight, placeholderHeight)
        val middleSectionHeight = topBottomPadding * 2 + inputFieldHeight
        return maxOf(
            middleSectionHeight.roundToInt(),
            max(leadingHeight, trailingHeight),
            constraints.minHeight
        )
    }
}