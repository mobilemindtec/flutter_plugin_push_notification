package io.flutter.plugins.push.extra

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log

/**
 * Activity which is an entry point, whenever a notification from the bar is tapped and executed.
 * The activity fires, notifies the callback.
 */
class PushHandlerActivity : Activity() {

    /*
     * this activity will be started if the user touches a notification that we own.
     * We send it's data off to the push plugin for processing.
     * If needed, we boot up the main activity to kickstart the application.
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Creating...")

        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")

        val isPushPluginActive = PushPlugin.isActive
        processPushBundle(isPushPluginActive)

        // remove this activity from the top of the stack
        finish()

        Log.d(TAG, "isPushPluginActive = $isPushPluginActive")
        if (!isPushPluginActive) {
            forceMainActivityReload()
        }
    }

    /**
     * Takes the pushBundle extras from the intent,
     * and sends it through to the PushPlugin for processing.
     */
    private fun processPushBundle(isPushPluginActive: Boolean) {
        val extras = intent.extras
        Log.d(TAG, "Processing push extras: IsPushPluginActive = $isPushPluginActive")

        if (extras != null) {
            Log.d(TAG, "Has extras.")
            val originalExtras = extras!!.getBundle("pushBundle")

            if(originalExtras != null) {
                originalExtras.putBoolean("foreground", false)
                originalExtras.putBoolean("coldstart", !isPushPluginActive)
            }

            //PushPlugin.executeOnMessageReceivedCallback(originalExtras);
        }
    }

    /**
     * Forces the main activity to re-launch if it's unloaded.
     */
    private fun forceMainActivityReload() {
        val pm = packageManager
        val launchIntent = pm.getLaunchIntentForPackage(applicationContext.packageName)
        Log.d(TAG, "starting activity for package: " + applicationContext.packageName)
        launchIntent!!.setPackage(null)
        startActivity(launchIntent)
    }


    override fun onResume() {
        Log.d(TAG, "On resume")
        super.onResume()
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    companion object {
        private val TAG = "HandlerActivity"
    }

}