package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import java.io.IOException

/**
 * Responsible for unregister device from GCM service functionality.
 * By design, this must happen in async way in a Thread.
 */
class UnregisterTokenThread(private val projectId: String, private val appContext: Context, private val callbacks: PushPluginListener?) : Thread() {

    override fun run() {
        try {
            deleteTokenFrom()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun deleteTokenFrom() {
        val instanceId = FirebaseInstanceId.getInstance()
        instanceId.deleteInstanceId()
        //instanceId.deleteToken(this.projectId,
        //        GoogleCloudMessaging.INSTANCE_ID_SCOPE);

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