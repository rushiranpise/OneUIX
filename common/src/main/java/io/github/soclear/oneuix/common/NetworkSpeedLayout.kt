package io.github.soclear.oneuix.common

/**
 * Layouts of the module's network speed indicator.
 *
 * The preference stores the index into these options, so their order is part of what a saved
 * preference means and matches the order the settings list shows them in.
 */
object NetworkSpeedLayout {
    /** SystemUI builds and draws the reading itself, and the module only adds the markers to it. */
    const val SYSTEM_DEFAULT = 0

    /** Both speeds side by side on one line. */
    const val SPLIT_HORIZONTAL = 1

    /** Upload on the first line and download on the second. */
    const val SPLIT_VERTICAL = 2

    /** Only the direction that is moving, one marker and one number. */
    const val ACTIVE_DIRECTION = 3

    /** Keeps a stored index inside the list, so an old or hand-edited value cannot break the UI. */
    fun coerce(value: Int): Int = value.coerceIn(SYSTEM_DEFAULT, ACTIVE_DIRECTION)
}
