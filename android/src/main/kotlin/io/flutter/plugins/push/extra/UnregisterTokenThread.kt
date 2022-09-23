package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException

/**
 * Responsible for unregister device from GCM service functionality.
 * By design, this must happen in async way in a Thread.
 */
class UnregisterTokenThread(private val appContext: Context, private val callbacks: PushPluginListener?) : Thread() {

    override fun run() {
        try {
            deleteTokenFrom()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun deleteTokenFrom() {

        FirebaseMessaging.getInstance().deleteToken();

        //val instanceId = FirebaseInstanceId.getInstance()
        //instanceId.deleteInstanceId()

        Log.d(TAG, "Token deleted!")

        if (callbacks != null) {
            callbacks!!.success("Device unregistered!")
        }

        // TODO: Wrap the whole callback.
        PushPlugin.isActive = false
    }

    companion object {
        private val TAG = "UnregisterTokenThread"
    }
}