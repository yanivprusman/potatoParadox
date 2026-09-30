package com.automatelinux.potatoParadox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automatelinux.potatoParadox.ui.theme.Dry
import com.automatelinux.potatoParadox.ui.theme.DryDark
import com.automatelinux.potatoParadox.ui.theme.Ink
import com.automatelinux.potatoParadox.ui.theme.Muted
import com.automatelinux.potatoParadox.ui.theme.Potato
import com.automatelinux.potatoParadox.ui.theme.Track
import com.automatelinux.potatoParadox.ui.theme.Water
import kotlin.math.min
import kotlin.math.sqrt

// The launcher icon's potato, in its own 108-unit space (bbox x 25..80, y 43..81).
private const val POTATO_W = 55f
private const val POTATO_H = 38f
private const val POTATO_CX = 52.5f
private const val POTATO_CY = 62f

private fun potatoPath(cx: Float, cy: Float, width: Float): Path {
    val s = width / POTATO_W
    fun x(v: Float) = cx + (v - POTATO_CX) * s
    fun y(v: Float) = cy + (v - POTATO_CY) * s
    return Path().apply {
        moveTo(x(25f), y(65f))
        cubicTo(x(23f), y(53f), x(31f), y(45f), x(42f), y(46f))
        cubicTo(x(48f), y(46.5f), x(51f), y(43f), x(59f), y(43f))
        cubicTo(x(71f), y(43f), x(81f), y(51f), x(80f), y(62f))
        cubicTo(x(79f), y(72f), x(71f), y(78f), x(60f), y(78f))
        cubicTo(x(54f), y(78f), x(51f), y(81f), x(42f), y(81f))
        cubicTo(x(31f), y(81f), x(26f), y(74f), x(25f), y(65f))
        close()
    }
}

/**
 * The potato at 100 kg as a dashed ghost, and the dried potato inside it.
 * Area scales with mass, so half the mass looks like half the potato.
 */
@Composable
fun PotatoHero(massFraction: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(190.dp).testTag("potato-hero")) {
        val fullW = min(size.width * 0.82f, size.height / POTATO_H * POTATO_W * 0.95f)
        val cx = size.width / 2
        val cy = size.height / 2
        drawPath(
            potatoPath(cx, cy, fullW),
            color = Muted.copy(alpha = 0.55f),
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
        )
        val w = fullW * sqrt(massFraction.coerceIn(0.001f, 1f))
        // Sit the shrinking potato on the ghost's floor, like it is drying on a tray.
        val floor = cy + fullW / POTATO_W * (81f - POTATO_CY)
        val pcy = floor - w / POTATO_W * (81f - POTATO_CY)
        drawPotato(cx, pcy, w)
    }
}

private fun DrawScope.drawPotato(cx: Float, cy: Float, w: Float) {
    val s = w / POTATO_W
    drawPath(
        potatoPath(cx, cy, w),
        brush = Brush.verticalGradient(listOf(Potato, Dry), startY = cy - 20 * s, endY = cy + 20 * s),
    )
    // Highlight and eyes, as on the icon.
    fun p(x: Float, y: Float) = Offset(cx + (x - POTATO_CX) * s, cy + (y - POTATO_CY) * s)
    val hl = Path().apply {
        moveTo(p(32f, 58f).x, p(32f, 58f).y)
        cubicTo(p(33f, 52f).x, p(33f, 52f).y, p(38f, 50f).x, p(38f, 50f).y, p(44f, 51f).x, p(44f, 51f).y)
    }
    drawPath(hl, Color.White.copy(alpha = 0.45f), style = Stroke(width = 3 * s, cap = StrokeCap.Round))
    drawCircle(DryDark, 2.2f * s, p(37f, 64f))
    drawCircle(DryDark, 2.2f * s, p(54f, 71f))
    drawCircle(DryDark, 2f * s, p(68f, 61f))
}

/**
 * One way of describing the potatoes, before and after, on its own 0..max track.
 * The ghost bar is "before", the solid bar is "after" — the gap between them is the change.
 */
