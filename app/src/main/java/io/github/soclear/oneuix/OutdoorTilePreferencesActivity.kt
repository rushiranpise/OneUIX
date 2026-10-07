package io.github.soclear.oneuix

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

/**
 * Launched when the Outdoor mode tile is long pressed.
 *
 * A tile has no long-press callback: the system looks for an activity of the tile's package that
 * handles ACTION_QS_TILE_PREFERENCES, and falls back to the app info screen when there is none.
 * This activity claims that action and forwards to Settings -> Display, where the full Outdoor mode
 * option lives.
 *
 * It is a trampoline only: no display theme, finishes immediately.
 */
class OutdoorTilePreferencesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            startActivity(
                Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            // Nothing else to try; finishing leaves the user on the previous screen.
        }

        finish()
    }
}
