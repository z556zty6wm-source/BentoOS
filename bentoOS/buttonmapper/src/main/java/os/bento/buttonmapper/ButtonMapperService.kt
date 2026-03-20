package os.bento.buttonmapper

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat

class ButtonMapperService : AccessibilityService() {

    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var currentApp = ""
    private val mappings = mutableMapOf<String, MutableMap<Int, BentoAction>>()

    companion object {
        const val CHANNEL_ID = "bento_buttonmapper"
        const val NOTIF_ID = 1001
        val DEFAULT_MAPPINGS = mapOf(
            KeyEvent.KEYCODE_BUTTON_START  to BentoAction.SPOTLIGHT,
            KeyEvent.KEYCODE_BUTTON_SELECT to BentoAction.MISSION_CONTROL,
            KeyEvent.KEYCODE_F1            to BentoAction.SPOTLIGHT,
            KeyEvent.KEYCODE_F2            to BentoAction.MISSION_CONTROL,
            KeyEvent.KEYCODE_F3            to BentoAction.SCREENSHOT,
            KeyEvent.KEYCODE_F4            to BentoAction.CONTROL_CENTER,
            KeyEvent.KEYCODE_MENU          to BentoAction.CONTROL_CENTER,
        )
        const val ACTION_UPDATE_MAPPINGS = "os.bento.buttonmapper.UPDATE"
        const val ACTION_RELOAD          = "os.bento.buttonmapper.RELOAD"
        const val EXTRA_APP_PACKAGE      = "app_package"
        const val EXTRA_KEYCODE          = "keycode"
        const val EXTRA_ACTION           = "action"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences("bento_button_mappings", Context.MODE_PRIVATE)
        loadMappings()
        startForegroundNotification()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            currentApp = event.packageName?.toString() ?: ""
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        val keycode = event.keyCode
        val appMapping = mappings[currentApp]
        if (appMapping != null && appMapping.containsKey(keycode)) {
            executeAction(appMapping[keycode]!!)
            return true
        }
        val globalMapping = mappings["global"]
        if (globalMapping != null && globalMapping.containsKey(keycode)) {
            executeAction(globalMapping[keycode]!!)
            return true
        }
        val defaultAction = DEFAULT_MAPPINGS[keycode]
        if (defaultAction != null) {
            executeAction(defaultAction)
            return true
        }
        return false
    }

    private fun executeAction(action: BentoAction) {
        when (action) {
            BentoAction.SPOTLIGHT        -> sendBroadcast(Intent("os.bento.systemui.SHOW_SPOTLIGHT"))
            BentoAction.MISSION_CONTROL  -> sendBroadcast(Intent("os.bento.systemui.SHOW_MISSION_CONTROL"))
            BentoAction.CONTROL_CENTER   -> sendBroadcast(Intent("os.bento.systemui.SHOW_CONTROL_CENTER"))
            BentoAction.SCREENSHOT       -> performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            BentoAction.HOME             -> performGlobalAction(GLOBAL_ACTION_HOME)
            BentoAction.BACK             -> performGlobalAction(GLOBAL_ACTION_BACK)
            BentoAction.RECENTS          -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            BentoAction.NOTIFICATIONS    -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            BentoAction.GAMING_MODE      -> sendBroadcast(Intent("os.bento.systemui.TOGGLE_GAMING_MODE"))
            BentoAction.VOLUME_UP        -> sendBroadcast(Intent("os.bento.systemui.VOLUME_UP"))
            BentoAction.VOLUME_DOWN      -> sendBroadcast(Intent("os.bento.systemui.VOLUME_DOWN"))
            BentoAction.LAUNCH_FILES     -> {
                val intent = packageManager.getLaunchIntentForPackage("os.bento.filemanager")
                intent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
            BentoAction.LAUNCH_SETTINGS  -> {
                val intent = packageManager.getLaunchIntentForPackage("os.bento.settings")
                intent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
            BentoAction.NONE -> {}
        }
    }

    private fun loadMappings() {
        mappings.clear()
        val all = prefs.all
        for ((key, value) in all) {
            val parts = key.split(":")
            if (parts.size != 2) continue
            val pkg = parts[0]
            val keycode = parts[1].toIntOrNull() ?: continue
            val actionName = value as? String ?: continue
            val action = try { BentoAction.valueOf(actionName) } catch (e: Exception) { continue }
            if (!mappings.containsKey(pkg)) mappings[pkg] = mutableMapOf()
            mappings[pkg]!![keycode] = action
        }
    }

    fun saveMapping(pkg: String, keycode: Int, action: BentoAction) {
        prefs.edit().putString("$pkg:$keycode", action.name).apply()
        loadMappings()
    }

    fun removeMapping(pkg: String, keycode: Int) {
        prefs.edit().remove("$pkg:$keycode").apply()
        loadMappings()
    }

    private fun startForegroundNotification() {
        val channel = NotificationChannel(CHANNEL_ID, "Button Mapper", NotificationManager.IMPORTANCE_MIN)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BentoOS Button Mapper")
            .setContentText("Active")
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
        startForeground(NOTIF_ID, notification)
    }

    override fun onInterrupt() {}
}

enum class BentoAction(val label: String) {
    NONE            ("None"),
    HOME            ("Go Home"),
    BACK            ("Go Back"),
    RECENTS         ("Recent Apps"),
    SCREENSHOT      ("Take Screenshot"),
    SPOTLIGHT       ("Open Spotlight"),
    MISSION_CONTROL ("Mission Control"),
    CONTROL_CENTER  ("Control Center"),
    NOTIFICATIONS   ("Notifications"),
    GAMING_MODE     ("Toggle Gaming Mode"),
    VOLUME_UP       ("Volume Up"),
    VOLUME_DOWN     ("Volume Down"),
    LAUNCH_FILES    ("Open Files"),
    LAUNCH_SETTINGS ("Open Settings"),
}
