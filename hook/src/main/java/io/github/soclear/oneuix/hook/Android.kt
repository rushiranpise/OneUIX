package io.github.soclear.oneuix.hook

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.os.Bundle
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.soclear.oneuix.hook.util.reflect
import io.github.soclear.oneuix.hook.util.xlog

@SuppressLint("PrivateApi")
object Android {
    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun disableWritingToolkitGlobally() {
        val galaxyAiRestrictionsPackage = "com.samsung.android.knox.galaxyai"
        val writingToolkitKey = "key_writing_toolkit"
        val grayoutKey = "grayout"

        val classLoader = param.classLoader

        try {
            val proxyClass = classLoader.loadClass("com.android.server.enterprise.EDMProxyService")
            proxyClass.declaredMethods.filter {
                it.name == "getApplicationRestrictions"
            }.forEach {
                xposedModule.hook(it).intercept { chain ->
                    val result = chain.proceed()
                    if (chain.args.getOrNull(0) != galaxyAiRestrictionsPackage) {
                        result
                    } else {
                        val restrictions = Bundle(result as? Bundle ?: Bundle.EMPTY)
                        val writingToolkit = Bundle(restrictions.getBundle(writingToolkitKey) ?: Bundle.EMPTY)
                        writingToolkit.putBoolean(grayoutKey, true)
                        restrictions.putBundle(writingToolkitKey, writingToolkit)
                        restrictions
                    }
                }
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    private const val OUTDOOR_MODE = "display_outdoor_mode"
    private const val CONTENT_PROVIDER_CLASS = "android.content.ContentProvider"
    private const val SETTINGS_PROVIDER_CLASS = "com.android.providers.settings.SettingsProvider"
    private const val ENFORCE_MUTATION_METHOD =
        "enforceRestrictedSystemSettingsMutationForCallingPackage"

    /**
     * Lets the app write "display_outdoor_mode" itself, so the Outdoor mode tile needs no root.
     *
     * The provider refuses a system setting that is not in its PUBLIC_SETTINGS list when the caller
     * only holds the WRITE_SETTINGS app op. This lifts that check for this one key only; every other
     * setting keeps the normal restriction.
     */
    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun allowOutdoorModeWriteFromApp() {
        try {
            // The provider lives in its own APK and is loaded by its own classloader, so looking it up
            // by name from system_server throws ClassNotFoundException. Take the class from the first
            // provider instance that gets constructed instead.
            val contentProviderClass = param.classLoader.loadClass(CONTENT_PROVIDER_CLASS)
            contentProviderClass.declaredConstructors.forEach { constructor ->
                xposedModule.hook(constructor).intercept { chain ->
                    val result = chain.proceed()
                    val provider = chain.thisObject
                    if (provider?.javaClass?.name == SETTINGS_PROVIDER_CLASS) {
                        hookProviderEnforcement(provider.javaClass)
                    }
                    result
                }
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    context(xposedModule: XposedModule)
    private fun hookProviderEnforcement(providerClass: Class<*>) {
        try {
            val method = providerClass.declaredMethods.firstOrNull {
                it.name == ENFORCE_MUTATION_METHOD
            } ?: return

            xposedModule.hook(method).intercept { chain ->
                // (int operation, String name, int userId)
                if (chain.args.getOrNull(1) == OUTDOOR_MODE) null else chain.proceed()
            }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    @SuppressLint("BlockedPrivateApi")
    context(xposedModule: XposedModule)
    fun setBlockableNotificationChannel() {
        try {
            val notificationChannelClass = NotificationChannel::class.java

            notificationChannelClass.declaredConstructors.forEach {
                xposedModule.hook(it).intercept { chain ->
                    val result = chain.proceed()
                    chain.thisObject.reflect["mBlockableSystem"] = true
                    chain.thisObject.reflect["mImportanceLockedByOEM"] = false
                    chain.thisObject.reflect["mImportanceLockedDefaultApp"] = false
                    result
                }
            }

            notificationChannelClass
                .getDeclaredMethod("setBlockable", Boolean::class.javaPrimitiveType)
                .let { xposedModule.hook(it) }
                .intercept { chain ->
                    val newArgs = chain.args.toTypedArray()
                    newArgs[0] = true
                    chain.proceed(newArgs)
                }

            notificationChannelClass
                .getDeclaredMethod("setImportanceLockedByOEM", Boolean::class.javaPrimitiveType)
                .let { xposedModule.hook(it) }
                .intercept { chain ->
                    val newArgs = chain.args.toTypedArray()
                    newArgs[0] = false
                    chain.proceed(newArgs)
                }

            notificationChannelClass
                .getDeclaredMethod("setImportanceLockedByCriticalDeviceFunction", Boolean::class.javaPrimitiveType)
                .let { xposedModule.hook(it) }
                .intercept { chain ->
                    val newArgs = chain.args.toTypedArray()
                    newArgs[0] = false
                    chain.proceed(newArgs)
                }
        } catch (t: Throwable) {
            xlog(t)
        }
    }


    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun setMaxNeverKilledAppNum(num: Int) {
        try {
            param.classLoader.loadClass("com.android.server.am.DynamicHiddenApp").reflect["MAX_NEVERKILLEDAPP_NUM"] =
                num
        } catch (t: Throwable) {
            xlog(t)
        }
    }


    // 解除国行/港版对 GMS（含 FCM 推送）的网络限制
    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun liftFcmNetworkLimit() {
        try {
            param.classLoader
                .loadClass("com.android.server.alarm.GmsAlarmManager")
                .constructors
                .forEach {
                    xposedModule.hook(it).intercept { chain ->
                        val result = chain.proceed()
                        chain.thisObject.reflect["isChinaMode"] = false
                        chain.thisObject.reflect["isHongKongMode"] = false
                        result
                    }
                }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    // 禁用每 72 小时验证锁屏密码
    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun disablePinVerifyPer72h() {
        try {
            param.classLoader
                .loadClass("com.android.server.locksettings.LockSettingsStrongAuth")
                .declaredMethods
                .filter { it.name == "rescheduleStrongAuthTimeoutAlarm" }
                .forEach {
                    xposedModule.hook(it).intercept { null }
                }
        } catch (t: Throwable) {
            xlog(t)
        }
    }

    // 移除充电器时禁止亮屏
    // PowerManagerService.updateIsPoweredLocked 在插拔充电器时会调用 wakePowerGroupLocked 点亮屏幕，
    // 唤醒理由字符串为 "android.server.power:PLUGGED:" + mIsPowered。
    // 拔出充电器时 mIsPowered 为 false，拦截该次唤醒即可（插入仍正常亮屏）。
    context(xposedModule: XposedModule, param: XposedModuleInterface.SystemServerStartingParam)
    fun disableScreenWakeOnPowerUnplugged() {
        try {
            param.classLoader
                .loadClass("com.android.server.power.PowerManagerService")
                .declaredMethods
                .filter { it.name == "wakePowerGroupLocked" }
                .forEach {
                    xposedModule.hook(it).intercept { chain ->
                        if (chain.args.getOrNull(3) == "android.server.power:PLUGGED:false") {
                            null
                        } else {
                            chain.proceed()
                        }
                    }
                }
        } catch (t: Throwable) {
            xlog(t)
        }
    }
}
