package com.automatelinux.potatoParadox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automatelinux.potatoParadox.ui.theme.AppTheme
import com.automatelinux.potatoParadox.ui.theme.Dry
import com.automatelinux.potatoParadox.ui.theme.Water
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

private const val START_MASS_KG = 100.0
// Latent heat of vaporisation of water, 2.26 MJ/kg, in kWh per kg.
private const val KWH_PER_KG_EVAPORATED = 2.26 / 3.6
// Bringing one litre of tap water to the boil in a kettle (~4.2 kJ/kg·K × 80 K).
private const val KWH_PER_KETTLE = 0.093

private data class Preset(val start: Double, val end: Double)

private val PRESETS = listOf(
    Preset(99.0, 98.0),
    Preset(50.0, 49.0),
    Preset(90.0, 80.0),
    Preset(99.9, 99.0),
)

@Composable
fun App() {
    AppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                ParadoxScreen()
            }
        }
    }
}

@Composable
private fun ParadoxScreen() {
    var startPct by remember { mutableDoubleStateOf(99.0) }
    var endPct by remember { mutableDoubleStateOf(98.0) }

    // The dry matter never evaporates — it is the one fixed quantity.
    val dryKg = START_MASS_KG * (1 - startPct / 100)
    val endMassKg = dryKg / (1 - endPct / 100)
    val removedKg = START_MASS_KG - endMassKg
    val kwh = removedKg * KWH_PER_KG_EVAPORATED
    val ratioBefore = startPct / (100 - startPct)
    val ratioAfter = endPct / (100 - endPct)

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("פרדוקס תפוחי האדמה", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "100 ק״ג תפוחי אדמה, ומייבשים אותם בשמש. האחוז זז בקושי — המשקל לא.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PRESETS.forEachIndexed { i, p ->
                FilterChip(
                    selected = p.start == startPct && p.end == endPct,
                    onClick = { startPct = p.start; endPct = p.end },
                    label = { Text("מ-${pct(p.start)} ל-${pct(p.end)}") },
                    modifier = Modifier.testTag("preset-$i"),
                )
            }
        }

        Section("מה שהאחוז מראה") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PercentGrid("לפני", startPct)
                PercentGrid("אחרי", endPct)
            }
            Text(
                "שתי התמונות כמעט זהות: ${cells(startPct)} מול ${cells(endPct)} ריבועים כחולים מתוך 100.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Section("מה שהמשקל עושה") {
            MassBars(dryKg = dryKg, endMassKg = endMassKg)
        }

        Section("כמה מים במים") {
            LabeledSlider(
                label = "אחוז מים בהתחלה",
                value = startPct,
                range = 50.0..99.9,
                tag = "slider-start",
                onChange = { v ->
                    startPct = v
                    if (endPct >= v) endPct = roundTo(v - 0.1, 1)
                },
            )
            LabeledSlider(
                label = "אחוז מים אחרי הייבוש",
                value = endPct,
                range = 0.0..(startPct - 0.1),
                tag = "slider-end",
                onChange = { v -> endPct = v },
            )
        }

        Section("המחיר") {
            Stat("משקל אחרי הייבוש", "${num(endMassKg, 1)} ק״ג", big = true)
            Stat("מים שצריך לאדות", "${num(removedKg, 1)} ק״ג")
            Stat("אנרגיה לאידוי", "${num(kwh, 1)} קוט״ש")
            Stat("כמו להרתיח קומקום", "${num(kwh / KWH_PER_KETTLE, 0)} פעמים")
            Stat("יחס מים ליבש", "מ-${ratio(ratioBefore)}:1 ל-${ratio(ratioAfter)}:1")
        }

        Section("מחיר של נקודת אחוז אחת") {
            Text(
                "כמה ק״ג מים צריך להוציא כדי להוריד את האחוז בנקודה אחת, לכל ק״ג של חומר יבש:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PointCostCurve(markerPct = startPct)
        }

        Section("העיקרון") {
            Text(
                "האחוז הוא סרגל מעוות. ליד 100% נקודת אחוז אחת מסתירה שינוי ענק ביחס: " +
                    "מ-${ratio(ratioBefore)}:1 ל-${ratio(ratioAfter)}:1. " +
                    "כמות המים שצריך להוציא — ולכן גם האנרגיה — תלויה בשינוי ביחס, לא בשינוי באחוז. " +
                    "לכן 99% ל-98% עולה חצי מהמשקל, ו-50% ל-49% עולה רק 2%.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun PercentGrid(label: String, pctWater: Double) {
    val blue = cells(pctWater)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(120.dp)) {
            val gap = 2.dp.toPx()
            val cell = (size.width - gap * 9) / 10
            for (i in 0 until 100) {
                val row = i / 10
                val col = i % 10
                drawRoundRect(
                    color = if (i < blue) Water else Dry,
                    topLeft = Offset(col * (cell + gap), row * (cell + gap)),
                    size = Size(cell, cell),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("$label: ${pct(pctWater)}", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun MassBars(dryKg: Double, endMassKg: Double) {
    val barMaxHeight = 200.dp
    Row(
        Modifier.fillMaxWidth().height(barMaxHeight + 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        MassBar("לפני", START_MASS_KG, dryKg, barMaxHeight)
        MassBar("אחרי", endMassKg, dryKg, barMaxHeight)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Legend(Water, "מים")
        Legend(Dry, "חומר יבש: ${num(dryKg, 2)} ק״ג, לא משתנה")
    }
}

@Composable
private fun MassBar(label: String, totalKg: Double, dryKg: Double, maxHeight: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${num(totalKg, 1)} ק״ג", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        val h = maxHeight * (totalKg / START_MASS_KG).toFloat()
        // The dry share is often under 1% of the bar; keep it visible.
        val dryH = max(3f, (h.value * (dryKg / totalKg)).toFloat()).dp
        Column(
            Modifier
                .width(84.dp)
                .height(max(h.value, dryH.value).dp),
        ) {
            Box(
                Modifier.fillMaxWidth().weight(1f)
                    .background(Water, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
            )
            Box(Modifier.fillMaxWidth().height(dryH).background(Dry))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    tag: String,
    onChange: (Double) -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(pct(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        // Sliders read left-to-right like a number line, even in Hebrew.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(roundTo(it.toDouble(), 1).coerceIn(range)) },
                valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
                colors = SliderDefaults.colors(thumbColor = Water, activeTrackColor = Water),
                modifier = Modifier.testTag(tag),
            )
        }
    }
}

@Composable
private fun Stat(label: String, value: String, big: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * kg of water to remove to go from p% to (p-1)% water, per kg of dry matter:
 * total mass is dry/(1-p), so the cost is 1/(1-p) - 1/(1-(p-1%)).
 * Flat for most of the range, then a wall as p approaches 100%.
 */
@Composable
private fun PointCostCurve(markerPct: Double) {
    val measurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = TextStyle(fontSize = 11.sp, color = axisColor)
    val xMin = 50.0
    val xMax = 99.0
    val yMax = 50.0
    fun cost(p: Double): Double {
        val a = p / 100
        return 1 / (1 - a) - 1 / (1 - (a - 0.01))
    }
    // Chart axes are a number line: keep them left-to-right.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(Modifier.fillMaxWidth().height(200.dp).testTag("point-cost-curve")) {
            val left = 36.dp.toPx()
            val bottom = size.height - 22.dp.toPx()
            val top = 8.dp.toPx()
            val w = size.width - left - 8.dp.toPx()
            val h = bottom - top
            fun x(p: Double) = left + ((p - xMin) / (xMax - xMin) * w).toFloat()
            fun y(c: Double) = bottom - (min(c, yMax) / yMax * h).toFloat()

            drawLine(axisColor, Offset(left, bottom), Offset(left + w, bottom), 1.dp.toPx())
            drawLine(axisColor, Offset(left, top), Offset(left, bottom), 1.dp.toPx())
            for (tick in listOf(0.0, 25.0, 50.0)) {
                val t = measurer.measure("${tick.toInt()}", labelStyle)
                drawText(t, topLeft = Offset(left - t.size.width - 6.dp.toPx(), y(tick) - t.size.height / 2))
            }
            for (tick in listOf(50.0, 70.0, 90.0, 99.0)) {
                val t = measurer.measure("${tick.toInt()}%", labelStyle)
                drawText(t, topLeft = Offset(x(tick) - t.size.width / 2, bottom + 4.dp.toPx()))
            }

            val path = Path()
            var p = xMin
            path.moveTo(x(p), y(cost(p)))
            while (p < xMax) {
                p = min(xMax, p + 0.1)
                path.lineTo(x(p), y(cost(p)))
            }
            drawPath(path, Water, style = Stroke(width = 3.dp.toPx()))

            if (markerPct in xMin..xMax) {
                val mc = cost(markerPct)
                drawLine(Dry, Offset(x(markerPct), bottom), Offset(x(markerPct), y(mc)), 2.dp.toPx())
                drawCircle(Dry, 6.dp.toPx(), Offset(x(markerPct), y(mc)))
                val t = measurer.measure("${num(mc, 2)} ק״ג", labelStyle.copy(fontWeight = FontWeight.Bold, color = Dry))
                val tx = (x(markerPct) - t.size.width - 8.dp.toPx()).coerceAtLeast(left + 4.dp.toPx())
                drawText(t, topLeft = Offset(tx, (y(mc) - t.size.height - 4.dp.toPx()).coerceAtLeast(0f)))
            }
        }
    }
}

private fun cells(pctWater: Double) = round(pctWater).toInt().coerceIn(0, 100)

private fun roundTo(x: Double, decimals: Int): Double {
    val m = 10.0.pow(decimals)
    return round(x * m) / m
}

/** Fixed-decimal formatting; commonMain has no String.format. */
private fun num(x: Double, decimals: Int): String {
    val m = 10.0.pow(decimals).toLong()
    val n = round(abs(x) * m).toLong()
    val sign = if (x < 0 && n != 0L) "-" else ""
    val whole = n / m
    if (decimals == 0) return "$sign$whole"
    return "$sign$whole." + (n % m).toString().padStart(decimals, '0')
}

/** A percentage with one decimal, dropping a trailing ".0". */
private fun pct(x: Double): String = num(x, 1).removeSuffix(".0") + "%"

private fun ratio(r: Double): String = if (r >= 10) num(r, 0) else num(r, 1).removeSuffix(".0")
