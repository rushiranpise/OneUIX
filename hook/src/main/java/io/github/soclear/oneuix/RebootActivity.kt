package io.github.soclear.oneuix

import android.app.Activity
import android.os.Bundle
import io.github.soclear.oneuix.common.PowerMenuAction

class RebootActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.action?.let { action ->
            // A soft reboot restarts the framework rather than the kernel; init has no reboot
            // subcommand for that, so restart zygote instead. Everything else is "reboot <action>".
            val command = if (action == PowerMenuAction.SOFT_REBOOT) {
                "setprop ctl.restart zygote"
            } else {
                "reboot $action"
            }
            ProcessBuilder("su", "-c", command).start()
        }
        finish()
    }
}
