package io.github.soclear.oneuix.common

/**
 * How a network speed is written out.
 *
 * The preference stores the index into these options, so the order is part of what a saved
 * preference means and matches the order the settings list shows them in.
 */
object NetworkSpeedUnit {
    /**
     * One way of writing a speed.
     *
     * [bits] multiplies the reading, which is in bytes per second, by eight, the way link speeds are
     * usually quoted. [base] is 1000 to scale like a link speed or a drive, and 1024 to scale in
     * powers of two.
     */
    data class Unit(
        val bits: Boolean,
        val base: Int,
        val byteSuffix: String,
        val kiloPrefix: String,
        val megaPrefix: String,
    )

    val UNITS: List<Unit> = listOf(
        Unit(bits = false, base = 1024, byteSuffix = "B", kiloPrefix = "K", megaPrefix = "M"),
        Unit(bits = false, base = 1000, byteSuffix = "B", kiloPrefix = "K", megaPrefix = "M"),
        Unit(bits = true, base = 1000, byteSuffix = "b", kiloPrefix = "Kb", megaPrefix = "Mb"),
        Unit(bits = true, base = 1024, byteSuffix = "b", kiloPrefix = "Kib", megaPrefix = "Mib"),
    )

    /** Keeps a stored index inside the list, so an old or hand-edited value cannot break the UI. */
    fun coerce(value: Int): Int = value.coerceIn(0, UNITS.lastIndex)

    /** The unit for [value]. */
    fun of(value: Int): Unit = UNITS[coerce(value)]
}