@Composable
fun CompareTrack(
    title: String,
    before: String,
    after: String,
    beforeFraction: Float,
    afterFraction: Float,
    drop: String,
    color: Color,
    tag: String,
) {
    Column(Modifier.fillMaxWidth().testTag(tag)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Ink)
            Text("ירד ב־$drop", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Box(
            Modifier.padding(top = 6.dp).fillMaxWidth().height(14.dp)
                .background(Track, RoundedCornerShape(7.dp)),
        ) {
            Box(
                Modifier.fillMaxWidth(beforeFraction.coerceIn(0f, 1f)).height(14.dp)
                    .background(color.copy(alpha = 0.25f), RoundedCornerShape(7.dp)),
            )
            Box(
                Modifier.fillMaxWidth(afterFraction.coerceIn(0.004f, 1f)).height(14.dp)
                    .background(color, RoundedCornerShape(7.dp)),
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("לפני $before", style = MaterialTheme.typography.bodySmall, color = Muted)
            Text("אחרי $after", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

/**
 * Cost of lowering the water share by one point, as a function of where you are.
 * Flat for most of the range, then a wall as the share approaches 100%.
 */
@Composable
fun PointCostCurve(markerPct: Double) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 11.sp, color = Muted)
    val xMin = 50.0
    val xMax = 99.0
    val yMax = 50.0
    // A chart's x axis is a number line: keep it left-to-right even in Hebrew.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(Modifier.fillMaxWidth().height(190.dp).testTag("point-cost-curve")) {
            val left = 30.dp.toPx()
            val right = 10.dp.toPx()
            val bottom = size.height - 20.dp.toPx()
            val top = 22.dp.toPx()
            val w = size.width - left - right
            val h = bottom - top
            fun x(p: Double) = left + ((p - xMin) / (xMax - xMin) * w).toFloat()
            fun y(c: Double) = bottom - (min(c, yMax) / yMax * h).toFloat()

            for (tick in listOf(0.0, 25.0, 50.0)) {
                drawLine(Track, Offset(left, y(tick)), Offset(left + w, y(tick)), 1.dp.toPx())
                val t = measurer.measure(tick.toInt().toString(), labelStyle)
                drawText(t, topLeft = Offset(left - t.size.width - 6.dp.toPx(), y(tick) - t.size.height / 2))
            }
            for (tick in listOf(50.0, 70.0, 90.0, 99.0)) {
                val t = measurer.measure("${tick.toInt()}%", labelStyle)
                drawText(t, topLeft = Offset(x(tick) - t.size.width / 2, bottom + 4.dp.toPx()))
            }

            val line = Path()
            val area = Path()
            var p = xMin
            line.moveTo(x(p), y(onePointCost(p)))
            area.moveTo(x(p), bottom)
            area.lineTo(x(p), y(onePointCost(p)))
            while (p < xMax) {
                p = min(xMax, p + 0.1)
                line.lineTo(x(p), y(onePointCost(p)))
                area.lineTo(x(p), y(onePointCost(p)))
            }
            area.lineTo(x(xMax), bottom)
            area.close()
            drawPath(area, Brush.verticalGradient(listOf(Water.copy(alpha = 0.28f), Water.copy(alpha = 0.02f)), top, bottom))
            drawPath(line, Water, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

            val mp = markerPct.coerceIn(xMin, xMax)
            val mc = onePointCost(mp)
            drawLine(Dry, Offset(x(mp), bottom), Offset(x(mp), y(mc)), 2.dp.toPx())
            drawCircle(Color.White, 8.dp.toPx(), Offset(x(mp), y(mc)))
            drawCircle(Dry, 6.dp.toPx(), Offset(x(mp), y(mc)))
            val t = measurer.measure(
                "${num(mc, if (mc < 1) 2 else 0)} ק״ג",
                labelStyle.copy(fontWeight = FontWeight.Bold, color = DryDark, fontSize = 12.sp),
            )
            val tx = (x(mp) - t.size.width - 10.dp.toPx()).coerceAtLeast(left + 4.dp.toPx())
            val ty = (y(mc) - t.size.height - 6.dp.toPx()).coerceAtLeast(0f)
            drawText(t, topLeft = Offset(tx, ty))
        }
    }
}
