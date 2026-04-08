package io.github.chayanforyou.cartoonphoto.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chayanforyou.cartoonphoto.R
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    before: Painter,
    after: Painter,
    modifier: Modifier = Modifier,
    initialPosition: Float = 0.5f
) {
    var sliderPosition by remember { mutableFloatStateOf(initialPosition) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val minScale = remember(before, containerSize) {
        val imgW = before.intrinsicSize.width
        val imgH = before.intrinsicSize.height

        val containerW = containerSize.width.toFloat()
        val containerH = containerSize.height.toFloat()

        calculateMinScale(imgW, imgH, containerW, containerH)
    }

    LaunchedEffect(minScale) {
        scale = minScale
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)

                    val maxX = (containerSize.width * (scale - 1)) / 2
                    val maxY = (containerSize.height * (scale - 1)) / 2

                    offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                    offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                }
            }
    ) {
        val widthPx = containerSize.width.toFloat()
        val sliderX = widthPx * sliderPosition

        // Before image
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = before,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            ImageLabel(
                text = "Before",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                background = Color.Black.copy(alpha = 0.5f)
            )
        }

        // After image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(left = sliderX) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            Image(
                painter = after,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            )

            ImageLabel(
                text = "After",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                background = Color.Black.copy(alpha = 0.5f)
            )
        }

        // Divider line
        Box(
            modifier = Modifier
                .offset { IntOffset(sliderX.roundToInt(), 0) }
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.White)
        )

        // Handle
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (sliderX - 16.dp.toPx()).roundToInt(),
                        (containerSize.height / 2) - 16.dp.toPx().roundToInt()
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val width = containerSize.width.toFloat().coerceAtLeast(1f)
                        sliderPosition = (sliderPosition + (dragAmount.x / width)).coerceIn(0f, 1f)
                    }
                }
                .size(32.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_compare),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

private fun calculateMinScale(
    imgWidth: Float,
    imgHeight: Float,
    containerWidth: Float,
    containerHeight: Float
): Float {
    if (imgWidth <= 0f || imgHeight <= 0f || containerWidth <= 0f || containerHeight <= 0f) {
        return 1f
    }

    val imgAspect = imgWidth / imgHeight
    val containerAspect = containerWidth / containerHeight

    val renderedW: Float
    val renderedH: Float

    if (imgAspect > containerAspect) {
        renderedW = containerWidth
        renderedH = containerWidth / imgAspect
    } else {
        renderedH = containerHeight
        renderedW = containerHeight * imgAspect
    }

    val scaleX = containerWidth / renderedW
    val scaleY = containerHeight / renderedH

    return maxOf(scaleX, scaleY).coerceAtLeast(1f)
}

@Composable
private fun ImageLabel(
    text: String,
    modifier: Modifier,
    background: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
        )
    }
}
