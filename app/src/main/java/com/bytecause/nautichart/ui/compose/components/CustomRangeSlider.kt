package com.bytecause.nautichart.ui.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * A custom slider composable that allows selecting a value within a given range.
 *
 * @param modifier The modifier to be applied to the slider.
 * @param gap The spacing between indicators on the slider.
 * @param showIndicator Determines whether to show indicators on the slider.
 * @param showLabel Determines whether to show a label above the slider.
 * @param enabled Determines whether the slider is enabled for interaction.
 * @param thumb The composable used to display the thumb of the slider.
 * @param track The composable used to display the track of the slider.
 * @param indicator The composable used to display the indicators on the slider.
 * @param label The composable used to display the label above the slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRangeSlider(
    rangeSliderState: RangeSliderState,
    modifier: Modifier = Modifier,
    gap: Int = Gap,
    showIndicator: Boolean = false,
    showLabel: Boolean = false,
    thumb: @Composable (thumbValue: Int) -> Unit = { thumbValue ->
        CustomSliderDefaults.Thumb(
            thumbValue = thumbValue.toString()
        )
    },
    track: @Composable (RangeSliderState) -> Unit = {
        CustomSliderDefaults.Track(rangeSliderState = it)
    },
    indicator: @Composable (indicatorValue: Int) -> Unit = { indicatorValue ->
        CustomSliderDefaults.Indicator(indicatorValue = indicatorValue.toString())
    },
    label: @Composable (labelValue: Int) -> Unit = { labelValue ->
        CustomSliderDefaults.Label(labelValue = labelValue.toString())
    }
) {

    val itemCount =
        (rangeSliderState.valueRange.endInclusive - rangeSliderState.valueRange.start).roundToInt()

    /*Box(modifier = modifier) {
        RangeSlider(
            state = rangeSliderState,
            modifier = Modifier
                .fillMaxWidth()
                .layoutId(CustomSliderComponents.SLIDER),
            startThumb = {
                thumb(interactionSource, rangeSliderState.activeRangeStart.toInt())
            },
            endThumb = {
                thumb(interactionSource, rangeSliderState.activeRangeEnd.toInt())
            },
            track = { rangeSliderState ->
                track(rangeSliderState)
            },
            enabled = true
        )*/
    Layout(
        modifier = modifier,
        measurePolicy = customSliderMeasurePolicy(
            itemCount = itemCount,
            gap = gap,
            value = rangeSliderState.activeRangeStart,
            startValue = rangeSliderState.valueRange.start
        ),
        content = {
            if (showLabel)
                Label(
                    modifier = Modifier.layoutId(CustomSliderComponents.LABEL),
                    value = rangeSliderState.activeRangeStart,
                    label = label
                )

            Box(modifier = Modifier.layoutId(CustomSliderComponents.THUMB)) {
                thumb(20f.roundToInt())
            }

            RangeSlider(
                state = rangeSliderState,
                modifier = Modifier
                    .fillMaxWidth()
                    .layoutId(CustomSliderComponents.SLIDER),
                startThumb = {
                    thumb(rangeSliderState.activeRangeStart.toInt())
                },
                endThumb = {
                    thumb(rangeSliderState.activeRangeEnd.toInt())
                },
                track = { rangeSliderState ->
                    track(rangeSliderState)
                },
                enabled = true
            )

            /*Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .layoutId(CustomSliderComponents.SLIDER),
                value = value,
                valueRange = valueRange,
                steps = steps,
                onValueChange = { onValueChange(it) },
                thumb = {
                    thumb(value.roundToInt())
                },
                track = { track(it) },
                enabled = enabled
            )*/

            if (showIndicator)
                Indicator(
                    modifier = Modifier.layoutId(CustomSliderComponents.INDICATOR),
                    valueRange = rangeSliderState.valueRange,
                    gap = gap,
                    indicator = indicator
                )
        })
}

@Composable
private fun Label(
    modifier: Modifier = Modifier,
    value: Float,
    label: @Composable (labelValue: Int) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        label(value.roundToInt())
    }
}

@Composable
private fun Indicator(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    gap: Int,
    indicator: @Composable (indicatorValue: Int) -> Unit
) {
    // Iterate over the value range and display indicators at regular intervals.
    for (i in valueRange.start.roundToInt()..valueRange.endInclusive.roundToInt() step gap) {
        Box(
            modifier = modifier
        ) {
            indicator(i)
        }
    }
}

