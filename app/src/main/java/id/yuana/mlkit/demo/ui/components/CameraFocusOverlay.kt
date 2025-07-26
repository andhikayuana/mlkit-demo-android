package id.yuana.mlkit.demo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
fun CameraFocusOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val rectWidth = size.width * 0.9f
        val rectHeight = size.height * 0.4f
        val left = (size.width - rectWidth) / 2
        val top = (size.height - rectHeight) / 2

        // Draw dark overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        // Cut out the focus area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Draw white border for the focus area
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}