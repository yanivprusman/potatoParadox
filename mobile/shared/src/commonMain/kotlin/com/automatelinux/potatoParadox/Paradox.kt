package com.automatelinux.potatoParadox

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

const val START_MASS_KG = 100.0
// Latent heat of vaporisation of water, 2.26 MJ/kg, in kWh per kg.
private const val KWH_PER_KG_EVAPORATED = 2.26 / 3.6
// Bringing one litre of tap water to the boil in a kettle (~4.2 kJ/kg·K × 80 K).
const val KWH_PER_KETTLE = 0.093

/** Everything follows from one fact: drying removes water, never dry matter. */
data class Paradox(val startPct: Double, val endPct: Double) {
    val dryKg = START_MASS_KG * (1 - startPct / 100)
    val endMassKg = dryKg / (1 - endPct / 100)
    val removedKg = START_MASS_KG - endMassKg
    val kwh = removedKg * KWH_PER_KG_EVAPORATED
    val ratioBefore = waterToDry(startPct)
    val ratioAfter = waterToDry(endPct)

    /** Relative drop, 0..1, of each way of describing the same potatoes. */
    val pctDrop = 1 - endPct / startPct
    val ratioDrop = 1 - ratioAfter / ratioBefore
    val massDrop = removedKg / START_MASS_KG
}

fun waterToDry(pct: Double) = pct / (100 - pct)

/** kg of water to remove, per kg of dry matter, to lower the water share by one point from [pct]. */
fun onePointCost(pct: Double): Double = waterToDry(pct) - waterToDry(pct - 1)

fun roundTo(x: Double, decimals: Int): Double {
    val m = 10.0.pow(decimals)
    return round(x * m) / m
}

/** Fixed-decimal formatting; commonMain has no String.format. */
fun num(x: Double, decimals: Int): String {
    val m = 10.0.pow(decimals).toLong()
    val n = round(abs(x) * m).toLong()
    val sign = if (x < 0 && n != 0L) "-" else ""
    val whole = n / m
    if (decimals == 0) return "$sign$whole"
    return "$sign$whole." + (n % m).toString().padStart(decimals, '0')
}

/** Drops a trailing ".0" so 99.0 reads as 99 but 99.9 stays. */
fun trim1(x: Double) = num(x, 1).removeSuffix(".0")

fun pct(x: Double) = trim1(x) + "%"

/** Mass reads best with one decimal only while it is small. */
fun kg(x: Double) = (if (x >= 10) num(x, 0) else trim1(x)) + " ק״ג"

fun ratio(r: Double) = (if (r >= 10) num(r, 0) else trim1(r)) + ":1"

/** A 0..1 share as a whole percentage, e.g. "50%". Below 1% keep one decimal. */
fun share(x: Double) = if (x * 100 < 1) pct(x * 100) else num(x * 100, 0) + "%"