private fun customSliderMeasurePolicy(
    itemCount: Int,
    gap: Int,
    value: Float,
    startValue: Float
) = MeasurePolicy { measurables, constraints ->
    // Measure the thumb component and calculate its radius.
    val thumbPlaceable = measurables.first {
        it.layoutId == CustomSliderComponents.THUMB
    }.measure(constraints)
    val thumbRadius = (thumbPlaceable.width / 2).toFloat()

    val indicatorPlaceables = measurables.filter {
        it.layoutId == CustomSliderComponents.INDICATOR
    }.map { measurable ->
        measurable.measure(constraints)
    }
    val indicatorHeight = indicatorPlaceables.maxByOrNull { it.height }?.height ?: 0

    val sliderPlaceable = measurables.first {
        it.layoutId == CustomSliderComponents.SLIDER
    }.measure(constraints)
    val sliderHeight = sliderPlaceable.height

    val labelPlaceable = measurables.find {
        it.layoutId == CustomSliderComponents.LABEL
    }?.measure(constraints)
    val labelHeight = labelPlaceable?.height ?: 0

    // Calculate the total width and height of the custom slider layout
    val width = sliderPlaceable.width
    val height = labelHeight + sliderHeight + indicatorHeight

    // Calculate the available width for the track (excluding thumb radius on both sides).
    val trackWidth = width - (2 * thumbRadius)

    // Calculate the width of each section in the track.
    val sectionWidth = trackWidth / itemCount
    // Calculate the horizontal spacing between indicators.
    val indicatorSpacing = sectionWidth * gap

    // To calculate offset of the label, first we will calculate the progress of the slider
    // by subtracting startValue from the current value.
    // After that we will multiply this progress by the sectionWidth.
    // Add thumb radius to this resulting value.
    val labelOffset = (sectionWidth * (value - startValue)) + thumbRadius

    layout(width = width, height = height) {
        var indicatorOffsetX = thumbRadius
        // Place label at top.
        // We have to subtract the half width of the label from the labelOffset,
        // to place our label at the center.
        labelPlaceable?.placeRelative(
            x = (labelOffset - (labelPlaceable.width / 2)).roundToInt(),
            y = 0
        )

        // Place slider placeable below the label.
        sliderPlaceable.placeRelative(x = 0, y = labelHeight)

        // Place indicators below the slider.
        indicatorPlaceables.forEach { placeable ->
            // We have to subtract the half width of the each indicator from the indicatorOffset,
            // to place our indicators at the center.
            placeable.placeRelative(
                x = (indicatorOffsetX - (placeable.width / 2)).roundToInt(),
                y = labelHeight + sliderHeight
            )
            indicatorOffsetX += indicatorSpacing
        }
    }
}

/**
 * Object to hold defaults used by [CustomRangeSlider]
 */
object CustomSliderDefaults {

