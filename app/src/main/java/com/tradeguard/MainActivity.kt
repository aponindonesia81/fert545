package com.tradeguard

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) { pushState() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "TradeGuard")
        web.loadUrl("file:///android_asset/index.html")
        setContentView(web)
        registerReceiver(receiver, IntentFilter(ACTION_STATE), RECEIVER_NOT_EXPORTED)
    }

    inner class Bridge {
        @JavascriptInterface fun enableAccessibility() {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        @JavascriptInterface fun startCapture() {
            requestCapture()
        }

        @JavascriptInterface fun saveSettings(limit: Int, hours: Int, zero: Boolean, l: Float, t: Float, r: Float, b: Float) {
            GuardPrefs.saveSettings(this@MainActivity, limit, hours, zero, l, t, r, b)
            pushState()
        }

        @JavascriptInterface fun testLock() {
            val h = GuardPrefs.hours(this@MainActivity)
            GuardPrefs.setLock(this@MainActivity, System.currentTimeMillis() + h * 3600000L)
            pushState()
        }

        @JavascriptInterface fun reset() {
            GuardPrefs.reset(this@MainActivity)
            pushState()
        }

        @JavascriptInterface fun getState(): String = stateJson().toString()
    }

    private fun requestCapture() {
        val mgr = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(mgr.createScreenCaptureIntent(), REQ_CAPTURE)
    }

    @Deprecated("legacy callback is sufficient for this test build")
    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == REQ_CAPTURE && res == RESULT_OK && data != null) {
            val s = Intent(this, ScreenSensorService::class.java)
                .putExtra("resultCode", res).putExtra("data", data)
            startForegroundService(s)
            Toast.makeText(this, "Screen sensor started", Toast.LENGTH_SHORT).show()
            pushState()
        }
    }

    override fun onResume() { super.onResume(); pushState() }

    private fun stateJson(): JSONObject = JSONObject().apply {
        put("locked", GuardPrefs.locked(this@MainActivity))
        put("lockUntil", GuardPrefs.lockUntil(this@MainActivity))
        put("total", GuardPrefs.total(this@MainActivity))
        put("wins", GuardPrefs.wins(this@MainActivity))
        put("losses", GuardPrefs.losses(this@MainActivity))
        put("streak", GuardPrefs.streak(this@MainActivity))
        put("limit", GuardPrefs.limit(this@MainActivity))
        put("hours", GuardPrefs.hours(this@MainActivity))
        put("zeroIsLoss", GuardPrefs.zeroIsLoss(this@MainActivity))
        val roi = GuardPrefs.roi(this@MainActivity)
        put("roiL", roi[0]); put("roiT", roi[1]); put("roiR", roi[2]); put("roiB", roi[3])
    }

    private fun pushState() {
        if (!::web.isInitialized) return
        val json = JSONObject.quote(stateJson().toString())
        web.post { web.evaluateJavascript("window.nativeState($json)", null) }
    }

    override fun onDestroy() {
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        web.destroy()
        super.onDestroy()
    }

    companion object {
        const val REQ_CAPTURE = 9001
        const val ACTION_STATE = "com.tradeguard.STATE"
    }
}
