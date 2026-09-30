package com.automatelinux.potatoParadox

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.automatelinux.potatoParadox.ui.theme.Alarm
import com.automatelinux.potatoParadox.ui.theme.AppTheme
import com.automatelinux.potatoParadox.ui.theme.Card
import com.automatelinux.potatoParadox.ui.theme.Dry
import com.automatelinux.potatoParadox.ui.theme.DryDark
import com.automatelinux.potatoParadox.ui.theme.Ink
import com.automatelinux.potatoParadox.ui.theme.Muted
import com.automatelinux.potatoParadox.ui.theme.Track
import com.automatelinux.potatoParadox.ui.theme.Water
import com.automatelinux.potatoParadox.ui.theme.WaterDeep

/** Starting water shares offered as chips; each one opens on a one-point drop so they compare. */
private val STARTS = listOf(99.9, 99.0, 90.0, 50.0)

/** kg of water to evaporate to take [startPct] down to [endPct]. */
private fun removedFor(startPct: Double, endPct: Double) =
    START_MASS_KG - START_MASS_KG * (1 - startPct / 100) / (1 - endPct / 100)

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
    // The slider is in kilograms of water evaporated, not in percent: kilograms are what
    // drying actually costs, and a percent slider would cram the whole story into its last millimetre.
    var removedKg by remember { mutableDoubleStateOf(removedFor(99.0, 98.0)) }
    val waterKg = START_MASS_KG * startPct / 100

    val animRemoved by animateFloatAsState(
        removedKg.toFloat(),
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
    )
    val removed = animRemoved.toDouble().coerceIn(0.0, waterKg)
    val endPct = (waterKg - removed) / (START_MASS_KG - removed) * 100
    val px = Paradox(startPct, endPct)

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
            Text("פרדוקס", style = MaterialTheme.typography.labelLarge, color = Dry, fontWeight = FontWeight.Bold)
            Text("תפוחי האדמה", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text(
                "100 ק״ג תפוחי אדמה שרובם מים. מייבשים אותם בשמש — כמה הם שוקלים עכשיו?",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Panel {
            PotatoHero(massFraction = (px.endMassKg / START_MASS_KG).toFloat())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStat("אחוז מים", pct(endPct), "לפני ${pct(startPct)}", Water)
                BigStat("משקל", kg(px.endMassKg), "לפני 100 ק״ג", Alarm)
            }
            Column {
                Text(
                    "גררו כדי לאדות מים: ${kg(removedKg)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                // Left is dry, right is wet, whatever the reading direction.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Slider(
                        value = (waterKg - removedKg).toFloat(),
                        onValueChange = { removedKg = roundTo(waterKg - it, 1).coerceIn(0.0, waterKg) },
                        valueRange = 0f..waterKg.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Water,
                            activeTrackColor = Water,
                            inactiveTrackColor = Track,
                        ),
                        modifier = Modifier.testTag("slider-evaporate"),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("רטוב", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text("יבש לגמרי", style = MaterialTheme.typography.labelMedium, color = Muted)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "מתחילים מ־ (ומורידים נקודת אחוז אחת):",
                style = MaterialTheme.typography.titleSmall,
                color = Muted,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                STARTS.forEach { s ->
                    FilterChip(
                        selected = s == startPct,
                        onClick = {
                            startPct = s
                            removedKg = roundTo(removedFor(s, s - 1), 1)
                        },
                        label = { Text("${pct(s)} מים", fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Card,
                            selectedContainerColor = Water,
                            selectedLabelColor = Color.White,
                        ),
                        modifier = Modifier.testTag("start-${trim1(s)}"),
                    )
                }
            }
        }

        Panel("שלוש דרכים לתאר את אותו ייבוש") {
            CompareTrack(
                title = "אחוז המים",
                before = pct(startPct),
                after = pct(endPct),
                beforeFraction = (startPct / 100).toFloat(),
                afterFraction = (endPct / 100).toFloat(),
                drop = share(px.pctDrop),
                color = Water,
                tag = "track-percent",
            )
            CompareTrack(
                title = "יחס מים לחומר יבש",
                before = ratio(px.ratioBefore),
                after = ratio(px.ratioAfter),
                beforeFraction = 1f,
                afterFraction = (px.ratioAfter / px.ratioBefore).toFloat(),
                drop = share(px.ratioDrop),
                color = WaterDeep,
                tag = "track-ratio",
            )
            CompareTrack(
                title = "משקל",
                before = "100 ק״ג",
                after = kg(px.endMassKg),
                beforeFraction = 1f,
                afterFraction = (px.endMassKg / START_MASS_KG).toFloat(),
                drop = share(px.massDrop),
                color = Alarm,
                tag = "track-mass",
            )
            Text(
                "האחוז הוא הסרגל שמשקר. היחס והמשקל מראים מה באמת קרה.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        }

        Panel("מה זה עלה") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CostTile(Icons.Rounded.WaterDrop, num(removedKg, if (removedKg < 10) 1 else 0), "ק״ג מים אודו", Water)
                CostTile(Icons.Rounded.Bolt, num(px.kwh, if (px.kwh < 10) 1 else 0), "קוט״ש אנרגיה", Dry)
                CostTile(Icons.Rounded.LocalCafe, num(px.kwh / KWH_PER_KETTLE, 0), "קומקומים רותחים", DryDark)
            }
        }

        Panel("כמה עולה נקודת אחוז אחת") {
            Text(
                "ק״ג מים שצריך לאדות כדי להוריד את האחוז בנקודה אחת, לכל ק״ג חומר יבש. הסימון הוא נקודת ההתחלה שבחרתם.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            PointCostCurve(markerPct = startPct)
        }

        Column(
            Modifier.fillMaxWidth()
                .background(WaterDeep, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("אז מה העיקרון?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "מה שעולה אנרגיה הוא המים שמוציאים — וזה בדיוק השינוי ביחס מים לחומר יבש. " +
                    "האחוז דוחס את היחס: ליד 100% יחס ענק נכנס לתוך נקודות בודדות, " +
                    "ולכן צעד קטן באחוז הוא צעד ענק ביחס.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Text(
                "מ־99% ל־98% מים: חצי מהמשקל. מ־50% ל־49%: פחות מ־2%.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Panel(title: String? = null, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(Card, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink)
        }
        content()
    }
}

@Composable
private fun RowScope.BigStat(label: String, value: String, before: String, color: Color) {
    Column(
        Modifier.weight(1f)
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black), color = color, maxLines = 1)
        Text(before, style = MaterialTheme.typography.bodyMedium, color = Muted)
    }
}

@Composable
private fun RowScope.CostTile(icon: ImageVector, value: String, label: String, color: Color) {
    Column(
        Modifier.weight(1f)
            .background(color.copy(alpha = 0.09f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Ink, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Muted, maxLines = 1)
    }
}