    /**
     * Composable function that represents the thumb of the slider.
     *
     * @param thumbValue The value to display on the thumb.
     * @param modifier The modifier for styling the thumb.
     * @param color The color of the thumb.
     * @param size The size of the thumb.
     * @param shape The shape of the thumb.
     */
    @Composable
    fun Thumb(
        thumbValue: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        size: Dp = ThumbSize,
        shape: Shape = CircleShape,
        content: @Composable () -> Unit = {
            Box(
                modifier = modifier
                    .thumb(size = size, shape = shape)
                    .background(color)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = thumbValue,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {
        content()
    }

    /**
     * Composable function that represents the track of the slider.
     *
     * @param rangeSliderState The state of the slider.
     * @param modifier The modifier for styling the track.
     * @param height The height of the track.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Track(
        rangeSliderState: RangeSliderState,
        modifier: Modifier = Modifier,
        height: Dp = TrackHeight
    ) {

        val inactiveTrackColor = MaterialTheme.colorScheme.secondary
        val activeTrackColor = MaterialTheme.colorScheme.primary
        val inactiveTickColor = MaterialTheme.colorScheme.onTertiary
        val activeTickColor = MaterialTheme.colorScheme.tertiary

        Canvas(
            modifier
                .fillMaxWidth()
                .height(height)
        ) {
            drawTrack(
                tickFractions(rangeSliderState.steps),
                coercedActiveRangeStartAsFraction(rangeSliderState),
                coercedActiveRangeEndAsFraction(rangeSliderState),
                inactiveTrackColor,
                activeTrackColor,
                inactiveTickColor,
                activeTickColor
            )
        }

        /*Box(
            modifier = modifier
                .track(height = height, shape = shape)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .progress(
                        sliderState = sliderState,
                        height = height,
                        shape = shape
                    )
                    .background(progressColor)
            )
        }*/
    }

    private fun tickFractions(steps: Int): FloatArray = stepsToTickFractions(steps)
    private fun stepsToTickFractions(steps: Int): FloatArray {
        return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
    }

    private fun calcFraction(a: Float, b: Float, pos: Float) =
        (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

    @OptIn(ExperimentalMaterial3Api::class)
    private fun coercedActiveRangeStartAsFraction(rangeSliderState: RangeSliderState) =
        calcFraction(
            rangeSliderState.valueRange.start,
            rangeSliderState.valueRange.endInclusive,
            rangeSliderState.activeRangeStart
        )

    @OptIn(ExperimentalMaterial3Api::class)
    private fun coercedActiveRangeEndAsFraction(rangeSliderState: RangeSliderState) = calcFraction(
        rangeSliderState.valueRange.start,
        rangeSliderState.valueRange.endInclusive,
        rangeSliderState.activeRangeEnd
    )

    private fun DrawScope.drawTrack(
        tickFractions: FloatArray,
        activeRangeStart: Float,
        activeRangeEnd: Float,
        inactiveTrackColor: Color,
        activeTrackColor: Color,
        inactiveTickColor: Color,
        activeTickColor: Color
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val tickSize = 12f
        val trackStrokeWidth = 4f
        drawLine(
            color = inactiveTrackColor,
            start = sliderStart,
            end = sliderEnd,
            strokeWidth = trackStrokeWidth,
            cap = StrokeCap.Round
        )
        val sliderValueEnd = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * activeRangeEnd,
            center.y
        )

        val sliderValueStart = Offset(
            sliderStart.x +
                    (sliderEnd.x - sliderStart.x) * activeRangeStart,
            center.y
        )

        drawLine(
            activeTrackColor,
            sliderValueStart,
            sliderValueEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )

        for (tick in tickFractions) {
            val outsideFraction = tick > activeRangeEnd || tick < activeRangeStart
            drawCircle(
                color = if (outsideFraction) inactiveTickColor else activeTickColor,
                center = Offset(lerp(sliderStart, sliderEnd, tick).x, center.y),
                radius = tickSize / 2f
            )
        }
    }

    /**
     * Composable function that represents the indicator of the slider.
     *
     * @param indicatorValue The value to display as the indicator.
     * @param modifier The modifier for styling the indicator.
     * @param style The style of the indicator text.
     */
    @Composable
    fun Indicator(
        indicatorValue: String,
        modifier: Modifier = Modifier,
        style: TextStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal)
    ) {
        Box(modifier = modifier) {
            Text(
                text = indicatorValue,
                style = style,
                textAlign = TextAlign.Center
            )
        }
    }

    /**
     * Composable function that represents the label of the slider.
     *
     * @param labelValue The value to display as the label.
     * @param modifier The modifier for styling the label.
     * @param style The style of the label text.
     */
    @Composable
    fun Label(
        labelValue: String,
        modifier: Modifier = Modifier,
        style: TextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
    ) {
        Box(modifier = modifier) {
            Text(
                text = labelValue,
                style = style,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun Modifier.track(
    height: Dp = TrackHeight,
    shape: Shape = CircleShape
) = this
    .fillMaxWidth()
    .heightIn(min = height)
    .clip(shape)

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.progress(
    sliderState: SliderState,
    height: Dp = TrackHeight,
    shape: Shape = CircleShape
) = this
    // Compute the fraction based on the slider's current value.
    // We do this by dividing the current value by the total value.
    // However, the start value might not always be 0, so we need to
    // subtract the start value from both the current value and the total value.
    .fillMaxWidth(fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start))
    .heightIn(min = height)
    .clip(shape)

fun Modifier.thumb(
    size: Dp = ThumbSize,
    shape: Shape = CircleShape
) = this
    .defaultMinSize(minWidth = size, minHeight = size)
    .shadow(4.dp, shape = CircleShape, ambientColor = Color.Black, clip = false)
    .clip(shape)

private enum class CustomSliderComponents {
    SLIDER, LABEL, INDICATOR, THUMB
}

val PrimaryColor = Color(0xFF6650a4)
val TrackColor = Color(0xFFE7E0EC)

private const val Gap = 3
private val ValueRange = 0f..10f
private val TrackHeight = 8.dp
private val ThumbSize = 30.dp