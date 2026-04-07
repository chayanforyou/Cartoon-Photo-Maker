package io.github.chayanforyou.cartoonphoto.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val width = size.width.toFloat()
                    sliderPosition =
                        (change.position.x / width).coerceIn(0f, 1f)
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val sliderX = widthPx * sliderPosition

        // Before image
        Image(
            painter = before,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.Center
        )

        // After image (clipped)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    clipRect(right = sliderX) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            Image(
                painter = after,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.Center
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
                        (sliderX - 32.dp.toPx() / 2).roundToInt(),
                        (constraints.maxHeight / 2)
                    )
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