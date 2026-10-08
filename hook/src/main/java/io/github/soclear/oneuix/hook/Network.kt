package io.github.soclear.oneuix.hook

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ScaleXSpan
import android.util.TypedValue
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.TextView
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.soclear.oneuix.common.NetworkSpeedMarker
import io.github.soclear.oneuix.common.NetworkSpeedUnit
import io.github.soclear.oneuix.common.Package
import io.github.soclear.oneuix.hook.util.afterAttach
import io.github.soclear.oneuix.hook.util.reflect
import io.github.soclear.oneuix.hook.util.xlog
import java.net.NetworkInterface
import kotlin.math.roundToInt

object Network {
    /** The scale SystemUI itself writes its readings in, assumed for anything it draws. */
    private const val SYSTEM_BASE = 1024

    /** The first number of a reading, with its optional decimal part. */
    private val SPEED_NUMBER = Regex("[0-9]+(?:[.,][0-9]+)?")

    context(xposedModule: XposedModule, param: XposedModuleInterface.PackageReadyParam)
    fun supportRealTimeNetworkSpeed() {
        if (param.packageName != Package.SETTINGS &&
            param.packageName != Package.SYSTEMUI
        ) {
            return
        }
        try {
            val semCscFeatureClass =
                param.classLoader.loadClass("com.samsung.android.feature.SemCscFeature")
            val method = semCscFeatureClass.getDeclaredMethod(
                "getBoolean",
                String::class.java,
                Boolean::class.javaPrimitiveType
            )
            xposedModule.hook(method).intercept { chain ->
                if (chain.args.firstOrNull() == "CscFeature_Common_SupportZProjectFunctionInGlobal") {
                    true
                } else {
                    chain.proceed()
                }
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    @SuppressLint("PrivateApi")
    @Suppress("DEPRECATION")
    context(xposedModule: XposedModule, param: XposedModuleInterface.PackageReadyParam)
    /**
     * Draws the module's own network speed reading in place of the one SystemUI produces.
     *
     * With [separateLines] upload and download get a line each, otherwise they share one line.
     * [compact] drops to the single direction that is moving and hides the indicator while neither
     * is, and [arrows] marks which number is which, using [markerUp] and [markerDown]. The view only
     * exists while the system's own real-time network speed setting is on.
     *
     * With [systemDefault] SystemUI builds and draws the reading itself, exactly as it does without
     * the module, and only the markers are added to what it produced. The text size and line spacing
     * are left alone in that mode, since they would change the look this mode exists to keep.
     *
     * [textSizeSp], [markerSizeSp], [markerGap] and [lineSpacingDp] are all optional: at 0 the text
     * keeps whatever the view already uses, a marker size of 0 follows the text, and a gap of 1 is
     * a single space. Sizes and the gap are spans, because the marker and the numbers share one
     * piece of text. [unit] decides bits or bytes and how the numbers are scaled, and a reading
     * below [threshold] kilo units of that unit is hidden.
     */
    fun networkSpeedIndicator(
        intervalMillisecond: Long = 3000L,
        threshold: Int = 0,
        separateLines: Boolean = false,
        compact: Boolean = false,
        systemDefault: Boolean = false,
        arrows: Boolean = true,
        markerUp: String = NetworkSpeedMarker.UP[0],
        markerDown: String = NetworkSpeedMarker.DOWN[0],
        unit: NetworkSpeedUnit.Unit = NetworkSpeedUnit.UNITS[0],
        textSizeSp: Float = 0f,
        markerSizeSp: Float = 0f,
        markerGap: Float = 1f,
        lineSpacingDp: Float = 0f,
    ) = afterAttach {
        if (param.packageName != Package.SYSTEMUI || intervalMillisecond <= 0L) {
            return@afterAttach
        }
        val netSpeedViewString = "${Package.SYSTEMUI}.statusbar.policy.NetspeedView"
        val controllerString = "${netSpeedViewString}Controller"

        data class NetworkStats(val totalTx: Long, val totalRx: Long, val interfaces: Set<String>)

        val messageInitial = 1
        val messageUpdate = 2

        // Held so the readout can be applied to the view directly, which keeps the size and
        // spacing spans when the view would otherwise flatten the text it is handed.
        var speedView: TextView? = null

        // Whether the system's own reading has already been reported as unattributable, and whether
        // its number has already been reported as unreadable.
        var reportedSystemReading = false
        var reportedSystemUnit = false

        var lastNetworkStats = NetworkStats(0L, 0L, emptySet())
        var lastUpdateTime = 0L

        fun Handler.scheduleNextUpdate() {
            sendEmptyMessageDelayed(messageUpdate, intervalMillisecond)
        }

        fun getCurrentNetworkStats(): NetworkStats {
            var totalTx = 0L
            var totalRx = 0L
            val validInterfaces = mutableSetOf<String>()
            try {
                // 获取设备上所有的网络接口
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    // 排除本地回环接口(lo)和VPN虚拟接口(tun)
                    if (networkInterface.isUp &&
                        !networkInterface.isVirtual &&
                        !networkInterface.isLoopback &&
                        !networkInterface.name.startsWith("tun") &&
                        !networkInterface.name.startsWith("dummy")
                    ) {
                        totalTx += TrafficStats.getTxBytes(networkInterface.name)
                        totalRx += TrafficStats.getRxBytes(networkInterface.name)
                        validInterfaces.add(networkInterface.name)
                    }
                }
            } catch (t: Throwable) {
                // 如果出错，回退到可能不准的方法，但至少不会崩溃
                xlog(t)
            }
            return NetworkStats(totalTx, totalRx, validInterfaces)
        }

        // 格式化网速，speed 为每秒字节数
        fun formatSpeed(bytesPerSecond: Float): String {
            // The reading is in bytes per second; the unit decides bits or bytes and the scaling
            // base, so a hundred megabit link can read as 100Mb or as 12.5MB.
            val scaled = if (unit.bits) bytesPerSecond * 8f else bytesPerSecond
            if (scaled <= 0f) {
                return "0${unit.byteSuffix}"
            }
            if (scaled < unit.base) {
                return "${scaled.roundToInt()}${unit.byteSuffix}"
            }
            val kilos = scaled / unit.base
            if (kilos < 100f) {
                return "%.2f${unit.kiloPrefix}".format(kilos)
            }
            if (kilos < 1000f) {
                return "%.1f${unit.kiloPrefix}".format(kilos)
            }
            val megas = kilos / unit.base
            if (megas < 100f) {
                return "%.2f${unit.megaPrefix}".format(megas)
            }
            return "%.1f${unit.megaPrefix}".format(megas)
        }

        fun shouldDisplayNetworkSpeed(
            txBytesPerSecond: Float,
            rxBytesPerSecond: Float,
            threshold: Int,
        ): Boolean {
            if (threshold <= 0) return true
            // The threshold is given in the unit the reading is written in, so it scales and
            // converts the same way: whole kilo units of bits or of bytes.
            val thresholdBytesPerSecond =
                threshold * unit.base / (if (unit.bits) 8f else 1f)
            return txBytesPerSecond > thresholdBytesPerSecond ||
                    rxBytesPerSecond > thresholdBytesPerSecond
        }

        // Writes one reading: the marker, a gap whose width is adjustable, then the number.
        fun appendReading(
            text: SpannableStringBuilder,
            marker: String,
            speed: String,
        ) {
            if (marker.isNotEmpty()) {
                val markerStart = text.length
                text.append(marker)
                if (markerSizeSp > 0f) {
                    text.setSpan(
                        AbsoluteSizeSpan(markerSizeSp.roundToInt(), true),
                        markerStart,
                        text.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                if (markerGap > 0f) {
                    val gapStart = text.length
                    text.append(" ")
                    if (markerGap != 1f) {
                        text.setSpan(
                            ScaleXSpan(markerGap),
                            gapStart,
                            text.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            text.append(speed)
        }

        fun calculateSpeedString(
            current: NetworkStats,
            previous: NetworkStats,
            actualIntervalSeconds: Float
        ): CharSequence {
            val txBytesPerSecond = (current.totalTx - previous.totalTx) / actualIntervalSeconds
            val rxBytesPerSecond = (current.totalRx - previous.totalRx) / actualIntervalSeconds
            if (!shouldDisplayNetworkSpeed(txBytesPerSecond, rxBytesPerSecond, threshold)) {
                return ""
            }
            val result = SpannableStringBuilder()

            // Compact: one marker for whichever direction carries the traffic, and nothing at all
            // while neither does, so the indicator uses as little of the status bar as possible.
            if (compact) {
                val speed = maxOf(txBytesPerSecond, rxBytesPerSecond)
                // Under a byte per second nothing is really transferring.
                if (speed < 1f) {
                    return ""
                }
                val marker = if (!arrows) {
                    ""
                } else if (txBytesPerSecond >= rxBytesPerSecond) {
                    markerUp
                } else {
                    markerDown
                }
                appendReading(result, marker, formatSpeed(speed))
                result.append('\u00A0')
                return result
            }

            // Upload on the first line and download on the second, or both on one line with
            // upload first. The marker keeps each number readable on its own.
            appendReading(result, if (arrows) markerUp else "", formatSpeed(txBytesPerSecond))
            if (separateLines) {
                result.append('\u00A0').append('\n')
            } else {
                result.append(' ')
            }
            appendReading(result, if (arrows) markerDown else "", formatSpeed(rxBytesPerSecond))
            result.append('\u00A0')
            return result
        }

        /**
         * Adds the markers to the reading SystemUI built itself, leaving its layout and font alone.
         *
         * Two lines get the upload marker on the first and the download marker on the second, and
         * their numbers are rewritten in the chosen unit when that unit is not the one SystemUI
         * writes. A single number cannot be attributed to a direction, so it is left as SystemUI
         * drew it and reported once, so the shape can be seen for what it is.
         */
        /**
         * Reads a speed SystemUI wrote, so it can be written again in the chosen unit.
         *
         * SystemUI writes a number, optionally scaled by K, M or G, optionally marked as bytes or
         * bits, and it is assumed to scale in powers of two. Null when the reading cannot be read,
         * in which case it is left exactly as SystemUI drew it.
         */
        fun parseSpeed(reading: String): Float? {
            val number = SPEED_NUMBER.find(reading) ?: return null
            val value = number.value.replace(',', '.').toFloatOrNull() ?: return null
            val rest = reading.substring(number.range.last + 1)
            val multiplier = when (rest.firstOrNull { it in "KkMmGg" }?.uppercaseChar()) {
                'K' -> SYSTEM_BASE
                'M' -> SYSTEM_BASE * SYSTEM_BASE
                'G' -> SYSTEM_BASE * SYSTEM_BASE * SYSTEM_BASE
                else -> 1
            }
            val scaled = value * multiplier
            // The module reads in bytes per second, so a reading marked in bits is converted.
            return if (rest.contains('b') && !rest.contains('B')) scaled / 8f else scaled
        }

        fun prefixMarkers() {
            val view = speedView ?: return
            val shown = view.text?.toString().orEmpty()
            val lines = shown.split('\n')
            if (lines.size < 2) {
                if (shown.isNotEmpty() && !reportedSystemReading) {
                    reportedSystemReading = true
                    xlog(
                        "system network speed reading is a single value, no marker added: $shown",
                        priority = android.util.Log.DEBUG
                    )
                }
                return
            }

            // SystemUI writes bytes scaled by 1024, so its numbers only have to be written again
            // when the chosen unit says something it cannot: bits, or the 1000 scale.
            val rewrite = unit.bits || unit.base != SYSTEM_BASE
            val markers = listOf(markerUp, markerDown)
            val marked = SpannableStringBuilder()
            lines.forEachIndexed { index, line ->
                if (index > 0) {
                    marked.append('\n')
                }
                val drawn = line.trim()
                val reading = if (!rewrite) {
                    drawn
                } else {
                    val speed = parseSpeed(drawn)
                    if (speed == null) {
                        if (!reportedSystemUnit) {
                            reportedSystemUnit = true
                            xlog(
                                "system network speed reading cannot be read: $drawn",
                                priority = android.util.Log.DEBUG
                            )
                        }
                        drawn
                    } else {
                        formatSpeed(speed)
                    }
                }
                val marker = if (arrows) markers.getOrNull(index).orEmpty() else ""
                if (reading.isEmpty() || marker.isEmpty()) {
                    marked.append(reading)
                } else {
                    appendReading(marked, marker, reading)
                }
            }
            if (marked.isNotEmpty()) {
                view.text = marked
            }
        }

        try {
            val handlerClass = param.classLoader.loadClass($$"$${controllerString}$NetworkSpeedManager$1")
            val handleMessageMethod = handlerClass.getDeclaredMethod("handleMessage", Message::class.java)
            xposedModule.hook(handleMessageMethod).intercept { chain ->
                try {
                    if (systemDefault) {
                        // SystemUI builds and draws the reading itself in this mode, and only the
                        // markers are added, to the text it just produced.
                        val result = chain.proceed()
                        prefixMarkers()
                        return@intercept result
                    }
                    val message = chain.args[0] as Message
                    val handler = chain.thisObject as Handler
                    val observable = chain.thisObject.reflect["this$0"] as? java.util.Observable
                    if (observable != null && observable.countObservers() > 0) {
                        when (message.what) {
                            messageInitial -> {
                                lastNetworkStats = getCurrentNetworkStats()
                                lastUpdateTime = SystemClock.elapsedRealtime()
                                handler.scheduleNextUpdate()
                            }

                            messageUpdate -> {
                                val currentNetworkStats = getCurrentNetworkStats()
                                val currentTime = SystemClock.elapsedRealtime()
                                if (currentNetworkStats.interfaces == lastNetworkStats.interfaces &&
                                    currentNetworkStats.totalTx >= lastNetworkStats.totalTx &&
                                    currentNetworkStats.totalRx >= lastNetworkStats.totalRx &&
                                    currentTime > lastUpdateTime
                                ) {
                                    val actualIntervalSeconds = (currentTime - lastUpdateTime) / 1000f
                                    val speedString = calculateSpeedString(
                                        currentNetworkStats,
                                        lastNetworkStats,
                                        actualIntervalSeconds
                                    )

                                    observable.reflect.call("setChanged")
                                    // The view expects a plain string from its observer, so it is handed
                                    // one, and the marked-up reading is put on the view afterwards,
                                    // which is what keeps the marker size and spacing spans. Handing
                                    // it the spanned reading instead left the view empty.
                                    try {
                                        observable.notifyObservers(speedString.toString())
                                    } catch (t: Throwable) {
                                        xlog(t)
                                    }
                                    if (speedString.isNotEmpty()) {
                                        speedView?.apply {
                                            visibility = View.VISIBLE
                                            text = speedString
                                        }
                                    }
                                }

                                lastNetworkStats = currentNetworkStats
                                lastUpdateTime = currentTime
                                handler.scheduleNextUpdate()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    xlog(t)
                }
                null
            }

            val netspeedViewClass = param.classLoader.loadClass(netSpeedViewString)
            val onFinishInflateMethod = netspeedViewClass.getDeclaredMethod("onFinishInflate")
            xposedModule.hook(onFinishInflateMethod).intercept { chain ->
                val result = chain.proceed()
                try {
                    (chain.thisObject.reflect["mContentView"] as? TextView)?.apply {
                        speedView = this
                        // The system default keeps every property SystemUI set and only gains the
                        // markers, so nothing else is touched in that mode.
                        if (systemDefault) {
                            return@apply
                        }
                        setLines(if (separateLines && !compact) 2 else 1)
                        gravity = Gravity.END
                        textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                        setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                        if (textSizeSp > 0f) {
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                        }
                        if (lineSpacingDp > 0f) {
                            setLineSpacing(
                                TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    lineSpacingDp,
                                    resources.displayMetrics
                                ),
                                1f
                            )
                        }
                        speedView = this
                    }
                } catch (t: Throwable) {
                    xlog(t)
                }
                result
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    @SuppressLint("PrivateApi")
    context(xposedModule: XposedModule, param: XposedModuleInterface.PackageReadyParam)
    fun turnOn5gQsTile() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return
        }
        if (param.packageName == Package.SYSTEMUI ||
            param.packageName == Package.TELEPHONYUI
        ) {
            try {
                val semCscFeatureClass =
                    param.classLoader.loadClass("com.samsung.android.feature.SemCscFeature")
                semCscFeatureClass.declaredMethods
                    .filter { it.name == "getString" }
                    .forEach { method ->
                        xposedModule.hook(method).intercept { chain ->
                            val result = chain.proceed()
                            if (chain.args.firstOrNull() == "CscFeature_SystemUI_ConfigDefQuickSettingItem") {
                                val quickSettingItem = result as? String ?: ""
                                if (!quickSettingItem.contains("TurnOn5g")) {
                                    "$quickSettingItem,TurnOn5g"
                                } else {
                                    quickSettingItem
                                }
                            } else {
                                result
                            }
                        }
                    }
            } catch (t: Throwable) {
                xlog(t)
            }
        }
        if (param.packageName == Package.SYSTEMUI) {
            try {
                val qsTileHostClass = param.classLoader.loadClass("com.android.systemui.qs.QSTileHost")
                val isAvailableCustomTileMethod =
                    qsTileHostClass.getDeclaredMethod("isAvailableCustomTile", String::class.java)
                xposedModule.hook(isAvailableCustomTileMethod).intercept { chain ->
                    if (chain.args.firstOrNull() == "TurnOn5g") {
                        true
                    } else {
                        chain.proceed()
                    }
                }
            } catch (t: Throwable) {
                xlog(t)
            }
        }
    }

    context(xposedModule: XposedModule, param: XposedModuleInterface.PackageReadyParam)
    fun showWiFiLinkSpeed() {
        if (param.packageName != Package.SETTINGS) {
            return
        }
        try {
            val connectedListAdapterClass = param.classLoader.loadClass(
                "com.samsung.android.settings.wifi.ConnectedListAdapter"
            )
            val viewHolderClass = param.classLoader.loadClass(
                $$"androidx.recyclerview.widget.RecyclerView$ViewHolder"
            )

            fun getLinkSpeed(thisObject: Any, position: Int): String? {
                val wifiEntries = thisObject.reflect["mWifiEntries"] as? List<*> ?: return null
                val wifiEntry = wifiEntries.getOrNull(position) ?: return null
                val wifiInfo = wifiEntry.reflect["mWifiInfo"] as? WifiInfo ?: return null
                return "${wifiInfo.txLinkSpeedMbps},${wifiInfo.rxLinkSpeedMbps}"
            }

            val onBindViewHolderMethod = connectedListAdapterClass.getDeclaredMethod(
                "onBindViewHolder",
                viewHolderClass,
                Int::class.javaPrimitiveType
            )
            xposedModule.hook(onBindViewHolderMethod).intercept { chain ->
                val result = chain.proceed()
                try {
                    val position = chain.args[1] as Int
                    val linkSpeed = getLinkSpeed(chain.thisObject, position)
                    if (linkSpeed != null) {
                        val mSummary = chain.args[0].reflect["mSummary"] as? TextView
                        mSummary?.append(" $linkSpeed")
                    }
                } catch (t: Throwable) {
                    xlog(t)
                }
                result
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }
}
