package io.github.soclear.oneuix

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile for Samsung's Outdoor mode.
 *
 * Toggles the same `display_outdoor_mode` system setting the brightness row in the shade uses, so the
 * framework applies the brightness change on its own.
 *
 * The key can be written two ways, tried in order: directly when the app holds "Modify system
 * settings", otherwise through root. Root is what most users of this module already have, so the
 * permission screen only appears when neither is available. The module also lifts the provider's
 * restriction on this one key in system_server, which the direct write needs.
 */
class OutdoorModeTileService : TileService() {
    override fun onClick() {
        val target = if (isOutdoorModeEnabled()) 0 else 1
        if (writeOutdoorMode(target)) {
            updateTileState()
            return
        }

        // Neither the app op nor root: ask for "Modify system settings".
        openWriteSettingsScreen()
    }

    override fun onStartListening() {
        updateTileState()
    }

    private fun updateTileState() {
        val state = try {
            if (isOutdoorModeEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        } catch (_: Exception) {
            Tile.STATE_UNAVAILABLE
        }
        setTileState(state)
    }

    private fun isOutdoorModeEnabled(): Boolean {
        return Settings.System.getInt(contentResolver, OUTDOOR_MODE, 0) != 0
    }

    private fun writeOutdoorMode(value: Int): Boolean {
        if (Settings.System.canWrite(this)) {
            try {
                Settings.System.putInt(contentResolver, OUTDOOR_MODE, value)
                if (currentValue() == value) return true
            } catch (_: Exception) {
                // Fall through to root.
            }
        }
        return writeOutdoorModeAsRoot(value)
    }

    private fun writeOutdoorModeAsRoot(value: Int): Boolean = try {
        ProcessBuilder("su", "-c", "settings put system $OUTDOOR_MODE $value")
            .start()
            .waitFor() == 0 && currentValue() == value
    } catch (_: Exception) {
        false
    }

    private fun currentValue(): Int = Settings.System.getInt(contentResolver, OUTDOOR_MODE, -1)

    private fun openWriteSettingsScreen() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            .setData(Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun setTileState(state: Int) {
        qsTile?.state = state
        qsTile?.updateTile()
    }

    companion object {
        private const val OUTDOOR_MODE = "display_outdoor_mode"
    }
}
