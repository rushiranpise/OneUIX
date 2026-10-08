package io.github.soclear.oneuix.common

/**
 * Markers drawn in front of the upload and download speeds.
 *
 * The preference stores the index into these options, so the order is part of what a saved
 * preference means and matches the order the settings list shows them in. Sizes and spacing live
 * in their own preferences.
 */
object NetworkSpeedMarker {
    /** Upload markers, index for index with [DOWN]. */
    val UP: List<String> = listOf(
        "\u2191", // thin arrow
        "\u25B2", // solid triangle
        "\u25B3", // outline triangle
        "\u21E7", // block arrow
        "\u21E1", // dashed arrow
        "\u2303", // arrowhead
        "\u2195", // one marker for both directions
        "^",      // ascii
        "\u2B06", // emoji
    )

    /** Download markers, index for index with [UP]. An empty entry reuses the upload marker. */
    val DOWN: List<String> = listOf(
        "\u2193",
        "\u25BC",
        "\u25BD",
        "\u21E9",
        "\u21E3",
        "\u2304",
        "",
        "v",
        "\u2B07",
    )

    /** Keeps a stored index inside the list, so an old or hand-edited value cannot break the UI. */
    fun coerce(value: Int): Int = value.coerceIn(0, UP.lastIndex)

    /** Upload marker for [value]. */
    fun up(value: Int): String = UP[coerce(value)]

    /** Download marker for [value], falling back to the upload marker when it is empty. */
    fun down(value: Int): String = DOWN[coerce(value)].ifEmpty { up(value) }
}
