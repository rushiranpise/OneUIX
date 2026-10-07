package io.github.soclear.oneuix

import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings tile for a soft reboot.
 *
 * A soft reboot restarts the framework instead of the kernel: init has no reboot subcommand for
 * that, so zygote is restarted, the same command the soft reboot power menu action runs. Root is
 * required, like the other tiles here.
 *
 * A single tap arms the tile and a second one within [ARM_TIMEOUT_MILLIS] reboots, so a tap made
 * while pulling the shade open cannot restart the framework by accident. A confirmation dialog is
 * not an option: the shade swallows an activity launched from a tile on this build.
 */
class SoftRebootTileService : TileService() {
    private var armed = false

    private val handler = Handler(Looper.getMainLooper())
    private val disarm = Runnable {
        armed = false
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        armed = false
        updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        qsTile?.label = getString(R.string.softRebootQsTile_label)
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()

        handler.removeCallbacks(disarm)
        armed = false
    }

    override fun onClick() {
        super.onClick()

        if (!armed) {
            armed = true
            handler.removeCallbacks(disarm)
            handler.postDelayed(disarm, ARM_TIMEOUT_MILLIS)
            updateTile()
            return
        }

        handler.removeCallbacks(disarm)
        armed = false
        softReboot()
    }

    private fun softReboot() {
        try {
            ProcessBuilder("su", "-c", "setprop ctl.restart zygote").start()
        } catch (_: Exception) {
            // Without root there is nothing else to try; the tile just stays inactive.
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        tile.state = if (armed) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (armed) getString(R.string.softRebootQsTile_confirm) else ""
        tile.updateTile()
    }

    companion object {
        /** How long an armed tile waits for the confirming tap. */
        private const val ARM_TIMEOUT_MILLIS = 5_000L
    }
}
